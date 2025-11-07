package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story;

import io.github.tempsotsusei.kotobanotane.domain.story.Story;

/** ストーリーのドメインモデルとエンティティを相互変換するユーティリティ。 */
public final class StoryMapper {

  private StoryMapper() {}

  public static Story toDomain(StoryEntity entity) {
    return new Story(
        entity.getStoryId(),
        entity.getAuth0UserId(),
        entity.getStoryTitle(),
        entity.getThumbnailId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static StoryEntity toEntity(Story story) {
    return new StoryEntity(
        story.storyId(),
        story.auth0UserId(),
        story.storyTitle(),
        story.thumbnailId(),
        story.createdAt(),
        story.updatedAt());
  }

  public static StoryEntity toEntityForUpdate(
      StoryEntity entity, String storyTitle, String thumbnailId, java.time.Instant updatedAt) {
    entity.updateDetails(storyTitle, thumbnailId, updatedAt);
    return entity;
  }
}
