package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** thumbnails テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "thumbnails")
public class ThumbnailEntity {

  /** サムネイルを一意に識別するカラム。 */
  @Id
  @Column(name = "thumbnail_id", length = 255, nullable = false)
  private String thumbnailId;

  /** 画像ファイルの保存先パス。 */
  @Column(name = "thumbnail_path", length = 255, nullable = false)
  private String thumbnailPath;

  /** レコード作成日時。 */
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** レコード更新日時。 */
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA がリフレクションで利用するためのデフォルトコンストラクタ。 */
  protected ThumbnailEntity() {}

  public ThumbnailEntity(
      String thumbnailId, String thumbnailPath, Instant createdAt, Instant updatedAt) {
    this.thumbnailId = thumbnailId;
    this.thumbnailPath = thumbnailPath;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getThumbnailId() {
    return thumbnailId;
  }

  public String getThumbnailPath() {
    return thumbnailPath;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   * サムネイルパスと更新日時を変更する。
   *
   * @param newPath 変更後のパス
   * @param newUpdatedAt 変更後の更新日時
   */
  public void updatePath(String newPath, Instant newUpdatedAt) {
    this.thumbnailPath = newPath;
    this.updatedAt = newUpdatedAt;
  }
}
