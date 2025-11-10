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

/** thumbnail template CRUD API の動作を検証する結合テスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevThumbnailTemplateCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ThumbnailService thumbnailService;

  private String baseThumbnailId;
  private String secondaryThumbnailId;

  @BeforeEach
  void setUp() {
    Thumbnail base = thumbnailService.create("/path/to/base.png");
    Thumbnail secondary = thumbnailService.create("/path/to/secondary.png");
    baseThumbnailId = base.thumbnailId();
    secondaryThumbnailId = secondary.thumbnailId();
  }

  @Test
  void performsCrudCycle() throws Exception {
    Map<String, Object> createRequest = new HashMap<>();
    createRequest.put("thumbnailId", baseThumbnailId);

    // Create
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/thumbnail_template")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.thumbnailId").value(baseThumbnailId))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String templateId = created.get("thumbnailTemplateId").toString();

    // List
    mockMvc.perform(get("/api/crud/thumbnail_templates")).andExpect(status().isOk());

    // Get
    mockMvc
        .perform(get("/api/crud/thumbnail_template/" + templateId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.thumbnailTemplateId").value(templateId));

    // Update
    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("thumbnailId", secondaryThumbnailId);

    mockMvc
        .perform(
            put("/api/crud/thumbnail_template/" + templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.thumbnailId").value(secondaryThumbnailId));

    // Delete
    mockMvc
        .perform(delete("/api/crud/thumbnail_template/" + templateId))
        .andExpect(status().isNoContent());
  }

  @Test
  void returnsBadRequestWhenThumbnailDoesNotExist() throws Exception {
    mockMvc
        .perform(
            post("/api/crud/thumbnail_template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("thumbnailId", "non-existent"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsBlankThumbnailIdOnUpdate() throws Exception {
    String templateId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/crud/thumbnail_template")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of("thumbnailId", baseThumbnailId))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("thumbnailTemplateId")
            .asText();

    mockMvc
        .perform(
            put("/api/crud/thumbnail_template/" + templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("thumbnailId", ""))))
        .andExpect(status().isBadRequest());
  }
}
