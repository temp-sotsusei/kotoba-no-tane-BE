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

/** feedback CRUD API の挙動を確認する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevFeedbackCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserService userService;
  @Autowired private ThumbnailService thumbnailService;
  @Autowired private StoryService storyService;
  @Autowired private ChapterService chapterService;

  private String chapterId;

  @BeforeEach
  void setUp() {
    String auth0Id = "auth0|feedback-crud";
    userService.findOrCreate(auth0Id);
    Thumbnail thumbnail = thumbnailService.create("/path/to/feedback-thumb.png");
    Story story = storyService.create(auth0Id, "Feedback Story", thumbnail.thumbnailId());
    com.fasterxml.jackson.databind.node.ObjectNode chapterJson =
        objectMapper.createObjectNode().put("body", "Feedback chapter");
    Chapter chapter = chapterService.create(story.storyId(), 1, chapterJson);
    chapterId = chapter.chapterId();
  }

  @Test
  void performsCrudCycle() throws Exception {
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("chapterId", chapterId);
    createRequest.put("feedback", "Great job!");

    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/feedback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.feedback").value("Great job!"))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String feedbackId = created.get("feedbackId").toString();

    // List
    mockMvc.perform(get("/api/crud/feedbacks")).andExpect(status().isOk());

    // Get
    mockMvc
        .perform(get("/api/crud/feedback/" + feedbackId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.feedbackId").value(feedbackId));

    // Update
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("feedback", "Needs improvement");

    mockMvc
        .perform(
            put("/api/crud/feedback/" + feedbackId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.feedback").value("Needs improvement"));

    // Delete
    mockMvc.perform(delete("/api/crud/feedback/" + feedbackId)).andExpect(status().isNoContent());
  }

  @Test
  void returnsBadRequestWhenChapterDoesNotExist() throws Exception {
    mockMvc
        .perform(
            post("/api/crud/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("chapterId", "non-existent", "feedback", "hello"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsBlankFeedbackOnUpdate() throws Exception {
    String feedbackId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of("chapterId", chapterId, "feedback", "initial text"))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("feedbackId")
            .asText();

    mockMvc
        .perform(
            put("/api/crud/feedback/" + feedbackId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("feedback", ""))))
        .andExpect(status().isBadRequest());
  }
}
