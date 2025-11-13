package io.github.tempsotsusei.kotobanotane.interfaces.api;

import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 章本文が存在しない初回フェーズ向けにキーワード候補を返すコントローラー。
 *
 * <p>LLM から取得した 4 語×3 セットの配列をそのままクライアントへ返却する。
 */
@RestController
@RequestMapping("/api/story/chapter/keywords")
public class StoryChapterKeywordsController {

  private final KeywordListsGenerationService keywordListsGenerationService;

  public StoryChapterKeywordsController(
      KeywordListsGenerationService keywordListsGenerationService) {
    this.keywordListsGenerationService = keywordListsGenerationService;
  }

  /** 初回サジェスト用のキーワードを取得する。 */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<List<String>> getInitialKeywords() {
    return keywordListsGenerationService.generateInitialKeywords();
  }
}
