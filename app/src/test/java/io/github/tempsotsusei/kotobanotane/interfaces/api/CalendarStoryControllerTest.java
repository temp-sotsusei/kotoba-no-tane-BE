package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story.StoryJpaRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail.ThumbnailJpaRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/calendar/stories` の統合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CalendarStoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private StoryRepository storyRepository;
  @Autowired private ThumbnailRepository thumbnailRepository;
  @Autowired private StoryJpaRepository storyJpaRepository;
  @Autowired private ThumbnailJpaRepository thumbnailJpaRepository;

  @BeforeEach
  void setUp() {
    storyJpaRepository.deleteAll();
    thumbnailJpaRepository.deleteAll();
  }

  @Test
  void returnsCalendarStoriesForAuthenticatedUser() throws Exception {
    String auth0Id = "auth0|calendar";
    Instant createdLatest = Instant.parse("2025-03-02T10:15:30Z");
    Instant createdOld = Instant.parse("2025-02-28T08:00:00Z");

    thumbnailRepository.save(new Thumbnail("thumb-1", "/images/thumb.png", createdOld, createdOld));

    storyRepository.save(
        new Story("story-new", auth0Id, "Latest Story", "thumb-1", createdLatest, createdLatest));
    storyRepository.save(
        new Story("story-old", auth0Id, "Old Story", null, createdOld, createdOld));

    mockMvc
        .perform(get("/api/calendar/stories").with(jwt().jwt(jwt -> jwt.subject(auth0Id))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].storyId").value("story-new"))
        .andExpect(jsonPath("$[0].thumbnailPath").value("/images/thumb.png"))
        .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
        .andExpect(jsonPath("$[1].storyId").value("story-old"))
        .andExpect(jsonPath("$[1].thumbnailPath").value(nullValue()));
  }

  @Test
  void returnsUnauthorizedWithoutJwt() throws Exception {
    mockMvc.perform(get("/api/calendar/stories")).andExpect(status().isUnauthorized());
  }
}
