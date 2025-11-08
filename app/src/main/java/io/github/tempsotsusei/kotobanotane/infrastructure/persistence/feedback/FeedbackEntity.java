package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** feedbacks テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "feedbacks")
public class FeedbackEntity {

  /** フィードバック ID。 */
  @Id
  @Column(name = "feedback_id", length = 255, nullable = false)
  private String feedbackId;

  /** 紐付く章 ID。 */
  @Column(name = "chapter_id", length = 255, nullable = false)
  private String chapterId;

  /**
   * フィードバック本文。
   *
   * <p>OID ではなく TEXT を利用したいので columnDefinition を指定している。
   */
  @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
  private String feedback;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA が利用するデフォルトコンストラクタ。 */
  protected FeedbackEntity() {}

  public FeedbackEntity(
      String feedbackId, String chapterId, String feedback, Instant createdAt, Instant updatedAt) {
    this.feedbackId = feedbackId;
    this.chapterId = chapterId;
    this.feedback = feedback;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getFeedbackId() {
    return feedbackId;
  }

  public String getChapterId() {
    return chapterId;
  }

  public String getFeedback() {
    return feedback;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 章 ID / 本文を更新し、更新日時を反映する。
   *
   * @param newChapterId 更新後の章 ID
   * @param newFeedback 更新後の本文
   * @param newUpdatedAt 更新日時
   */
  public void updateDetails(String newChapterId, String newFeedback, Instant newUpdatedAt) {
    this.chapterId = newChapterId;
    this.feedback = newFeedback;
    this.updatedAt = newUpdatedAt;
  }
}
