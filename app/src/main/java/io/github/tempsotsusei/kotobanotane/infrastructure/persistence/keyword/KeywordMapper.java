package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.keyword;

import io.github.tempsotsusei.kotobanotane.domain.keyword.Keyword;
import java.time.Instant;

/** keyword ドメイン ↔ エンティティ変換を行うユーティリティ。 */
public final class KeywordMapper {

  private KeywordMapper() {}

  public static Keyword toDomain(KeywordEntity entity) {
    return new Keyword(
        entity.getKeywordId(),
        entity.getChapterId(),
        entity.getKeyword(),
        entity.getKeywordPosition(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public static KeywordEntity toEntity(Keyword keyword) {
    return new KeywordEntity(
        keyword.keywordId(),
        keyword.chapterId(),
        keyword.keyword(),
        keyword.keywordPosition(),
        keyword.createdAt(),
        keyword.updatedAt());
  }

  public static KeywordEntity toEntityForUpdate(
      KeywordEntity entity, String chapterId, String keywordValue, int keywordPosition, Instant updatedAt) {
    entity.updateDetails(chapterId, keywordValue, keywordPosition, updatedAt);
    return entity;
  }
}
