package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/story/chapter/keywords` の挙動を確認する統合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoryChapterKeywordsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticatedTokenService authenticatedTokenService;
  @MockBean private KeywordListsGenerationService keywordListsGenerationService;

  @BeforeEach
  void setUp() {
    when(authenticatedTokenService.extractAuth0Id(any())).thenReturn("auth0|user");
    when(authenticatedTokenService.requireExistingAuth0Id("auth0|user")).thenReturn("auth0|user");
  }

  /** JWT 付きリクエストで初期キーワード配列を返すことを確認する。 */
  @Test
  void returnsKeywordsForAuthenticatedUser() throws Exception {
    when(keywordListsGenerationService.generateInitialKeywords())
        .thenReturn(List.of(List.of("あお", "あか", "きいろ", "みどり")));

    mockMvc
        .perform(get("/api/story/chapter/keywords").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0][0]").value("あお"))
        .andExpect(jsonPath("$[0][3]").value("みどり"));
  }

  /** 未認証リクエストを 401 で弾くことを確認する。 */
  @Test
  void rejectsWhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/story/chapter/keywords")).andExpect(status().isUnauthorized());
  }
}
