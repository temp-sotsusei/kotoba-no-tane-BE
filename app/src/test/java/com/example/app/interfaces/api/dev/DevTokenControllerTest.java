package com.example.app.interfaces.api.dev;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** JWT テスト用エンドポイントの動作確認。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevTokenControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void testJwtReturnsClaims() throws Exception {
    mockMvc
        .perform(get("/api/test/test_jwt").with(jwt().jwt(jwt -> jwt.subject("auth0|jwt-test"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.subject").value("auth0|jwt-test"));
  }

  @Test
  void testUserCreatesAndReturnsUser() throws Exception {
    mockMvc
        .perform(get("/api/test/test_user").with(jwt().jwt(jwt -> jwt.subject("auth0|jwt-user"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.auth0Id").value("auth0|jwt-user"));
  }
}
