package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import io.github.tempsotsusei.kotobanotane.domain.feedback.FeedbackRepository;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import io.github.tempsotsusei.kotobanotane.domain.user.User;
import io.github.tempsotsusei.kotobanotane.domain.user.UserRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter.ChapterJpaRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.feedback.FeedbackJpaRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story.StoryJpaRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail.ThumbnailJpaRepository;
import io.github.tempsotsusei.kotobanotane.infrastructure.persistence.user.UserJpaRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/story?storyId=...` の取得を検証する MockMvc テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoryQueryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private StoryRepository storyRepository;
  @Autowired private ChapterRepository chapterRepository;
  @Autowired private FeedbackRepository feedbackRepository;
  @Autowired private ThumbnailRepository thumbnailRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private StoryJpaRepository storyJpaRepository;
  @Autowired private ChapterJpaRepository chapterJpaRepository;
  @Autowired private FeedbackJpaRepository feedbackJpaRepository;
  @Autowired private ThumbnailJpaRepository thumbnailJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    feedbackJpaRepository.deleteAll();
    chapterJpaRepository.deleteAll();
    storyJpaRepository.deleteAll();
    thumbnailJpaRepository.deleteAll();
    userJpaRepository.deleteAll();
  }

  /** 所有者 JWT の場合に hasFeedback=true でフィードバックが返ることを確認する。 */
  @Test
  void returnsFeedbackForOwner() throws Exception {
    Instant now = Instant.parse("2025-01-01T00:00:00Z");
    userRepository.save(new User("auth0|owner", now, now));
    thumbnailRepository.save(new Thumbnail("thumb-1", "/path/thumb.png", now, now));
    storyRepository.save(new Story("story-1", "auth0|owner", "タイトル", "thumb-1", now, now));

    JsonNode chapterJson =
        objectMapper.readTree(
            """
            {
              "type": "doc",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    { "type": "customWord", "attrs": { "text": "船" } },
                    { "type": "text", "text": "が" },
                    { "type": "customWord", "attrs": { "text": "静寂" } }
                  ]
                }
              ]
            }
            """);
    chapterRepository.save(new Chapter("chap-1", "story-1", 1, chapterJson, now, now));
    chapterRepository.save(new Chapter("chap-2", "story-1", 2, chapterJson, now, now));
    feedbackRepository.save(new Feedback("fb-1", "chap-1", "感想A", now, now));

    mockMvc
        .perform(
            get("/api/story")
                .param("storyId", "story-1")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|owner"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasFeedback").value(true))
        .andExpect(jsonPath("$.thumbnailPath").value("/path/thumb.png"))
        .andExpect(jsonPath("$.chapters[0].feedback").value("感想A"))
        .andExpect(jsonPath("$.chapters[1].feedback").value(""));
  }

  /** 非所有者 JWT の場合にフィードバックが非表示になることを確認する。 */
  @Test
  void hidesFeedbackForNonOwner() throws Exception {
    Instant now = Instant.parse("2025-01-02T00:00:00Z");
    storyRepository.save(new Story("story-2", "auth0|owner", "タイトル", null, now, now));
    userRepository.save(new User("auth0|owner", now, now));
    userRepository.save(new User("auth0|other", now, now));
    JsonNode chapterJson =
        objectMapper.readTree(
            """
            {
              "type": "doc",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    { "type": "customWord", "attrs": { "text": "静寂" } },
                    { "type": "text", "text": "の" },
                    { "type": "customWord", "attrs": { "text": "船" } }
                  ]
                }
              ]
            }
            """);
    chapterRepository.save(new Chapter("chap-3", "story-2", 1, chapterJson, now, now));
    feedbackRepository.save(new Feedback("fb-3", "chap-3", "感想B", now, now));

    mockMvc
        .perform(
            get("/api/story")
                .param("storyId", "story-2")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|other"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasFeedback").value(false))
        .andExpect(jsonPath("$.chapters[0].feedback").doesNotExist());
  }

  /** 匿名アクセスの場合にフィードバックが非表示になることを確認する。 */
  @Test
  void hidesFeedbackForAnonymous() throws Exception {
    Instant now = Instant.parse("2025-01-03T00:00:00Z");
    storyRepository.save(new Story("story-3", "auth0|owner", "タイトル", null, now, now));
    userRepository.save(new User("auth0|owner", now, now));
    JsonNode chapterJson =
        objectMapper.readTree(
            """
            {
              "type": "doc",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    { "type": "customWord", "attrs": { "text": "鼓動" } }
                  ]
                }
              ]
            }
            """);
    chapterRepository.save(new Chapter("chap-4", "story-3", 1, chapterJson, now, now));

    mockMvc
        .perform(
            get("/api/story").param("storyId", "story-3").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasFeedback").value(false))
        .andExpect(jsonPath("$.chapters[0].feedback").doesNotExist());
  }

  /** 存在しない storyId の場合に 404 となることを確認する。 */
  @Test
  void returnsNotFoundWhenStoryMissing() throws Exception {
    mockMvc.perform(get("/api/story").param("storyId", "missing")).andExpect(status().isNotFound());
  }
}
