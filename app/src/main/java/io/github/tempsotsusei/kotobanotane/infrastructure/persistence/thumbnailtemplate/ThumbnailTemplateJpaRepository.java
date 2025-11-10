package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnailtemplate;

import org.springframework.data.jpa.repository.JpaRepository;

/** thumbnail_templates テーブルへアクセスする Spring Data JPA リポジトリ。 */
public interface ThumbnailTemplateJpaRepository
    extends JpaRepository<ThumbnailTemplateEntity, String> {}
