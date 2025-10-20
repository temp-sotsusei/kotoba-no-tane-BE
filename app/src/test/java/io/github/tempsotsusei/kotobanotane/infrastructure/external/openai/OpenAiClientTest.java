package io.github.tempsotsusei.kotobanotane.infrastructure.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.application.llm.OpenAiStructuredRequest;
import io.github.tempsotsusei.kotobanotane.config.OpenAiProperties;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class OpenAiClientTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockWebServer mockWebServer;
  private OpenAiClient client;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    OpenAiProperties properties =
        new OpenAiProperties(
            mockWebServer.url("/").toString(), "dummy-key", "gpt-test", 10, 1000, 5);
    client = new OpenAiClient(WebClient.builder(), objectMapper, properties);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void extractsJsonFromResponse() throws Exception {
    // MockWebServer で擬似レスポンスを返し、JSON 部分を正しく抽出できることを確認する。
    ObjectNode json = objectMapper.createObjectNode();
    json.put("field", "value");
    ObjectNode content = objectMapper.createObjectNode();
    content.put("type", "output_json_schema");
    content.set("json", json);
    ObjectNode outputItem = objectMapper.createObjectNode();
    outputItem.set("content", objectMapper.createArrayNode().add(content));
    ObjectNode root = objectMapper.createObjectNode();
    root.set("output", objectMapper.createArrayNode().add(outputItem));

    mockWebServer.enqueue(
        new MockResponse().setHeader("Content-Type", "application/json").setBody(root.toString()));

    JsonNode result =
        client.requestStructuredJson(
            new OpenAiStructuredRequest(
                "system", "user", objectMapper.createObjectNode(), "schema_name", null));

    assertThat(result.path("field").asText()).isEqualTo("value");
  }
}
