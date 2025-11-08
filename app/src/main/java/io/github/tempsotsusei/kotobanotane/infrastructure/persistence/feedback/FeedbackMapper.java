package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.feedback;

import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import java.time.Instant;

/** feedback ドメイン ↔ エンティティ変換を行うユーティリティ。 */
public final class FeedbackMapper {

  private FeedbackMapper() {}

  public static Feedback toDomain(FeedbackEntity entity) {
    return new Feedback(
        entity.getFeedbackId(),
        entity.getChapterId(),
        entity.getFeedback(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static FeedbackEntity toEntity(Feedback feedback) {
    return new FeedbackEntity(
        feedback.feedbackId(),
        feedback.chapterId(),
        feedback.feedback(),
        feedback.createdAt(),
        feedback.updatedAt());
  }

  public static FeedbackEntity toEntityForUpdate(
      FeedbackEntity entity, String chapterId, String feedback, Instant updatedAt) {
    entity.updateDetails(chapterId, feedback, updatedAt);
    return entity;
  }
}
