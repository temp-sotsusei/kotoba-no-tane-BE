package io.github.tempsotsusei.kotobanotane.interfaces.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.tempsotsusei.kotobanotane.application.story.StoryChapterNextService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直前章の JSON を受け取り、次章生成用キーワードを返却する本番向け API。
 *
 * <p>LLM へは文字列化した本文のみを渡し、レスポンスは 4 語 × 3 セットの配列となる。
 */
@RestController
@RequestMapping("/api/story/chapter/next")
public class StoryChapterNextController {

  private final StoryChapterNextService storyChapterNextService;

  public StoryChapterNextController(StoryChapterNextService storyChapterNextService) {
    this.storyChapterNextService = storyChapterNextService;
  }

  /**
   * TipTap JSON からキーワードを生成する。
   *
   * @param request 章 JSON を含むリクエスト DTO
   * @return LLM が生成したキーワード集合
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public List<List<String>> generateNextKeywords(
      @Valid @RequestBody StoryChapterNextRequest request) {
    return storyChapterNextService.generateNextChapterKeywords(request.chapterJson());
  }

  /** `/api/story/chapter/next` のリクエスト DTO。 */
  public record StoryChapterNextRequest(
      @JsonProperty("chapterJson") @NotNull JsonNode chapterJson) {}
}
