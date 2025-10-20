package io.github.tempsotsusei.kotobanotane.application.llm;

/** OpenAI 連携で異常が発生した場合に送出する例外。 */
public class OpenAiClientException extends RuntimeException {

  public OpenAiClientException(String message) {
    super(message);
  }

  public OpenAiClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
