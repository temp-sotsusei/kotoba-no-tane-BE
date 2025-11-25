package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateQueryService;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/thumbnail-templates` の振る舞いを確認する MockMvc テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ThumbnailTemplateControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticatedTokenService authenticatedTokenService;
  @MockBean private ThumbnailTemplateQueryService thumbnailTemplateQueryService;

  @BeforeEach
  void setUp() {
    when(authenticatedTokenService.extractAuth0Id(any())).thenReturn("auth0|user");
    when(authenticatedTokenService.requireExistingAuth0Id("auth0|user")).thenReturn("auth0|user");
  }

  /** JWT 付きの正常系でテンプレートが返ることを検証する。 */
  @Test
  void returnsTemplatesWhenAuthenticated() throws Exception {
    when(thumbnailTemplateQueryService.listTemplates())
        .thenReturn(
            List.of(
                new ThumbnailTemplateSummary("thumb-1", "/path/1.png"),
                new ThumbnailTemplateSummary("thumb-2", "/path/2.png")));

    mockMvc
        .perform(get("/api/thumbnail-templates").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].thumbnailId").value("thumb-1"))
        .andExpect(jsonPath("$[1].thumbnailPath").value("/path/2.png"));
  }

  /** 未認証リクエストが 401 で拒否されることを確認する。 */
  @Test
  void rejectsWhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/thumbnail-templates")).andExpect(status().isUnauthorized());
  }
}
