package io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate;

import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplateRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * テンプレート用のサムネイルだけを取得する読み取り専用サービス。
 *
 * <p>thumbnail_templates に存在する thumbnail_id を起点に、thumbnails の id/path を返却する。 並び順はテンプレートの createdAt
 * 降順で固定。
 */
@Service
@Transactional(readOnly = true)
public class ThumbnailTemplateQueryService {

  private final ThumbnailTemplateRepository thumbnailTemplateRepository;
  private final ThumbnailRepository thumbnailRepository;

  public ThumbnailTemplateQueryService(
      ThumbnailTemplateRepository thumbnailTemplateRepository,
      ThumbnailRepository thumbnailRepository) {
    this.thumbnailTemplateRepository = thumbnailTemplateRepository;
    this.thumbnailRepository = thumbnailRepository;
  }

  /**
   * テンプレートに紐づくサムネイルを取得する。
   *
   * <p>テンプレートに存在しないサムネイル ID は無視し、取得できたものだけを返す。
   *
   * @return thumbnailId と thumbnailPath の一覧（createdAt 降順）
   */
  public List<ThumbnailTemplateSummary> listTemplates() {
    List<ThumbnailTemplate> templates = thumbnailTemplateRepository.findAll();
    Map<String, Thumbnail> thumbnailMap = toThumbnailMap(templates);

    return templates.stream()
        .sorted(Comparator.comparing(ThumbnailTemplate::createdAt).reversed())
        .map(template -> toSummary(template, thumbnailMap.get(template.thumbnailId())))
        .filter(TemplateConversionResult::isPresent)
        .map(TemplateConversionResult::summary)
        .toList();
  }

  private Map<String, Thumbnail> toThumbnailMap(List<ThumbnailTemplate> templates) {
    List<String> thumbnailIds =
        templates.stream().map(ThumbnailTemplate::thumbnailId).distinct().toList();
    return thumbnailRepository.findAllByIds(thumbnailIds).stream()
        .collect(Collectors.toMap(Thumbnail::thumbnailId, Function.identity()));
  }

  private TemplateConversionResult toSummary(
      ThumbnailTemplate template, Thumbnail thumbnailOrNull) {
    if (thumbnailOrNull == null) {
      // テンプレートに紐づくサムネイルが存在しない場合はスキップ
      return TemplateConversionResult.empty();
    }
    ThumbnailTemplateSummary summary =
        new ThumbnailTemplateSummary(
            thumbnailOrNull.thumbnailId(), thumbnailOrNull.thumbnailPath());
    return TemplateConversionResult.of(summary);
  }

  /** Optional の代わりに small DTO で存在判定を行う。 */
  private record TemplateConversionResult(ThumbnailTemplateSummary summary, boolean isPresent) {
    static TemplateConversionResult of(ThumbnailTemplateSummary summary) {
      return new TemplateConversionResult(summary, true);
    }

    static TemplateConversionResult empty() {
      return new TemplateConversionResult(null, false);
    }
  }
}
