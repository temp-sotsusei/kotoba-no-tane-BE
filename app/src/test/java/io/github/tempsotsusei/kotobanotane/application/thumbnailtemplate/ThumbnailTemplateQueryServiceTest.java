package io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplateRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ThumbnailTemplateQueryServiceTest {

  private final ThumbnailTemplateRepository thumbnailTemplateRepository =
      Mockito.mock(ThumbnailTemplateRepository.class);
  private final ThumbnailRepository thumbnailRepository = Mockito.mock(ThumbnailRepository.class);

  private ThumbnailTemplateQueryService service;

  @BeforeEach
  void setUp() {
    service = new ThumbnailTemplateQueryService(thumbnailTemplateRepository, thumbnailRepository);
  }

  /** テンプレートに紐づくサムネイルのみが createdAt 降順で返ることを確認する。 */
  @Test
  void listTemplatesReturnsOnlyTemplateThumbnailsInOrder() {
    ThumbnailTemplate newerTemplate =
        new ThumbnailTemplate("tmpl-2", "thumb-2", Instant.parse("2025-01-02T00:00:00Z"), null);
    ThumbnailTemplate olderTemplate =
        new ThumbnailTemplate("tmpl-1", "thumb-1", Instant.parse("2025-01-01T00:00:00Z"), null);
    when(thumbnailTemplateRepository.findAll()).thenReturn(List.of(olderTemplate, newerTemplate));

    Thumbnail thumb1 = new Thumbnail("thumb-1", "/path/1.png", null, null);
    Thumbnail thumb2 = new Thumbnail("thumb-2", "/path/2.png", null, null);
    when(thumbnailRepository.findAllByIds(List.of("thumb-1", "thumb-2")))
        .thenReturn(List.of(thumb1, thumb2));

    List<ThumbnailTemplateSummary> summaries = service.listTemplates();

    assertThat(summaries).hasSize(2);
    assertThat(summaries.get(0))
        .extracting(ThumbnailTemplateSummary::thumbnailId, ThumbnailTemplateSummary::thumbnailPath)
        .containsExactly("thumb-2", "/path/2.png");
    assertThat(summaries.get(1))
        .extracting(ThumbnailTemplateSummary::thumbnailId, ThumbnailTemplateSummary::thumbnailPath)
        .containsExactly("thumb-1", "/path/1.png");
  }

  /** テンプレートに紐づくサムネイルが存在しない場合はスキップされることを検証する。 */
  @Test
  void listTemplatesSkipsMissingThumbnails() {
    ThumbnailTemplate template =
        new ThumbnailTemplate("tmpl-1", "missing-thumb", Instant.now(), null);
    when(thumbnailTemplateRepository.findAll()).thenReturn(List.of(template));
    when(thumbnailRepository.findAllByIds(List.of("missing-thumb"))).thenReturn(List.of());

    List<ThumbnailTemplateSummary> summaries = service.listTemplates();

    assertThat(summaries).isEmpty();
  }
}
