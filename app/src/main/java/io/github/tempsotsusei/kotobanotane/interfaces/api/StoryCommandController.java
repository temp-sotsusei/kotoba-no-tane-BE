package io.github.tempsotsusei.kotobanotane.interfaces.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterJsonTextService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterTextAnalysis;
import io.github.tempsotsusei.kotobanotane.application.feedback.FeedbackGenerationJobService;
import io.github.tempsotsusei.kotobanotane.application.story.ChapterDraft;
import io.github.tempsotsusei.kotobanotane.application.story.StoryCreationResult;
import io.github.tempsotsusei.kotobanotane.application.story.StoryCreationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 物語と章を一括登録する本番向け API。
 *
 * <p>保存完了後、各章について非同期でフィードバック生成ジョブを起動する。
 */
@RestController
@RequestMapping("/api/story")
public class StoryCommandController {

  private final AuthenticatedTokenService authenticatedTokenService;
  private final ChapterJsonTextService chapterJsonTextService;
  private final StoryCreationService storyCreationService;
  private final FeedbackGenerationJobService feedbackGenerationJobService;

  public StoryCommandController(
      AuthenticatedTokenService authenticatedTokenService,
      ChapterJsonTextService chapterJsonTextService,
      StoryCreationService storyCreationService,
      FeedbackGenerationJobService feedbackGenerationJobService) {
    this.authenticatedTokenService = authenticatedTokenService;
    this.chapterJsonTextService = chapterJsonTextService;
    this.storyCreationService = storyCreationService;
    this.feedbackGenerationJobService = feedbackGenerationJobService;
  }

  /**
   * 物語と章を登録し、フィードバック生成ジョブを起動する。
   *
   * @param request 物語作成リクエスト
   * @param authentication 認証トークン
   * @return 作成された storyId
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public CreateStoryResponse createStory(
      @Valid @RequestBody CreateStoryRequest request, JwtAuthenticationToken authentication) {
    String auth0Id =
        authenticatedTokenService.requireExistingAuth0Id(
            authenticatedTokenService.extractAuth0Id(authentication.getToken()));
    validateChapterNumbers(request.chapters());

    List<ChapterDraft> drafts =
        request.chapters().stream().map(this::toDraftWithValidation).toList();

    StoryCreationResult result =
        storyCreationService.createStoryWithChapters(
            auth0Id, request.storyTitle(), request.thumbnailId(), drafts);

    // 章ごとに非同期でフィードバック生成ジョブを起動する
    result
        .feedbackTargets()
        .forEach(
            target ->
                feedbackGenerationJobService.generateAndSave(
                    target.chapterId(), target.plainText()));

    return new CreateStoryResponse(result.storyId());
  }

  /**
   * chapterNum が重複していないか、1 以上かを検証する。
   *
   * @param chapters 章リスト
   */
  private void validateChapterNumbers(List<ChapterPayload> chapters) {
    if (chapters == null || chapters.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapters must not be empty");
    }
    Set<Integer> numbers = new HashSet<>();
    for (ChapterPayload chapter : chapters) {
      if (chapter.chapterNum() == null || chapter.chapterNum() < 1) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "chapterNum must be greater than or equal to 1");
      }
      boolean added = numbers.add(chapter.chapterNum());
      if (!added) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterNum must be unique");
      }
    }
  }

  /**
   * 章 JSON を解析し、平文化された本文を使ってドラフト DTO を生成する。
   *
   * @param chapterPayload リクエスト内の章
   * @return 章番号・JSON・平文化テキストを含むドラフト
   */
  private ChapterDraft toDraftWithValidation(ChapterPayload chapterPayload) {
    ChapterTextAnalysis analysis = chapterJsonTextService.analyze(chapterPayload.chapterJson());
    String plainText = analysis.plainText();
    validatePlainTextLength(plainText);
    return new ChapterDraft(chapterPayload.chapterNum(), chapterPayload.chapterJson(), plainText);
  }

  /**
   * 平文化後の章本文が 1〜200 文字の範囲に収まっているかを検証する。
   *
   * @param plainText 平文化後の本文
   */
  private void validatePlainTextLength(String plainText) {
    if (plainText == null || plainText.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson text length must be between 1 and 200");
    }
    if (plainText.length() > 200) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson text length must be between 1 and 200");
    }
  }

  /** `/api/story` のリクエスト DTO。 */
  public record CreateStoryRequest(
      @JsonProperty("storyTitle") @NotBlank @Size(max = 15) String storyTitle,
      @JsonProperty("thumbnailId") String thumbnailId,
      @JsonProperty("chapters") @NotEmpty @Size(min = 1, max = 5)
          List<@Valid ChapterPayload> chapters) {}

  /** 章のリクエスト要素。 */
  public record ChapterPayload(
      @JsonProperty("chapterNum") @NotNull Integer chapterNum,
      @JsonProperty("chapterJson") @NotNull JsonNode chapterJson) {}

  /** `/api/story` のレスポンス DTO。 */
  public record CreateStoryResponse(String storyId) {}
}
