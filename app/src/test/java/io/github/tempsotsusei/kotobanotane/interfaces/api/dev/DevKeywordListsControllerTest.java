package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test", "dev"})
class DevKeywordListsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private KeywordListsGenerationService keywordListsGenerationService;

  @Test
  void returnsKeywordLists() throws Exception {
    when(keywordListsGenerationService.generate(any()))
        .thenReturn(
            List.of(
                List.of("りんご", "ばなな", "くるま", "ねこ"),
                List.of("いぬ", "とり", "つくえ", "そら"),
                List.of("みかん", "もも", "ぼうし", "くつ")));

    mockMvc
        .perform(
            post("/api/test/keyword_lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new DevKeywordListsController.KeywordSetRequest("サンプル文章"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").isArray())
        .andExpect(jsonPath("$[0][0]").value("りんご"));
  }

  @Test
  void returnsBadRequestWhenChapterTextBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/test/keyword_lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"chapterText\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
