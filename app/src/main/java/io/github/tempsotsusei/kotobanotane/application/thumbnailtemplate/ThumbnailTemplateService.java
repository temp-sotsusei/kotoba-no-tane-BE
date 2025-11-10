package io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate;

import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplateRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/** thumbnail_templates テーブルへの CRUD を担うアプリケーションサービス。 */
@Service
public class ThumbnailTemplateService {

  private final ThumbnailTemplateRepository thumbnailTemplateRepository;
  private final ThumbnailRepository thumbnailRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;

  public ThumbnailTemplateService(
      ThumbnailTemplateRepository thumbnailTemplateRepository,
      ThumbnailRepository thumbnailRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider) {
    this.thumbnailTemplateRepository = thumbnailTemplateRepository;
    this.thumbnailRepository = thumbnailRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
  }

  /** すべてのテンプレートを取得する。 */
  public List<ThumbnailTemplate> findAll() {
    return thumbnailTemplateRepository.findAll();
  }

  /** テンプレート ID で取得する。 */
  public Optional<ThumbnailTemplate> findById(String templateId) {
    return thumbnailTemplateRepository.findById(templateId);
  }

  /**
   * テンプレートを新規作成する。
   *
   * @param thumbnailId 紐付けるサムネイル ID
   * @return 作成したテンプレート
   */
  @Transactional
  public ThumbnailTemplate create(String thumbnailId) {
    ensureThumbnailExists(thumbnailId);
    Instant now = timeProvider.nowInstant();
    ThumbnailTemplate template =
        new ThumbnailTemplate(uuidGeneratorService.generateV7(), thumbnailId, now, now);
    return thumbnailTemplateRepository.save(template);
  }

  /**
   * テンプレートを更新する。
   *
   * @param templateId テンプレート ID
   * @param command 更新コマンド
   * @return 更新後テンプレート（存在しない場合は空）
   */
  @Transactional
  public Optional<ThumbnailTemplate> update(
      String templateId, ThumbnailTemplateUpdateCommand command) {
    return thumbnailTemplateRepository
        .findById(templateId)
        .map(existing -> applyUpdate(existing, command))
        .map(thumbnailTemplateRepository::save);
  }

  /** テンプレートを削除する。 */
  @Transactional
  public void delete(String templateId) {
    thumbnailTemplateRepository.deleteById(templateId);
  }

  private ThumbnailTemplate applyUpdate(
      ThumbnailTemplate existing, ThumbnailTemplateUpdateCommand command) {
    String nextThumbnailId = existing.thumbnailId();
    if (command.thumbnailIdSpecified()) {
      String candidate = command.thumbnailId();
      if (!StringUtils.hasText(candidate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "thumbnailId must not be blank");
      }
      ensureThumbnailExists(candidate);
      nextThumbnailId = candidate;
    }

    Instant updatedAt = timeProvider.nowInstant();
    return new ThumbnailTemplate(
        existing.thumbnailTemplateId(), nextThumbnailId, existing.createdAt(), updatedAt);
  }

  private void ensureThumbnailExists(String thumbnailId) {
    boolean exists = thumbnailRepository.findById(thumbnailId).isPresent();
    if (!exists) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "thumbnailId does not exist: " + thumbnailId);
    }
  }
}
