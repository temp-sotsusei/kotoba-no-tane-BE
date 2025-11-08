package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import java.time.Instant;

/** chapter のドメイン ↔ エンティティを変換するユーティリティ。 */
public final class ChapterMapper {

  private ChapterMapper() {}

  public static Chapter toDomain(ChapterEntity entity) {
    return new Chapter(
        entity.getChapterId(),
        entity.getStoryId(),
        entity.getChapterNum(),
        entity.getChapterText(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static ChapterEntity toEntity(Chapter chapter) {
    return new ChapterEntity(
        chapter.chapterId(),
        chapter.storyId(),
        chapter.chapterNum(),
        chapter.chapterText(),
        chapter.createdAt(),
        chapter.updatedAt());
  }

  public static ChapterEntity toEntityForUpdate(
      ChapterEntity entity, String storyId, int chapterNum, String chapterText, Instant updatedAt) {
    entity.updateDetails(storyId, chapterNum, chapterText, updatedAt);
    return entity;
  }
}
