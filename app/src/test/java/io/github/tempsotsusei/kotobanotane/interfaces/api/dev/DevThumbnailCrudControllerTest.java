package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * DevThumbnailCrudController の CRUD 動作を一巡させて検証する統合テスト。
 *
 * <p>開発用エンドポイントが期待どおりに動作するかを確認する。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevThumbnailCrudControllerTest {

  /** 開発用 API を呼び出すためのモック MVC。 */
  @Autowired private MockMvc mockMvc;

  /** JSON 変換に利用する ObjectMapper。 */
  @Autowired private ObjectMapper objectMapper;

  @Test
  void performsCrudCycle() throws Exception {
    String initialPath = "/path/to/thumbnail.png";
    String updatedPath = "/path/to/updated-thumbnail.png";

    // --- Create: 新規作成してレスポンスから ID を取得 ---
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/crud/thumbnail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("thumbnailPath", initialPath))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.thumbnailPath").value(initialPath))
            .andReturn();

    Map<String, Object> created =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            new TypeReference<Map<String, Object>>() {});
    String thumbnailId = created.get("thumbnailId").toString();

    // --- Read (list): 一覧取得で登録済みデータが存在することを検証 ---
    mockMvc
        .perform(get("/api/crud/thumbnails"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].thumbnailId").exists());

    // --- Read (single): 個別取得で最初に作成したパスが返ることを確認 ---
    mockMvc
        .perform(get("/api/crud/thumbnail/" + thumbnailId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.thumbnailPath").value(initialPath));

    // --- Update: パスを更新して変更が反映されることを確認 ---
    mockMvc
        .perform(
            put("/api/crud/thumbnail/" + thumbnailId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("thumbnailPath", updatedPath))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.thumbnailPath").value(updatedPath));

    // --- Delete: 削除 API を呼び、204 応答を確認 ---
    mockMvc.perform(delete("/api/crud/thumbnail/" + thumbnailId)).andExpect(status().isNoContent());
  }
}
