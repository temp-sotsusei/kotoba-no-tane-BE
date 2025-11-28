package io.github.tempsotsusei.kotobanotane.interfaces.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.story.StoryQueryService;
import io.github.tempsotsusei.kotobanotane.application.story.StoryQueryService.KeywordItem;
import io.github.tempsotsusei.kotobanotane.application.story.StoryQueryService.StoryDetailResult;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Story 詳細取得 API。
 *
 * <p>認証がなくても参照できるが、認証済みかつ所有者の場合のみフィードバックを含めて返却する。
 */
@RestController
@RequestMapping("/api/story")
public class StoryQueryController {

  private final AuthenticatedTokenService authenticatedTokenService;
  private final StoryQueryService storyQueryService;

  public StoryQueryController(
      AuthenticatedTokenService authenticatedTokenService, StoryQueryService storyQueryService) {
    this.authenticatedTokenService = authenticatedTokenService;
    this.storyQueryService = storyQueryService;
  }

  /**
   * Story を取得する。認証済みかつ所有者であればフィードバックも返却する。
   *
   * @param storyId 取得対象 Story ID
   * @param authentication 任意の JWT（匿名の場合は null）
   * @return Story 詳細レスポンス
   */
  @GetMapping
  @PreAuthorize("permitAll()")
  public StoryResponse getStory(
      @RequestParam("storyId") String storyId, JwtAuthenticationToken authentication) {
    String auth0Id = null;
    if (authentication != null) {
      auth0Id =
          authenticatedTokenService.requireExistingAuth0Id(
              authenticatedTokenService.extractAuth0Id(authentication.getToken()));
    }

    StoryDetailResult detail = storyQueryService.fetch(storyId, auth0Id);
    List<ChapterResponse> chapters =
        detail.chapters().stream()
            .map(
                chapter ->
                    new ChapterResponse(
                        chapter.chapterNum(),
                        chapter.chapterJson(),
                        chapter.keywords(),
                        chapter.feedback()))
            .toList();

    return new StoryResponse(
        detail.storyTitle(), detail.thumbnailPath(), detail.hasFeedback(), chapters);
  }

  /** `/api/story` のレスポンス DTO。 */
  public record StoryResponse(
      String storyTitle,
      String thumbnailPath,
      boolean hasFeedback,
      List<ChapterResponse> chapters) {}

  /** 章のレスポンス DTO。 */
  public record ChapterResponse(
      int chapterNum,
      @JsonProperty("chapterJson") JsonNode chapterJson,
      List<KeywordItem> keywords,
      @JsonInclude(JsonInclude.Include.NON_NULL) String feedback) {}
}
