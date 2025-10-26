package io.github.tempsotsusei.kotobanotane.application.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import io.github.tempsotsusei.kotobanotane.infrastructure.external.openai.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 非同期ジョブの動作確認を目的とした簡易 LLM 呼び出しサービス。
 *
 * <p>OpenAI にはジョブ番号を付けて極小の入力を送り、結果は利用せずに破棄する。ジョブ完了は {@code users.updated_at} の更新で判断する。
 */
@Service
public class AsyncLlmJobService {

  private static final Logger log = LoggerFactory.getLogger(AsyncLlmJobService.class);

  private static final String SYSTEM_PROMPT = "Async job invocation logger.";
  private static final int MAX_OUTPUT_TOKENS = 16;

  private final OpenAiClient openAiClient;
  private final ObjectMapper objectMapper;
  private final UserService userService;

  public AsyncLlmJobService(
      OpenAiClient openAiClient, ObjectMapper objectMapper, UserService userService) {
    this.openAiClient = openAiClient;
    this.objectMapper = objectMapper;
    this.userService = userService;
  }

  /**
   * 非同期 LLM ジョブを実行し、完了したらユーザーの {@code updated_at} を進める。
   *
   * @param auth0Id 対象ユーザー
   * @param jobIndex 同一リクエスト内でのジョブ番号（1 始まり）
   */
  @Async("llmJobExecutor")
  public void runJob(String auth0Id, int jobIndex) {
    try {
      String userInput = "job-%d".formatted(jobIndex);
      JsonNode schema = buildAckSchema();
      OpenAiStructuredRequest request =
          new OpenAiStructuredRequest(
              SYSTEM_PROMPT, userInput, schema, "async_job_ack", MAX_OUTPUT_TOKENS);
      // 応答内容は使用しないため破棄する
      openAiClient.requestStructuredJson(request);
      userService.update(auth0Id);
      log.info("Async job completed auth0Id={} jobIndex={}", auth0Id, jobIndex);
    } catch (Exception e) {
      log.warn("Async job failed auth0Id={} jobIndex={}", auth0Id, jobIndex, e);
    }
  }

  private JsonNode buildAckSchema() {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("type", "object");
    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode ack = objectMapper.createObjectNode();
    ack.put("type", "string");
    ack.put("description", "tiny acknowledgement such as 'ok'");
    ack.put("maxLength", 8);
    properties.set("ack", ack);

    root.set("properties", properties);
    root.set("required", objectMapper.createArrayNode().add("ack"));
    root.put("additionalProperties", false);
    return root;
  }
}
