package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import com.fasterxml.jackson.databind.JsonNode;
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
        entity.getChapterJson(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static ChapterEntity toEntity(Chapter chapter) {
    return new ChapterEntity(
        chapter.chapterId(),
        chapter.storyId(),
        chapter.chapterNum(),
        chapter.chapterJson(),
        chapter.createdAt(),
        chapter.updatedAt());
  }

  public static ChapterEntity toEntityForUpdate(
      ChapterEntity entity,
      String storyId,
      int chapterNum,
      JsonNode chapterJson,
      Instant updatedAt) {
    entity.updateDetails(storyId, chapterNum, chapterJson, updatedAt);
    return entity;
  }
}
