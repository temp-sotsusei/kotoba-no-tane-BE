package io.github.tempsotsusei.kotobanotane.application.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** CalendarStoriesQueryService のユニットテスト。 */
@ExtendWith(MockitoExtension.class)
class CalendarStoriesQueryServiceTest {

  @Mock private StoryRepository storyRepository;
  @Mock private ThumbnailRepository thumbnailRepository;

  @InjectMocks private CalendarStoriesQueryService calendarStoriesQueryService;

  @Test
  void fetchByAuth0UserIdEnrichesThumbnailPaths() {
    Story storyWithThumbnail =
        new Story(
            "story-1",
            "auth0|user",
            "First Story",
            "thumb-1",
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-01T00:00:00Z"));
    Story storyWithoutThumbnail =
        new Story(
            "story-2",
            "auth0|user",
            "Second Story",
            null,
            Instant.parse("2025-01-02T00:00:00Z"),
            Instant.parse("2025-01-02T00:00:00Z"));

    when(storyRepository.findAllByAuth0UserIdOrderByCreatedAtDesc("auth0|user"))
        .thenReturn(List.of(storyWithThumbnail, storyWithoutThumbnail));
    when(thumbnailRepository.findAllByIds(Set.of("thumb-1")))
        .thenReturn(
            List.of(
                new Thumbnail(
                    "thumb-1",
                    "/thumb/path.png",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"))));

    List<CalendarStorySummary> summaries =
        calendarStoriesQueryService.fetchByAuth0UserId("auth0|user");

    assertThat(summaries).hasSize(2);
    Map<String, CalendarStorySummary> byId =
        summaries.stream()
            .collect(java.util.stream.Collectors.toMap(CalendarStorySummary::storyId, s -> s));
    assertThat(byId.get("story-1").thumbnailPath()).isEqualTo("/thumb/path.png");
    assertThat(byId.get("story-2").thumbnailPath()).isNull();
  }

  @Test
  void fetchByAuth0UserIdSkipsThumbnailLookupWhenNotNeeded() {
    Story story =
        new Story(
            "story-3",
            "auth0|user",
            "Story",
            null,
            Instant.parse("2025-01-03T00:00:00Z"),
            Instant.parse("2025-01-03T00:00:00Z"));

    when(storyRepository.findAllByAuth0UserIdOrderByCreatedAtDesc("auth0|user"))
        .thenReturn(List.of(story));

    List<CalendarStorySummary> summaries =
        calendarStoriesQueryService.fetchByAuth0UserId("auth0|user");

    assertThat(summaries).hasSize(1);
    assertThat(summaries.getFirst().thumbnailPath()).isNull();
    verifyNoInteractions(thumbnailRepository);
  }
}
