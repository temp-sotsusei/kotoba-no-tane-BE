package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tempsotsusei.kotobanotane.application.story.StoryService;
import io.github.tempsotsusei.kotobanotane.application.thumbnail.ThumbnailService;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
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

/** chapter CRUD API の基本挙動を確認する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevChapterCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private ThumbnailService thumbnailService;
  @Autowired private StoryService storyService;

  private String auth0Id;
  private String baseStoryId;
  private String secondaryStoryId;

  @BeforeEach
  void setUp() {
    auth0Id = "auth0|chapter-crud";
    userService.findOrCreate(auth0Id);
    Thumbnail thumbnail = thumbnailService.create("/path/to/chapter-thumb.png");

    Story baseStory = storyService.create(auth0Id, "Base Story", thumbnail.thumbnailId());
    baseStoryId = baseStory.storyId();

    Story secondaryStory = storyService.create(auth0Id, "Secondary Story", thumbnail.thumbnailId());
    secondaryStoryId = secondaryStory.storyId();
  }

  @Test
  void performsCrudCycle() throws Exception {
    ObjectNode chapterJson = objectMapper.createObjectNode().put("body", "First scene");
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("storyId", baseStoryId);
    createRequest.put("chapterNum", 1);
    createRequest.put("chapterJson", chapterJson);

    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/chapter")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.storyId").value(baseStoryId))
            .andExpect(jsonPath("$.chapterNum").value(1))
            .andExpect(jsonPath("$.chapterJson.body").value("First scene"))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String chapterId = created.get("chapterId").toString();

    // List
    mockMvc.perform(get("/api/crud/chapters")).andExpect(status().isOk());

    // Get
    mockMvc
        .perform(get("/api/crud/chapter/" + chapterId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chapterId").value(chapterId))
        .andExpect(jsonPath("$.storyId").value(baseStoryId));

    // Update
    ObjectNode updatedJson = objectMapper.createObjectNode().put("body", "Second scene");
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("chapterNum", 2);
    updateRequest.put("chapterJson", updatedJson);
    updateRequest.put("storyId", secondaryStoryId);

    mockMvc
        .perform(
            put("/api/crud/chapter/" + chapterId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chapterNum").value(2))
        .andExpect(jsonPath("$.chapterJson.body").value("Second scene"))
        .andExpect(jsonPath("$.storyId").value(secondaryStoryId));

    // Delete
    mockMvc.perform(delete("/api/crud/chapter/" + chapterId)).andExpect(status().isNoContent());
  }

  @Test
  void returnsBadRequestWhenStoryDoesNotExist() throws Exception {
    ObjectNode chapterJson = objectMapper.createObjectNode().put("body", "Body");
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("storyId", "non-existent-story");
    createRequest.put("chapterNum", 1);
    createRequest.put("chapterJson", chapterJson);

    mockMvc
        .perform(
            post("/api/crud/chapter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsNullChapterJsonOnUpdate() throws Exception {
    String chapterId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/chapter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "storyId",
                                        baseStoryId,
                                        "chapterNum",
                                        3,
                                        "chapterJson",
                                        objectMapper
                                            .createObjectNode()
                                            .put("body", "Original text")))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("chapterId")
            .asText();

    mockMvc
        .perform(
            put("/api/crud/chapter/" + chapterId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"chapterJson\": null}"))
        .andExpect(status().isBadRequest());
  }
}
