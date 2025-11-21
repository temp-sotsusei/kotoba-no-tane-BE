package io.github.tempsotsusei.kotobanotane.application.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterJsonTextService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterTextAnalysis;
import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class StoryChapterNextServiceTest {

  private final ChapterJsonTextService chapterJsonTextService =
      Mockito.mock(ChapterJsonTextService.class);
  private final KeywordListsGenerationService keywordListsGenerationService =
      Mockito.mock(KeywordListsGenerationService.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  private StoryChapterNextService service;
  private JsonNode chapterJson;

  @BeforeEach
  void setUp() throws Exception {
    service = new StoryChapterNextService(chapterJsonTextService, keywordListsGenerationService);
    chapterJson = objectMapper.readTree("{\"type\":\"doc\",\"content\":[]}");
  }

  /** 正常な JSON を受け取った際に LLM 呼び出しが行われることを確認する。 */
  @Test
  void generateNextChapterKeywordsCallsLlmWhenValid() {
    when(chapterJsonTextService.analyze(chapterJson))
        .thenReturn(new ChapterTextAnalysis("ひかりの道", List.of()));
    when(keywordListsGenerationService.generate("ひかりの道"))
        .thenReturn(List.of(List.of("ひかり", "みち", "そら", "ゆめ")));

    List<List<String>> response = service.generateNextChapterKeywords(chapterJson);

    assertThat(response).hasSize(1);
    verify(keywordListsGenerationService).generate("ひかりの道");
  }

  /** 文字列化した本文が空の場合に 400 エラーとなることを検証する。 */
  @Test
  void generateNextChapterKeywordsRejectsEmptyText() {
    when(chapterJsonTextService.analyze(chapterJson))
        .thenReturn(new ChapterTextAnalysis(" \n", List.of()));

    assertThatThrownBy(() -> service.generateNextChapterKeywords(chapterJson))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("between 1 and 200");
  }

  /** 200 文字を超える本文が送られた場合に 400 エラーで拒否されることを検証する。 */
  @Test
  void generateNextChapterKeywordsRejectsOverLimitText() {
    String longText = "あ".repeat(201);
    when(chapterJsonTextService.analyze(chapterJson))
        .thenReturn(new ChapterTextAnalysis(longText, List.of()));

    assertThatThrownBy(() -> service.generateNextChapterKeywords(chapterJson))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("between 1 and 200");
  }
}
