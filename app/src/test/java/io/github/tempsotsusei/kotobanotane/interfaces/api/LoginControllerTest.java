package io.github.tempsotsusei.kotobanotane.interfaces.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** `/api/login` エンドポイントの振る舞いを確認する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void returnsOkWhenJwtSubjectPresent() throws Exception {
    mockMvc
        .perform(post("/api/login").with(jwt().jwt(jwt -> jwt.subject("auth0|login"))))
        .andExpect(status().isOk());
  }

  @Test
  void returnsBadRequestWhenSubjectBlank() throws Exception {
    mockMvc
        .perform(post("/api/login").with(jwt().jwt(jwt -> jwt.claim("sub", ""))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsUnauthorizedWhenTokenMissing() throws Exception {
    mockMvc.perform(post("/api/login")).andExpect(status().isUnauthorized());
  }
}
