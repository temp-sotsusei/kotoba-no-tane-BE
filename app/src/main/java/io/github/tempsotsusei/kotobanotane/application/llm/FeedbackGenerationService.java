package io.github.tempsotsusei.kotobanotane.application.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.infrastructure.external.openai.OpenAiClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 4〜6歳の子どもが書いた文章を、文法だけやさしく直す LLM 呼び出しサービス。
 *
 * <p>LLM から構造化されたフィードバック配列を受け取り、original/corrected/reason のリストに変換する。
 */
@Service
public class FeedbackGenerationService {

  private static final String SYSTEM_PROMPT =
      """
			あなたは4〜6歳の子どもが書いた文章を、文法だけやさしく直すアシスタントです。
			内容やストーリーへの意見は不要です。
			出力はできるだけひらがなで、難しい言葉は使わないでください。

			出力は必ず JSON で返してください。フィールドは `feedbacks` のみで、以下の構造に従ってください:
			- feedbacks: 配列。間違いの数だけ要素を入れる（上限なし、間違いが無ければ空配列）。
			- 各要素はオブジェクトで以下を含む:
			  - original: 修正前の部分文（全文は入れない）
			  - corrected: 文法だけ直した文（意味は変えない）
			  - reason: 子どもにもわかるやさしい説明（短く）

			守ること:
			- 原文を文/行ごとに見て、文法が変なところだけをセット化する。
			- 1セットにつき1つの直し（全体まとめはしない）。原文の順番どおりに並べる。
			- 余計なフィールドや文字列化した JSON を返さない。feedbacks 以外は出さない。
			- 原文は original 以外で書き換えない。意味や内容には触れない。
			- 文法的に直すところが無い場合は、feedbacks を空配列にする。無理に間違いを作らない。

			出力例:
			{
			  "feedbacks": [
			    {
			      "original": "わたし、そらのしたで",
			      "corrected": "わたしはそらのしたで",
			      "reason": "「わたし」のあとに「は」がなくて、ぶんがつながっていないから。"
			    },
			    {
			      "original": "わたしにへあるいていって、",
			      "corrected": "わたしのほうへあるいていって、",
			      "reason": "「にへ」はへんなつながりで、どこにいくのかがわからなくなるから。"
			    }
			  ]
			}
			""";

  private static final int MAX_OUTPUT_TOKENS = 2000;

  private final OpenAiClient openAiClient;
  private final ObjectMapper objectMapper;

  public FeedbackGenerationService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
    this.openAiClient = openAiClient;
    this.objectMapper = objectMapper;
  }

  /**
   * 章本文からフィードバックの配列を生成する。
   *
   * @param chapterText 平文化済み章本文
   * @return original/corrected/reason を含むフィードバックリスト
   */
  public List<FeedbackItem> generate(String chapterText) {
    if (!StringUtils.hasText(chapterText)) {
      throw new IllegalArgumentException("chapterText must not be blank");
    }

    JsonNode schema = buildSchema();
    OpenAiStructuredRequest request =
        new OpenAiStructuredRequest(
            SYSTEM_PROMPT, chapterText, schema, "feedbacks_wrapper", MAX_OUTPUT_TOKENS);
    JsonNode response = openAiClient.requestStructuredJson(request);
    return parseFeedbacks(response.path("feedbacks"));
  }

  private List<FeedbackItem> parseFeedbacks(JsonNode feedbacksNode) {
    if (feedbacksNode == null || !feedbacksNode.isArray()) {
      return Collections.emptyList();
    }
    List<FeedbackItem> items = new ArrayList<>();
    for (JsonNode node : feedbacksNode) {
      String original = node.path("original").asText("");
      String corrected = node.path("corrected").asText("");
      String reason = node.path("reason").asText("");
      if (StringUtils.hasText(original)
          || StringUtils.hasText(corrected)
          || StringUtils.hasText(reason)) {
        items.add(new FeedbackItem(original, corrected, reason));
      }
    }
    return items;
  }

  /** Structured Outputs 用のスキーマを構築する（feedbacks: array of objects）。 */
  private JsonNode buildSchema() {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("type", "object");
    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode feedbacksArray = objectMapper.createObjectNode();
    feedbacksArray.put("type", "array");
    ObjectNode feedbackItem = objectMapper.createObjectNode();
    feedbackItem.put("type", "object");
    ObjectNode itemProps = objectMapper.createObjectNode();
    itemProps.putObject("original").put("type", "string");
    itemProps.putObject("corrected").put("type", "string");
    itemProps.putObject("reason").put("type", "string");
    feedbackItem.set("properties", itemProps);
    feedbackItem.set(
        "required", objectMapper.createArrayNode().add("original").add("corrected").add("reason"));
    feedbackItem.put("additionalProperties", false);
    feedbacksArray.set("items", feedbackItem);

    properties.set("feedbacks", feedbacksArray);
    root.set("properties", properties);
    root.set("required", objectMapper.createArrayNode().add("feedbacks"));
    root.put("additionalProperties", false);
    return root;
  }
}
