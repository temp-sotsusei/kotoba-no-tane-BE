package io.github.tempsotsusei.kotobanotane.application.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterService;
import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class StoryCreationServiceTest {

  private final StoryRepository storyRepository = Mockito.mock(StoryRepository.class);
  private final ChapterService chapterService = Mockito.mock(ChapterService.class);
  private final ThumbnailRepository thumbnailRepository = Mockito.mock(ThumbnailRepository.class);
  private final UuidGeneratorService uuidGeneratorService =
      Mockito.mock(UuidGeneratorService.class);
  private final TimeProvider timeProvider = Mockito.mock(TimeProvider.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  private StoryCreationService service;

  @BeforeEach
  void setUp() {
    service =
        new StoryCreationService(
            storyRepository,
            chapterService,
            thumbnailRepository,
            uuidGeneratorService,
            timeProvider);
  }

  /** Story と複数章が保存され、フィードバック対象が生成される正常系を検証する。 */
  @Test
  void createStoryWithChaptersSavesAllAndReturnsTargets() throws Exception {
    Instant now = Instant.parse("2025-01-01T00:00:00Z");
    when(timeProvider.nowInstant()).thenReturn(now);
    when(uuidGeneratorService.generateV7()).thenReturn("story-1");
    when(thumbnailRepository.findById("thumb-1"))
        .thenReturn(
            java.util.Optional.of(
                new io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail(
                    "thumb-1", "/path", now, now)));
    when(storyRepository.save(Mockito.any(Story.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Story.class));

    Chapter chapter1 = new Chapter("chap-1", "story-1", 1, objectMapper.readTree("{}"), now, now);
    Chapter chapter2 = new Chapter("chap-2", "story-1", 2, objectMapper.readTree("{}"), now, now);
    when(chapterService.create("story-1", 1, chapter1.chapterJson())).thenReturn(chapter1);
    when(chapterService.create("story-1", 2, chapter2.chapterJson())).thenReturn(chapter2);

    List<ChapterDraft> drafts =
        List.of(
            new ChapterDraft(2, chapter2.chapterJson(), "second"),
            new ChapterDraft(1, chapter1.chapterJson(), "first"));

    StoryCreationResult result =
        service.createStoryWithChapters("auth0|user", "物語タイトル", "thumb-1", drafts);

    assertThat(result.storyId()).isEqualTo("story-1");
    assertThat(result.feedbackTargets()).hasSize(2);
    assertThat(result.feedbackTargets().get(0).chapterId()).isEqualTo("chap-1");
    assertThat(result.feedbackTargets().get(1).chapterId()).isEqualTo("chap-2");
  }

  /** タイトルが16文字以上の場合に 400 となることを検証する。 */
  @Test
  void createStoryWithChaptersRejectsTooLongTitle() {
    List<ChapterDraft> drafts =
        List.of(new ChapterDraft(1, objectMapper.createObjectNode(), "text"));

    assertThatThrownBy(
            () -> service.createStoryWithChapters("auth0|user", "abcdefghijklmnop", null, drafts))
        .isInstanceOf(ResponseStatusException.class);
  }

  /** 章が 6 件以上の場合に 400 となることを検証する。 */
  @Test
  void createStoryWithChaptersRejectsTooManyChapters() {
    List<ChapterDraft> drafts =
        List.of(
            new ChapterDraft(1, objectMapper.createObjectNode(), "a"),
            new ChapterDraft(2, objectMapper.createObjectNode(), "b"),
            new ChapterDraft(3, objectMapper.createObjectNode(), "c"),
            new ChapterDraft(4, objectMapper.createObjectNode(), "d"),
            new ChapterDraft(5, objectMapper.createObjectNode(), "e"),
            new ChapterDraft(6, objectMapper.createObjectNode(), "f"));

    assertThatThrownBy(() -> service.createStoryWithChapters("auth0|user", "タイトル", null, drafts))
        .isInstanceOf(ResponseStatusException.class);
  }

  /** 存在しないサムネイルを指定した場合に 400 となることを検証する。 */
  @Test
  void createStoryWithChaptersRejectsMissingThumbnail() {
    List<ChapterDraft> drafts =
        List.of(new ChapterDraft(1, objectMapper.createObjectNode(), "text"));
    when(thumbnailRepository.findById("missing")).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(
            () -> service.createStoryWithChapters("auth0|user", "タイトル", "missing", drafts))
        .isInstanceOf(ResponseStatusException.class);
  }
}
