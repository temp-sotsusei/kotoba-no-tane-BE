package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterService;
import io.github.tempsotsusei.kotobanotane.application.story.StoryService;
import io.github.tempsotsusei.kotobanotane.application.thumbnail.ThumbnailService;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** keyword CRUD API の動作を検証する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevKeywordCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private ThumbnailService thumbnailService;
  @Autowired private StoryService storyService;
  @Autowired private ChapterService chapterService;

  private String baseChapterId;
  private String secondaryChapterId;

  @BeforeEach
  void setUp() {
    String auth0Id = "auth0|keyword-crud";
    userService.findOrCreate(auth0Id);
    Thumbnail thumbnail = thumbnailService.create("/path/to/keyword-thumb.png");
    Story story = storyService.create(auth0Id, "Keyword Story", thumbnail.thumbnailId());
    Chapter baseChapter = chapterService.create(story.storyId(), 1, "Base chapter");
    Chapter secondaryChapter = chapterService.create(story.storyId(), 2, "Secondary chapter");
    baseChapterId = baseChapter.chapterId();
    secondaryChapterId = secondaryChapter.chapterId();
  }

  @Test
  void performsCrudCycle() throws Exception {
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("chapterId", baseChapterId);
    createRequest.put("keyword", "puzzle");
    createRequest.put("keywordPosition", 1);

    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/keyword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.keyword").value("puzzle"))
            .andExpect(jsonPath("$.keywordPosition").value(1))
            .andExpect(jsonPath("$.chapterId").value(baseChapterId))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String keywordId = created.get("keywordId").toString();

    // List
    mockMvc.perform(get("/api/crud/keywords")).andExpect(status().isOk());

    // Get
    mockMvc
        .perform(get("/api/crud/keyword/" + keywordId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keywordId").value(keywordId));

    // Update
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("chapterId", secondaryChapterId);
    updateRequest.put("keyword", "mystery");
    updateRequest.put("keywordPosition", 3);

    mockMvc
        .perform(
            put("/api/crud/keyword/" + keywordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keyword").value("mystery"))
        .andExpect(jsonPath("$.keywordPosition").value(3))
        .andExpect(jsonPath("$.chapterId").value(secondaryChapterId));

    // Delete
    mockMvc.perform(delete("/api/crud/keyword/" + keywordId)).andExpect(status().isNoContent());

    // confirm deletion
    mockMvc.perform(get("/api/crud/keyword/" + keywordId)).andExpect(status().isNotFound());
  }

  @Test
  void returnsBadRequestWhenChapterDoesNotExist() throws Exception {
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("chapterId", "non-existent-chapter");
    createRequest.put("keyword", "alpha");
    createRequest.put("keywordPosition", 1);

    mockMvc
        .perform(
            post("/api/crud/keyword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsBlankKeywordOnUpdate() throws Exception {
    String keywordId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/keyword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "chapterId",
                                        baseChapterId,
                                        "keyword",
                                        "initial",
                                        "keywordPosition",
                                        1))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("keywordId")
            .asText();

    mockMvc
        .perform(
            put("/api/crud/keyword/" + keywordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("keyword", ""))))
        .andExpect(status().isBadRequest());
  }
}
