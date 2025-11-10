package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnailtemplate;

import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** ThumbnailTemplateRepository を JPA で実現する実装クラス。 */
@Repository
@Transactional(readOnly = true)
public class ThumbnailTemplateRepositoryImpl implements ThumbnailTemplateRepository {

  private final ThumbnailTemplateJpaRepository thumbnailTemplateJpaRepository;

  public ThumbnailTemplateRepositoryImpl(
      ThumbnailTemplateJpaRepository thumbnailTemplateJpaRepository) {
    this.thumbnailTemplateJpaRepository = thumbnailTemplateJpaRepository;
  }

  @Override
  public Optional<ThumbnailTemplate> findById(String thumbnailTemplateId) {
    return thumbnailTemplateJpaRepository
        .findById(thumbnailTemplateId)
        .map(ThumbnailTemplateMapper::toDomain);
  }

  @Override
  public List<ThumbnailTemplate> findAll() {
    return thumbnailTemplateJpaRepository.findAll().stream()
        .map(ThumbnailTemplateMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public ThumbnailTemplate save(ThumbnailTemplate template) {
    ThumbnailTemplateEntity entity =
        thumbnailTemplateJpaRepository
            .findById(template.thumbnailTemplateId())
            .map(
                existing ->
                    ThumbnailTemplateMapper.toEntityForUpdate(
                        existing, template.thumbnailId(), template.updatedAt()))
            .orElse(ThumbnailTemplateMapper.toEntity(template));

    ThumbnailTemplateEntity saved = thumbnailTemplateJpaRepository.save(entity);
    return ThumbnailTemplateMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String thumbnailTemplateId) {
    thumbnailTemplateJpaRepository.deleteById(thumbnailTemplateId);
  }
}
