package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterJsonTextService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterTextAnalysis;
import io.github.tempsotsusei.kotobanotane.application.feedback.FeedbackGenerationJobService;
import io.github.tempsotsusei.kotobanotane.application.story.ChapterFeedbackTarget;
import io.github.tempsotsusei.kotobanotane.application.story.StoryCreationResult;
import io.github.tempsotsusei.kotobanotane.application.story.StoryCreationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/story` のバリデーションと正常系を確認する MockMvc テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoryCommandControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticatedTokenService authenticatedTokenService;
  @MockBean private ChapterJsonTextService chapterJsonTextService;
  @MockBean private StoryCreationService storyCreationService;
  @MockBean private FeedbackGenerationJobService feedbackGenerationJobService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    when(authenticatedTokenService.extractAuth0Id(any())).thenReturn("auth0|user");
    when(authenticatedTokenService.requireExistingAuth0Id("auth0|user")).thenReturn("auth0|user");
  }

  /** JWT 付きの正常系で storyId が返り、フィードバックジョブが章数分起動することを検証する。 */
  @Test
  void createsStoryAndStartsFeedbackJobs() throws Exception {
    when(chapterJsonTextService.analyze(any()))
        .thenReturn(new ChapterTextAnalysis("短い本文", List.of()));
    StoryCreationResult result =
        new StoryCreationResult(
            "story-1",
            List.of(
                new ChapterFeedbackTarget("chap-1", "短い本文"),
                new ChapterFeedbackTarget("chap-2", "短い本文")));
    when(storyCreationService.createStoryWithChapters(any(), any(), any(), any()))
        .thenReturn(result);

    String requestBody =
        """
				{
					"storyTitle": "タイトル",
					"thumbnailId": "thumb-1",
					"chapters": [
						{ "chapterNum": 1, "chapterJson": { "type": "doc" } },
						{ "chapterNum": 2, "chapterJson": { "type": "doc" } }
					]
				}
				""";

    mockMvc
        .perform(
            post("/api/story")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.storyId").value("story-1"));

    verify(feedbackGenerationJobService, times(2)).generateAndSave(any(), any());
  }

  /** タイトルが16文字以上の場合に 400 となることを検証する。 */
  @Test
  void rejectsTooLongTitle() throws Exception {
    String longTitle = "abcdefghijklmnop";
    String requestBody =
            """
				{
					"storyTitle": "%s",
					"thumbnailId": "thumb-1",
					"chapters": [
						{ "chapterNum": 1, "chapterJson": { "type": "doc" } }
					]
				}
				"""
            .formatted(longTitle);

    mockMvc
        .perform(
            post("/api/story")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  /** 章数が 6 以上の場合に 400 となることを検証する。 */
  @Test
  void rejectsTooManyChapters() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"storyTitle\":\"タイトル\",\"chapters\":[");
    for (int i = 1; i <= 6; i++) {
      sb.append("{\"chapterNum\":").append(i).append(",\"chapterJson\":{\"type\":\"doc\"}},");
    }
    sb.append("]}");

    mockMvc
        .perform(
            post("/api/story")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sb.toString()))
        .andExpect(status().isBadRequest());
  }

  /** 平文化後 200 文字超の章がある場合に 400 となることを検証する。 */
  @Test
  void rejectsTooLongChapterText() throws Exception {
    String longText = "あ".repeat(201);
    when(chapterJsonTextService.analyze(any()))
        .thenReturn(new ChapterTextAnalysis(longText, List.of()));

    String requestBody =
        """
				{
					"storyTitle": "タイトル",
					"chapters": [
						{ "chapterNum": 1, "chapterJson": { "type": "doc" } }
					]
				}
				""";

    mockMvc
        .perform(
            post("/api/story")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  /** chapterNum が重複した場合に 400 となることを検証する。 */
  @Test
  void rejectsDuplicateChapterNum() throws Exception {
    when(chapterJsonTextService.analyze(any()))
        .thenReturn(new ChapterTextAnalysis("短い本文", List.of()));

    String requestBody =
        """
				{
					"storyTitle": "タイトル",
					"chapters": [
						{ "chapterNum": 1, "chapterJson": { "type": "doc" } },
						{ "chapterNum": 1, "chapterJson": { "type": "doc" } }
					]
				}
				""";

    mockMvc
        .perform(
            post("/api/story")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }
}
