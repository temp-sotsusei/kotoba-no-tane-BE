package io.github.tempsotsusei.kotobanotane.application.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.infrastructure.external.openai.OpenAiClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 文章からキーワード一覧を生成する用途別サービス。 */
@Service
public class KeywordListsGenerationService {

  private static final String SYSTEM_PROMPT =
      "あなたは4〜6歳の子ども向けに日本語文章からキーワードセットを抽出するアシスタントです。"
          + "各セットは「関連しそうな単語2つ」と「関連が薄い単語2つ」の計4語で構成してください。"
          + "すべての単語はひらがなで、可能であれば10文字以内に収めてください。"
          + "セットは必ず3つ返し、JSON配列以外の出力は行わないでください。";

  private static final String INITIAL_USER_PROMPT_TEMPLATE =
      """
			これは章本文が存在しない初回サジェスト用の依頼です。seed=%s
			子どもがワクワクするようなテーマや季節、感情、小さな発見などを自由に想像し、
			それぞれのセットに関連語2つ・無関係な遊び心ある単語2つを混ぜてください。
			ひらがな・10文字以内を守りつつ、毎回違う切り口になるよう意識してください。
			""";

  private static final int MAX_OUTPUT_TOKENS = 2000;

  private final OpenAiClient openAiClient;
  private final ObjectMapper objectMapper;

  public KeywordListsGenerationService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
    this.openAiClient = openAiClient;
    this.objectMapper = objectMapper;
  }

  /** 章テキストからキーワードの配列（配列の配列）を生成する。内部的には OpenAI の structured outputs を使用する。 */
  public List<List<String>> generate(String chapterText) {
    if (!StringUtils.hasText(chapterText)) {
      throw new IllegalArgumentException("chapterText must not be blank");
    }

    JsonNode schema = buildKeywordObjectSchema();
    OpenAiStructuredRequest request =
        new OpenAiStructuredRequest(
            SYSTEM_PROMPT, chapterText, schema, "keyword_matrix", MAX_OUTPUT_TOKENS);

    JsonNode response = openAiClient.requestStructuredJson(request);
    return convertToList(response);
  }

  /**
   * 初回サジェスト用に、章本文なしでキーワードを生成する。
   *
   * <p>毎回 seed を変えて LLM にランダム性を持たせる。
   *
   * @return 生成されたキーワードセット
   */
  public List<List<String>> generateInitialKeywords() {
    String seed = UUID.randomUUID().toString();
    String prompt = INITIAL_USER_PROMPT_TEMPLATE.formatted(seed);
    return generate(prompt);
  }

  private JsonNode buildKeywordObjectSchema() {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("$schema", "http://json-schema.org/draft-07/schema#");
    root.put("type", "object");

    // keywords プロパティに 3 セットのキーワード配列を持つ
    ObjectNode properties = objectMapper.createObjectNode();
    ObjectNode keywordsArray = objectMapper.createObjectNode();
    keywordsArray.put("type", "array");
    keywordsArray.put("additionalItems", false);

    ObjectNode innerArray = objectMapper.createObjectNode();
    innerArray.put("type", "array");
    // 内側には 4 つの文字列を含める
    ObjectNode stringItem = objectMapper.createObjectNode();
    stringItem.put("type", "string");
    innerArray.set("items", stringItem);
    innerArray.put("minItems", 4);
    innerArray.put("maxItems", 4);
    innerArray.put("additionalItems", false);

    keywordsArray.set("items", innerArray);
    keywordsArray.put("minItems", 3);
    keywordsArray.put("maxItems", 3);

    properties.set("keywords", keywordsArray);
    root.set("properties", properties);
    root.set("required", objectMapper.createArrayNode().add("keywords"));
    root.put("additionalProperties", false);

    return root;
  }

  /** OpenAI から返却された JSON を List<List<String>> に変換する。 */
  private List<List<String>> convertToList(JsonNode response) {
    JsonNode keywordsNode = response.path("keywords");
    if (!keywordsNode.isArray()) {
      throw new OpenAiClientException("OpenAI response must contain 'keywords' array");
    }
    List<List<String>> result = new ArrayList<>();
    for (JsonNode group : keywordsNode) {
      if (!group.isArray()) {
        continue;
      }
      List<String> keywords = new ArrayList<>();
      group.forEach(node -> keywords.add(Objects.toString(node.asText(), "")));
      result.add(keywords);
    }
    return result;
  }
}
