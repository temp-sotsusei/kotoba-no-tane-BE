package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import io.github.tempsotsusei.kotobanotane.application.llm.OpenAiClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 開発用 API の例外ハンドラ。 */
@RestControllerAdvice(assignableTypes = DevKeywordListsController.class)
@Profile({"dev", "test"})
public class DevApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(DevApiExceptionHandler.class);

  @ExceptionHandler(OpenAiClientException.class)
  public ResponseEntity<ErrorResponse> handleOpenAiClientException(OpenAiClientException ex) {
    log.warn("OpenAI client error: {}", ex.getMessage(), ex);
    ErrorResponse body =
        new ErrorResponse("LLM サービスの応答に遅延が発生しています。時間を置いて再実行してください。", "LLM-001", true);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
  }

  public record ErrorResponse(String message, String errorCode, boolean retriable) {}
}
