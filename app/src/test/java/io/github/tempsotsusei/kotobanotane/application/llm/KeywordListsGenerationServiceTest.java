package io.github.tempsotsusei.kotobanotane.application.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.infrastructure.external.openai.OpenAiClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class KeywordListsGenerationServiceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OpenAiClient openAiClient = Mockito.mock(OpenAiClient.class);
  private KeywordListsGenerationService service;

  @BeforeEach
  void setUp() {
    service = new KeywordListsGenerationService(openAiClient, objectMapper);
  }

  @Test
  void generateReturnsLists() {
    // OpenAI クライアントから想定される JSON が返ってきた場合に、リストへ変換できるか検証する。
    ObjectNode response = objectMapper.createObjectNode();
    ArrayNode keywords = objectMapper.createArrayNode();
    keywords.add(arrayOf("りんご", "ばなな", "くるま", "ねこ"));
    keywords.add(arrayOf("いぬ", "とり", "つくえ", "そら"));
    keywords.add(arrayOf("みかん", "もも", "ぼうし", "くつ"));
    response.set("keywords", keywords);

    when(openAiClient.requestStructuredJson(any(OpenAiStructuredRequest.class)))
        .thenReturn(response);

    List<List<String>> lists = service.generate("テスト文章");

    assertThat(lists).hasSize(3).allSatisfy(list -> assertThat(list).hasSize(4));
    assertThat(lists.get(0)).containsExactly("りんご", "ばなな", "くるま", "ねこ");
  }

  @Test
  void generateThrowsForBlankInput() {
    // 入力が空文字の場合に例外が送出されることを確認する。
    assertThatThrownBy(() -> service.generate(" ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void generateInitialKeywordsUsesRandomizedPrompt() {
    ObjectNode response = objectMapper.createObjectNode();
    ArrayNode keywords = objectMapper.createArrayNode();
    keywords.add(arrayOf("あお", "あか", "きいろ", "みどり"));
    keywords.add(arrayOf("そら", "うみ", "ほし", "つき"));
    keywords.add(arrayOf("はる", "なつ", "あき", "ふゆ"));
    response.set("keywords", keywords);

    when(openAiClient.requestStructuredJson(any(OpenAiStructuredRequest.class)))
        .thenReturn(response);

    List<List<String>> lists = service.generateInitialKeywords();

    assertThat(lists).hasSize(3);
    ArgumentCaptor<OpenAiStructuredRequest> captor =
        ArgumentCaptor.forClass(OpenAiStructuredRequest.class);
    verify(openAiClient).requestStructuredJson(captor.capture());
    assertThat(captor.getValue().userInput()).contains("seed=");
  }

  private JsonNode arrayOf(String... values) {
    ArrayNode node = objectMapper.createArrayNode();
    for (String value : values) {
      node.add(value);
    }
    return node;
  }
}
