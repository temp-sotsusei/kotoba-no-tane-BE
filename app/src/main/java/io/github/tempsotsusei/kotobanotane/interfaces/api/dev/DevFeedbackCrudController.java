package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tempsotsusei.kotobanotane.application.feedback.FeedbackService;
import io.github.tempsotsusei.kotobanotane.application.feedback.FeedbackUpdateCommand;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * フィードバック(feedbacks)向けの開発用 CRUD API を提供するコントローラ。
 *
 * <p>dev プロファイルで有効化し、Postman や curl による検証を容易にする。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevFeedbackCrudController {

  private final FeedbackService feedbackService;
  private final TimeProvider timeProvider;

  public DevFeedbackCrudController(FeedbackService feedbackService, TimeProvider timeProvider) {
    this.feedbackService = feedbackService;
    this.timeProvider = timeProvider;
  }

  /** フィードバック一覧を返す。 */
  @GetMapping("/feedbacks")
  @PreAuthorize("permitAll()")
  public List<FeedbackResponse> list() {
    return feedbackService.findAll().stream().map(this::toResponse).toList();
  }

  /** 指定したフィードバックを取得する。 */
  @GetMapping("/feedback/{feedbackId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<FeedbackResponse> get(@PathVariable String feedbackId) {
    return feedbackService
        .findById(feedbackId)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** フィードバックを新規作成する。 */
  @PostMapping("/feedback")
  @PreAuthorize("permitAll()")
  public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackCreateRequest request) {
    Feedback created = feedbackService.create(request.chapterId(), request.feedback());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  /** フィードバックを更新する。 */
  @PutMapping("/feedback/{feedbackId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<FeedbackResponse> update(
      @PathVariable String feedbackId, @Valid @RequestBody FeedbackUpdateRequest request) {
    if (request.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    FeedbackUpdateCommand command =
        new FeedbackUpdateCommand(
            request.chapterIdSpecified(), request.chapterId(), request.feedbackSpecified(), request.feedback());

    return feedbackService
        .update(feedbackId, command)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** フィードバックを削除する。 */
  @DeleteMapping("/feedback/{feedbackId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String feedbackId) {
    feedbackService.delete(feedbackId);
    return ResponseEntity.noContent().build();
  }

  private FeedbackResponse toResponse(Feedback feedback) {
    return new FeedbackResponse(
        feedback.feedbackId(),
        feedback.chapterId(),
        feedback.feedback(),
        timeProvider.formatIso(feedback.createdAt()),
        timeProvider.formatIso(feedback.updatedAt()));
  }

  /** フィードバック作成リクエスト。 */
  public record FeedbackCreateRequest(@NotBlank String chapterId, @NotBlank String feedback) {}

  /** フィードバック更新リクエスト。 */
  public static class FeedbackUpdateRequest {

    private String chapterId;
    private boolean chapterIdSpecified;
    private String feedback;
    private boolean feedbackSpecified;

    @JsonProperty("chapterId")
    public void setChapterId(String chapterId) {
      this.chapterId = chapterId;
      this.chapterIdSpecified = true;
    }

    @JsonProperty("feedback")
    public void setFeedback(String feedback) {
      this.feedback = feedback;
      this.feedbackSpecified = true;
    }

    public String chapterId() {
      return chapterId;
    }

    public String feedback() {
      return feedback;
    }

    public boolean chapterIdSpecified() {
      return chapterIdSpecified;
    }

    public boolean feedbackSpecified() {
      return feedbackSpecified;
    }

    boolean isEmpty() {
      return !chapterIdSpecified && !feedbackSpecified;
    }
  }

  /** フィードバックレスポンス DTO。 */
  public record FeedbackResponse(
      String feedbackId, String chapterId, String feedback, String createdAt, String updatedAt) {}
}
