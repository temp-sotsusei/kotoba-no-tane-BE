package io.github.tempsotsusei.kotobanotane.application.story;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterJsonTextService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterTextAnalysis;
import io.github.tempsotsusei.kotobanotane.application.chapter.KeywordPosition;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import io.github.tempsotsusei.kotobanotane.domain.feedback.FeedbackRepository;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Story 詳細を取得するクエリサービス。
 *
 * <p>認証ユーザーと所有者が一致する場合のみフィードバックを返却し、それ以外はフィードバックを非表示にする。
 */
@Service
public class StoryQueryService {

  private final StoryRepository storyRepository;
  private final ChapterRepository chapterRepository;
  private final FeedbackRepository feedbackRepository;
  private final ThumbnailRepository thumbnailRepository;
  private final ChapterJsonTextService chapterJsonTextService;
  private final ObjectMapper objectMapper;

  public StoryQueryService(
      StoryRepository storyRepository,
      ChapterRepository chapterRepository,
      FeedbackRepository feedbackRepository,
      ThumbnailRepository thumbnailRepository,
      ChapterJsonTextService chapterJsonTextService,
      ObjectMapper objectMapper) {
    this.storyRepository = storyRepository;
    this.chapterRepository = chapterRepository;
    this.feedbackRepository = feedbackRepository;
    this.thumbnailRepository = thumbnailRepository;
    this.chapterJsonTextService = chapterJsonTextService;
    this.objectMapper = objectMapper;
  }

  /**
   * Story を取得し、フィードバック表示可否を含めたレスポンス DTO を生成する。
   *
   * @param storyId 取得対象 Story ID
   * @param requesterAuth0Id 認証済みユーザー ID（匿名の場合は null）
   * @return Story 詳細レスポンス DTO
   */
  public StoryDetailResult fetch(String storyId, String requesterAuth0Id) {
    Story story =
        storyRepository
            .findById(storyId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "story not found: " + storyId));

    boolean isOwner = requesterAuth0Id != null && requesterAuth0Id.equals(story.auth0UserId());
    List<Chapter> chapters = chapterRepository.findAllByStoryIdOrderByChapterNum(story.storyId());

    Map<String, String> feedbackMap =
        isOwner && !chapters.isEmpty()
            ? feedbackRepository
                .findAllByChapterIdIn(chapters.stream().map(Chapter::chapterId).toList())
                .stream()
                .collect(Collectors.toMap(Feedback::chapterId, Feedback::feedback))
            : Collections.emptyMap();

    boolean hasFeedback = isOwner;
    List<ChapterDetailResult> chapterDetails =
        chapters.stream()
            .map(
                chapter -> {
                  JsonNode chapterJson = ensureObjectNode(chapter.chapterJson());
                  ChapterTextAnalysis analysis = chapterJsonTextService.analyze(chapterJson);
                  List<KeywordItem> keywords =
                      analysis.keywordsWithOffset().stream()
                          .map(KeywordPosition::keyword)
                          .map(KeywordItem::new)
                          .toList();
                  String feedback =
                      isOwner ? feedbackMap.getOrDefault(chapter.chapterId(), "") : null;
                  return new ChapterDetailResult(
                      chapter.chapterNum(), chapterJson, keywords, feedback);
                })
            .toList();

    String thumbnailPath =
        Optional.ofNullable(story.thumbnailId())
            .flatMap(thumbnailRepository::findById)
            .map(t -> t.thumbnailPath())
            .orElse(null);

    return new StoryDetailResult(story.storyTitle(), thumbnailPath, hasFeedback, chapterDetails);
  }

  /**
   * 文字列ノードで入ってきた場合は JSON にパースし直す。
   *
   * @param original Chapter に保存されている JSON ノード
   * @return ObjectNode 系の JSON
   */
  private JsonNode ensureObjectNode(JsonNode original) {
    if (original == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterJson is missing");
    }
    if (original.isTextual()) {
      try {
        return objectMapper.readTree(original.asText());
      } catch (JsonProcessingException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterJson parse error", e);
      }
    }
    return original;
  }

  /** Story 詳細レスポンス DTO。 */
  public record StoryDetailResult(
      String storyTitle,
      String thumbnailPath,
      boolean hasFeedback,
      List<ChapterDetailResult> chapters) {}

  /** 章のレスポンス DTO。 */
  public record ChapterDetailResult(
      int chapterNum,
      @JsonProperty("chapterJson") JsonNode chapterJson,
      List<KeywordItem> keywords,
      @JsonInclude(JsonInclude.Include.NON_NULL) String feedback) {}

  /** キーワード要素。 */
  public record KeywordItem(String keyword) {
    public KeywordItem {
      Objects.requireNonNull(keyword, "keyword must not be null");
    }
  }
}
