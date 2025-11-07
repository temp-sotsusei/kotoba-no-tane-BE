package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.thumbnail.ThumbnailService;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
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

/** ストーリー CRUD API の基本的な動作を確認する統合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevStoryCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private ThumbnailService thumbnailService;

  private String auth0Id;
  private String initialThumbnailId;

  @BeforeEach
  void setUp() {
    auth0Id = "auth0|story-crud";
    userService.findOrCreate(auth0Id);
    Thumbnail thumbnail = thumbnailService.create("/path/to/story-thumb.png");
    initialThumbnailId = thumbnail.thumbnailId();
  }

  @Test
  void performsCrudCycle() throws Exception {
    String storyTitle = "My First Story";

    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("auth0Id", auth0Id);
    createRequest.put("storyTitle", storyTitle);
    createRequest.put("thumbnailId", initialThumbnailId);

    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/story")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.storyTitle").value(storyTitle))
            .andExpect(jsonPath("$.thumbnailId").value(initialThumbnailId))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String storyId = created.get("storyId").toString();

    // List
    mockMvc
        .perform(get("/api/crud/stories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].storyId").exists());

    // Get
    mockMvc
        .perform(get("/api/crud/story/" + storyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.storyId").value(storyId))
        .andExpect(jsonPath("$.auth0Id").value(auth0Id));

    // Update
    String updatedTitle = "Updated Story Title";

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("storyTitle", updatedTitle);

    mockMvc
        .perform(
            put("/api/crud/story/" + storyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.storyTitle").value(updatedTitle))
        .andExpect(jsonPath("$.thumbnailId").value(initialThumbnailId));

    // Delete
    mockMvc.perform(delete("/api/crud/story/" + storyId)).andExpect(status().isNoContent());
  }

  @Test
  void skipsUpdatingNonNullableTitleWhenBlank() throws Exception {
    String storyId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/story")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "auth0Id", auth0Id,
                                        "storyTitle", "Original Title",
                                        "thumbnailId", initialThumbnailId))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("storyId")
            .asText();

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("storyTitle", "");
    updateRequest.put("thumbnailId", "");

    mockMvc
        .perform(
            put("/api/crud/story/" + storyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.storyTitle").value("Original Title"))
        .andExpect(jsonPath("$.thumbnailId").doesNotExist());
  }

  @Test
  void returnsBadRequestWhenThumbnailDoesNotExist() throws Exception {
    String storyId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/story")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "auth0Id", auth0Id,
                                        "storyTitle", "Original Title",
                                        "thumbnailId", initialThumbnailId))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("storyId")
            .asText();

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("thumbnailId", "non-existent-thumb");

    mockMvc
        .perform(
            put("/api/crud/story/" + storyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }
}
