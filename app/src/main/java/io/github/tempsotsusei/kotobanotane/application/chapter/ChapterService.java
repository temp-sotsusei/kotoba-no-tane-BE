package io.github.tempsotsusei.kotobanotane.application.chapter;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * chapter テーブルへの CRUD 操作をまとめるアプリケーションサービス。
 *
 * <p>story 存在チェックや ID 採番など、ドメイン横断の責務を担当する。
 */
@Service
public class ChapterService {

  private final ChapterRepository chapterRepository;
  private final StoryRepository storyRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;

  public ChapterService(
      ChapterRepository chapterRepository,
      StoryRepository storyRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider) {
    this.chapterRepository = chapterRepository;
    this.storyRepository = storyRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
  }

  /**
   * 章の一覧を取得する。
   *
   * @return chapter 一覧
   */
  public List<Chapter> findAll() {
    return chapterRepository.findAll();
  }

  /**
   * 章 ID を指定して取得する。
   *
   * @param chapterId 章 ID
   * @return 該当章（存在しない場合は空）
   */
  public Optional<Chapter> findById(String chapterId) {
    return chapterRepository.findById(chapterId);
  }

  /**
   * 章を新規作成する。
   *
   * @param storyId 紐付ける story ID
   * @param chapterNum 章番号
   * @param chapterJson 本文 JSON
   * @return 作成した章
   */
  @Transactional
  public Chapter create(String storyId, int chapterNum, JsonNode chapterJson) {
    ensureStoryExists(storyId);
    JsonNode normalizedJson = requireChapterJson(chapterJson);

    Instant now = timeProvider.nowInstant();
    Chapter chapter =
        new Chapter(
            uuidGeneratorService.generateV7(), storyId, chapterNum, normalizedJson, now, now);
    return chapterRepository.save(chapter);
  }

  /**
   * 章を更新する。
   *
   * @param chapterId 章 ID
   * @param command 更新内容
   * @return 更新後の章（存在しない場合は空）
   */
  @Transactional
  public Optional<Chapter> update(String chapterId, ChapterUpdateCommand command) {
    return chapterRepository
        .findById(chapterId)
        .map(existing -> applyUpdate(existing, command))
        .map(chapterRepository::save);
  }

  /**
   * 章を削除する。
   *
   * @param chapterId 章 ID
   */
  @Transactional
  public void delete(String chapterId) {
    chapterRepository.deleteById(chapterId);
  }

  private Chapter applyUpdate(Chapter existing, ChapterUpdateCommand command) {
    String nextStoryId = existing.storyId();
    if (command.storyIdSpecified()) {
      String candidate = command.storyId();
      if (!StringUtils.hasText(candidate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storyId must not be blank");
      }
      ensureStoryExists(candidate);
      nextStoryId = candidate;
    }

    int nextChapterNum = existing.chapterNum();
    if (command.chapterNumSpecified()) {
      Integer candidate = command.chapterNum();
      if (candidate == null || candidate < 1) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "chapterNum must be greater than or equal to 1");
      }
      nextChapterNum = candidate;
    }

    JsonNode nextChapterJson = existing.chapterJson();
    if (command.chapterJsonSpecified()) {
      JsonNode candidate = command.chapterJson();
      nextChapterJson = requireChapterJson(candidate);
    }

    Instant updatedAt = timeProvider.nowInstant();
    return new Chapter(
        existing.chapterId(),
        nextStoryId,
        nextChapterNum,
        nextChapterJson,
        existing.createdAt(),
        updatedAt);
  }

  private void ensureStoryExists(String storyId) {
    boolean exists = storyRepository.findById(storyId).isPresent();
    if (!exists) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "storyId does not exist: " + storyId);
    }
  }

  private JsonNode requireChapterJson(JsonNode chapterJson) {
    JsonNode candidate = Objects.requireNonNull(chapterJson, "chapterJson must not be null");
    if (candidate.isNull()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterJson must not be null");
    }
    return candidate;
  }
}
