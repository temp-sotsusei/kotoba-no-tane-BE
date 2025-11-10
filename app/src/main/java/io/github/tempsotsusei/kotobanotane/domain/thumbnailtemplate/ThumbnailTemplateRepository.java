package io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate;

import java.util.List;
import java.util.Optional;

/** thumbnail_templates テーブルへアクセスするリポジトリ。 */
public interface ThumbnailTemplateRepository {

  Optional<ThumbnailTemplate> findById(String thumbnailTemplateId);

  List<ThumbnailTemplate> findAll();

  ThumbnailTemplate save(ThumbnailTemplate template);

  void deleteById(String thumbnailTemplateId);
}
