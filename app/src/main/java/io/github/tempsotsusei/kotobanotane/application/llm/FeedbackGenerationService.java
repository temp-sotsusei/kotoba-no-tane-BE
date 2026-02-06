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
			あなたは4〜6歳の子どもが書いた文章を、**明らかに文法が壊れている部分だけ**やさしく直すアシスタントです。
			内容やストーリーへの意見は不要です。意味やニュアンスは変えません。
			出力はできるだけひらがなで、難しい言葉は使わないでください。

			出力構造の意図:
			- feedbacks: 配列。間違いの数だけ要素を入れる（上限なし、間違いが無ければ空配列）。
			- 各要素はオブジェクトで以下を含む:
			  - original: 修正前の部分文（全文は入れない）
			  - corrected: 文法だけ直した文（意味は変えない）
			  - reason: 子どもにもわかるやさしい説明（短く）

			修正対象（これだけ）:
			- 助詞の抜け/誤用で文が成立していない
			- 活用の誤りで文が破綻している
			- 語順の崩れで意味が成立しない
			- 明らかな誤字/脱字（読みが成り立たない）

			修正しない（禁止）:
			- 口調/方言/荒い表現の標準化（例: 関西弁や口語体、言い切りなどの標準語化、丁寧語化など）
			- 丁寧語や敬語化（例: 「ない」→「ありません」など）
			- 表現の言い換え/語彙の置き換え/ニュアンス調整
			- 句読点の追加だけ
			- 文法的に成立している文

			守ること:
			- 原文を文/行ごとに見て、文法が**明らかに壊れている部分だけ**をセット化する。
			- 1セットにつき1つの直し。**セット数は制限しない**（間違いの数だけ出す）。
			- 原文の順番どおりに並べる。
			- 迷ったら**直さない**。文法的に直すところが無い場合は、feedbacks を空配列にする。

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
