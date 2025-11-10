package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnailtemplate;

import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import java.time.Instant;

/** thumbnail template のドメイン ↔ エンティティ変換を行うユーティリティ。 */
public final class ThumbnailTemplateMapper {

  private ThumbnailTemplateMapper() {}

  public static ThumbnailTemplate toDomain(ThumbnailTemplateEntity entity) {
    return new ThumbnailTemplate(
        entity.getThumbnailTemplateId(),
        entity.getThumbnailId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static ThumbnailTemplateEntity toEntity(ThumbnailTemplate template) {
    return new ThumbnailTemplateEntity(
        template.thumbnailTemplateId(),
        template.thumbnailId(),
        template.createdAt(),
        template.updatedAt());
  }

  public static ThumbnailTemplateEntity toEntityForUpdate(
      ThumbnailTemplateEntity entity, String thumbnailId, Instant updatedAt) {
    entity.updateThumbnailId(thumbnailId, updatedAt);
    return entity;
  }
}
