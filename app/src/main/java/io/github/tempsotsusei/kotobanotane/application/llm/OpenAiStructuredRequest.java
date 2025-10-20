package io.github.tempsotsusei.kotobanotane.application.llm;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;

/** OpenAI Responses API へのリクエスト情報。 */
public record OpenAiStructuredRequest(
    String systemPrompt,
    String userInput,
    JsonNode schema,
    String schemaName,
    Integer maxOutputTokens) {

  public OpenAiStructuredRequest {
    if (systemPrompt == null || systemPrompt.isBlank()) {
      throw new IllegalArgumentException("systemPrompt must not be blank");
    }
    if (userInput == null || userInput.isBlank()) {
      throw new IllegalArgumentException("userInput must not be blank");
    }
    if (schema == null) {
      throw new IllegalArgumentException("schema must not be null");
    }
    if (maxOutputTokens != null && maxOutputTokens <= 0) {
      throw new IllegalArgumentException("maxOutputTokens must be positive");
    }
  }

  public Optional<String> optionalSchemaName() {
    return Optional.ofNullable(schemaName);
  }

  public Optional<Integer> optionalMaxOutputTokens() {
    return Optional.ofNullable(maxOutputTokens);
  }
}
