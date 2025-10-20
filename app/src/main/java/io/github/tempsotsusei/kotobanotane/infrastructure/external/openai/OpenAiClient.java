package io.github.tempsotsusei.kotobanotane.infrastructure.external.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.llm.OpenAiClientException;
import io.github.tempsotsusei.kotobanotane.application.llm.OpenAiStructuredRequest;
import io.github.tempsotsusei.kotobanotane.config.OpenAiProperties;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** OpenAI Responses API の呼び出しを担うクライアント。 */
@Component
public class OpenAiClient {

  private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
  private static final long BASE_BACKOFF_MILLIS = 500L;
  private static final long MAX_BACKOFF_MILLIS = 5_000L;

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final OpenAiProperties properties;

  public OpenAiClient(
      WebClient.Builder builder, ObjectMapper objectMapper, OpenAiProperties properties) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.webClient =
        builder
            .baseUrl(properties.openaiBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.openaiApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  /** structured outputs を利用して JSON を取得し、そのままコール元へ返す。 */
  public JsonNode requestStructuredJson(OpenAiStructuredRequest request) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("model", properties.openaiModel());

    List<Map<String, String>> messages =
        List.of(
            Map.of("role", "system", "content", request.systemPrompt()),
            Map.of("role", "user", "content", request.userInput()));
    payload.put("input", messages);

    Map<String, Object> responseFormat = new HashMap<>();
    responseFormat.put("type", "json_schema");
    responseFormat.put("name", request.optionalSchemaName().orElse("structured_output"));
    responseFormat.put("schema", request.schema());

    Map<String, Object> textConfig = new HashMap<>();
    textConfig.put("format", responseFormat);
    payload.put("text", textConfig);

    int resolvedMaxOutputTokens =
        request.optionalMaxOutputTokens().orElse(properties.defaultMaxOutputTokens());
    payload.put("max_output_tokens", resolvedMaxOutputTokens);

    int maxAttempts = Math.max(1, properties.maxAttempts());
    int attempt = 1;
    long startedAt = System.nanoTime();

    while (true) {
      try {
        JsonNode response =
            webClient
                .post()
                .uri("/responses")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(properties.openaiTimeout()));

        if (response == null) {
          throw new OpenAiClientException("OpenAI response was empty");
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.info(
            "OpenAI request succeeded attempts={} elapsedMs={} maxOutputTokens={}",
            attempt,
            elapsedMs,
            resolvedMaxOutputTokens);
        return extractJsonContent(response);
      } catch (WebClientResponseException e) {
        if (isRetryableStatus(e.getStatusCode()) && attempt < maxAttempts) {
          long backoffMillis = calculateBackoffMillis(attempt);
          log.warn(
              "OpenAI API error status={} attempt={}/{} retryingIn={}ms body={}",
              e.getStatusCode(),
              attempt,
              maxAttempts,
              backoffMillis,
              e.getResponseBodyAsString());
          sleep(backoffMillis);
          attempt++;
          continue;
        }
        log.error(
            "OpenAI API error status={} attempts={} body={}",
            e.getStatusCode(),
            attempt,
            e.getResponseBodyAsString());
        throw new OpenAiClientException("OpenAI API returned error: " + e.getStatusCode(), e);
      } catch (Exception e) {
        if (isRetryableException(e) && attempt < maxAttempts) {
          long backoffMillis = calculateBackoffMillis(attempt);
          log.warn(
              "OpenAI call failed attempt={}/{} reason={} retryingIn={}ms",
              attempt,
              maxAttempts,
              e.getMessage(),
              backoffMillis);
          sleep(backoffMillis);
          attempt++;
          continue;
        }
        log.error("OpenAI API call failed attempts={}", attempt, e);
        throw new OpenAiClientException("OpenAI API call failed", e);
      }
    }
  }

  /** OpenAI のレスポンスから JSON 本体を抽出するヘルパーメソッド。 */
  private JsonNode extractJsonContent(JsonNode response) {
    JsonNode output = response.path("output");
    if (!output.isArray() || output.isEmpty()) {
      throw new OpenAiClientException("Unexpected OpenAI response format: missing output array");
    }

    for (JsonNode item : output) {
      JsonNode contentArray = item.path("content");
      if (!contentArray.isArray()) {
        continue;
      }
      for (JsonNode content : contentArray) {
        String type = content.path("type").asText();
        if ("output_json_schema".equals(type)) {
          JsonNode json = content.path("json");
          if (!json.isMissingNode()) {
            return json;
          }
        }
        if ("output_text".equals(type)) {
          String text = content.path("text").asText();
          try {
            return objectMapper.readTree(text);
          } catch (Exception e) {
            throw new OpenAiClientException("Failed to parse textual JSON output", e);
          }
        }
      }
    }

    throw new OpenAiClientException("OpenAI response did not contain JSON output");
  }

  private boolean isRetryableStatus(HttpStatusCode status) {
    int code = status.value();
    return code == 408 || code == 429 || (code >= 500 && code < 600);
  }

  private boolean isRetryableException(Exception exception) {
    if (exception instanceof WebClientRequestException) {
      return true;
    }
    if (exception instanceof TimeoutException) {
      return true;
    }
    Throwable cause = exception.getCause();
    while (cause != null) {
      if (cause instanceof TimeoutException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private long calculateBackoffMillis(int attempt) {
    long multiplier = 1L << Math.max(0, attempt - 1);
    return Math.min(BASE_BACKOFF_MILLIS * multiplier, MAX_BACKOFF_MILLIS);
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OpenAiClientException("OpenAI retry interrupted", ex);
    }
  }
}
