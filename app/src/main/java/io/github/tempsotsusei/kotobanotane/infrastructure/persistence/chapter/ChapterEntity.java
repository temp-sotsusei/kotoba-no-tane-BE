package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** chapters テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "chapters")
public class ChapterEntity {

  /** 章 ID。 */
  @Id
  @Column(name = "chapter_id", length = 255, nullable = false)
  private String chapterId;

  /** 紐付く story ID。 */
  @Column(name = "story_id", length = 255, nullable = false)
  private String storyId;

  /** 章番号。 */
  @Column(name = "chapter_num", nullable = false)
  private int chapterNum;

  /** 章本文。 */
  @Column(name = "chapter_text", nullable = false, columnDefinition = "TEXT")
  private String chapterText;

  /** 作成日時。 */
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** 更新日時。 */
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA が利用するデフォルトコンストラクタ。 */
  protected ChapterEntity() {}

  public ChapterEntity(
      String chapterId,
      String storyId,
      int chapterNum,
      String chapterText,
      Instant createdAt,
      Instant updatedAt) {
    this.chapterId = chapterId;
    this.storyId = storyId;
    this.chapterNum = chapterNum;
    this.chapterText = chapterText;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getChapterId() {
    return chapterId;
  }

  public String getStoryId() {
    return storyId;
  }

  public int getChapterNum() {
    return chapterNum;
  }

  public String getChapterText() {
    return chapterText;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   * story ID / 章番号 / 本文 を更新し、更新日時を反映する。
   *
   * @param newStoryId 更新後の story ID
   * @param newChapterNum 更新後の章番号
   * @param newChapterText 更新後の本文
   * @param newUpdatedAt 更新日時
   */
  public void updateDetails(
      String newStoryId, int newChapterNum, String newChapterText, Instant newUpdatedAt) {
    this.storyId = newStoryId;
    this.chapterNum = newChapterNum;
    this.chapterText = newChapterText;
    this.updatedAt = newUpdatedAt;
  }
}
