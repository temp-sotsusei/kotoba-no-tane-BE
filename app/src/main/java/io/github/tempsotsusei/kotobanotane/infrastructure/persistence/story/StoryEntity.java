package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** stories テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "stories")
public class StoryEntity {

  /** ストーリーを一意に識別するカラム。 */
  @Id
  @Column(name = "story_id", length = 255, nullable = false)
  private String storyId;

  /** ストーリー作成者の Auth0 ID。 */
  @Column(name = "auth0_user_id", length = 255, nullable = false)
  private String auth0UserId;

  /** ストーリータイトル。 */
  @Column(name = "story_title", length = 255, nullable = false)
  private String storyTitle;

  /** サムネイル ID（任意）。 */
  @Column(name = "thumbnail_id", length = 255)
  private String thumbnailId;

  /** レコード作成日時。 */
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** レコード更新日時。 */
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA が利用するデフォルトコンストラクタ。 */
  protected StoryEntity() {}

  public StoryEntity(
      String storyId,
      String auth0UserId,
      String storyTitle,
      String thumbnailId,
      Instant createdAt,
      Instant updatedAt) {
    this.storyId = storyId;
    this.auth0UserId = auth0UserId;
    this.storyTitle = storyTitle;
    this.thumbnailId = thumbnailId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getStoryId() {
    return storyId;
  }

  public String getAuth0UserId() {
    return auth0UserId;
  }

  public String getStoryTitle() {
    return storyTitle;
  }

  public String getThumbnailId() {
    return thumbnailId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   * タイトルとサムネイル ID を変更し、更新日時を反映させる。
   *
   * @param newTitle 更新後のタイトル
   * @param newThumbnailId 更新後のサムネイル ID
   * @param newUpdatedAt 更新日時
   */
  public void updateDetails(String newTitle, String newThumbnailId, Instant newUpdatedAt) {
    this.storyTitle = newTitle;
    this.thumbnailId = newThumbnailId;
    this.updatedAt = newUpdatedAt;
  }
}
