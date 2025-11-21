package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tempsotsusei.kotobanotane.application.story.StoryChapterNextService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

/** `/api/story/chapter/next` の振る舞いを確認する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoryChapterNextControllerTest {

  private static final String REQUEST_BODY =
      """
			{
				"chapterJson": {
					"type": "doc",
					"content": [
						{ "type": "paragraph" }
					]
				}
			}
			""";

  @Autowired private MockMvc mockMvc;

  @MockBean private StoryChapterNextService storyChapterNextService;

  /** JWT 付きリクエストでキーワード配列が返る正常系を検証する。 */
  @Test
  void returnsKeywordsWhenAuthenticated() throws Exception {
    when(storyChapterNextService.generateNextChapterKeywords(any()))
        .thenReturn(List.of(List.of("ひかり", "そら", "みち", "ゆめ")));

    mockMvc
        .perform(
            post("/api/story/chapter/next")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0][1]").value("そら"));
  }

  /** 未認証リクエストが 401 で拒否されることを確認する。 */
  @Test
  void rejectsWhenUnauthenticated() throws Exception {
    mockMvc
        .perform(
            post("/api/story/chapter/next")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isUnauthorized());
  }

  /** Service 層からの 400 エラーが Controller でも 400 として伝播することを検証する。 */
  @Test
  void returnsBadRequestWhenServiceThrows() throws Exception {
    when(storyChapterNextService.generateNextChapterKeywords(any()))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "chapterJson text length must be between 1 and 200"));

    mockMvc
        .perform(
            post("/api/story/chapter/next")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
        .andExpect(status().isBadRequest());
  }
}
