package com.example.app.interfaces.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 開発用 CRUD API の基本シナリオを検証するテスト。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevUserCrudControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void performsCrudCycle() throws Exception {
    String auth0Id = "auth0|crud";

    // Create
    mockMvc
        .perform(
            post("/api/crud/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("auth0Id", auth0Id))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.auth0Id").value(auth0Id));

    // Read
    mockMvc
        .perform(get("/api/crud/user/" + auth0Id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.auth0Id").value(auth0Id));

    // Update
    mockMvc
        .perform(put("/api/crud/user/" + auth0Id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.auth0Id").value(auth0Id));

    // Delete
    mockMvc.perform(delete("/api/crud/user/" + auth0Id)).andExpect(status().isNoContent());
  }
}
