package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.llm.AsyncLlmJobService;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 非同期ジョブ用エンドポイントの挙動確認テスト。
 *
 * <p>Mock した AsyncLlmJobService が期待回数で呼び出されるか、レスポンス構造が正しいかを確認する。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"dev", "test"})
class DevAsyncJobControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @MockBean private AsyncLlmJobService asyncLlmJobService;

  @BeforeEach
  void setUp() {
    userService.findOrCreate("async-user");
  }

  @Test
  @DisplayName("POST /api/test/async-jobs で指定回数の非同期ジョブがキックされる")
  void triggerJobsEnqueuesAsyncTasks() throws Exception {
    DevAsyncJobController.JobTriggerRequest request =
        new DevAsyncJobController.JobTriggerRequest("async-user", 3);

    mockMvc
        .perform(
            post("/api/test/async-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.auth0Id").value("async-user"))
        .andExpect(jsonPath("$.queuedJobs").value(3))
        .andExpect(jsonPath("$.updatedAt").exists());

    verify(asyncLlmJobService, times(1)).runJob(eq("async-user"), eq(1));
    verify(asyncLlmJobService, times(1)).runJob(eq("async-user"), eq(2));
    verify(asyncLlmJobService, times(1)).runJob(eq("async-user"), eq(3));
  }
}
