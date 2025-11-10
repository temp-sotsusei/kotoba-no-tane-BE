package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnailtemplate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** thumbnail_templates テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "thumbnail_templates")
public class ThumbnailTemplateEntity {

  /** テンプレート ID。 */
  @Id
  @Column(name = "thumbnail_template_id", length = 255, nullable = false)
  private String thumbnailTemplateId;

  /** 紐付くサムネイル ID。 */
  @Column(name = "thumbnail_id", length = 255, nullable = false)
  private String thumbnailId;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA が利用するデフォルトコンストラクタ。 */
  protected ThumbnailTemplateEntity() {}

  public ThumbnailTemplateEntity(
      String thumbnailTemplateId, String thumbnailId, Instant createdAt, Instant updatedAt) {
    this.thumbnailTemplateId = thumbnailTemplateId;
    this.thumbnailId = thumbnailId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getThumbnailTemplateId() {
    return thumbnailTemplateId;
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
   * サムネイル ID を更新し、更新日時を反映する。
   *
   * @param newThumbnailId 更新後のサムネイル ID
   * @param newUpdatedAt 更新日時
   */
  public void updateThumbnailId(String newThumbnailId, Instant newUpdatedAt) {
    this.thumbnailId = newThumbnailId;
    this.updatedAt = newUpdatedAt;
  }
}
