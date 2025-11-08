package io.github.tempsotsusei.kotobanotane.application.keyword;

import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import io.github.tempsotsusei.kotobanotane.domain.keyword.Keyword;
import io.github.tempsotsusei.kotobanotane.domain.keyword.KeywordRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * keyword テーブルへの CRUD 操作を取りまとめるアプリケーションサービス。
 *
 * <p>章の存在検証や必須項目のバリデーションを担当する。
 */
@Service
public class KeywordService {

  private final KeywordRepository keywordRepository;
  private final ChapterRepository chapterRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;

  public KeywordService(
      KeywordRepository keywordRepository,
      ChapterRepository chapterRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider) {
    this.keywordRepository = keywordRepository;
    this.chapterRepository = chapterRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
  }

  /**
   * キーワード一覧を取得する。
   *
   * @return keyword 一覧
   */
  public List<Keyword> findAll() {
    return keywordRepository.findAll();
  }

  /**
   * キーワード ID で取得する。
   *
   * @param keywordId キーワード ID
   * @return 該当 keyword（存在しない場合は空）
   */
  public Optional<Keyword> findById(String keywordId) {
    return keywordRepository.findById(keywordId);
  }

  /**
   * 新規キーワードを登録する。
   *
   * @param chapterId 紐付ける章 ID
   * @param keywordValue キーワード本文
   * @param keywordPosition 表示順
   * @return 登録した keyword
   */
  @Transactional
  public Keyword create(String chapterId, String keywordValue, int keywordPosition) {
    validateChapterExists(chapterId);
    ensureKeywordValue(keywordValue);
    ensurePosition(keywordPosition);

    Instant now = timeProvider.nowInstant();
    Keyword keyword =
        new Keyword(
            uuidGeneratorService.generateV7(), chapterId, keywordValue, keywordPosition, now, now);
    return keywordRepository.save(keyword);
  }

  /**
   * キーワード情報を更新する。
   *
   * @param keywordId キーワード ID
   * @param command 更新内容
   * @return 更新後 keyword（存在しない場合は空）
   */
  @Transactional
  public Optional<Keyword> update(String keywordId, KeywordUpdateCommand command) {
    return keywordRepository
        .findById(keywordId)
        .map(existing -> applyUpdate(existing, command))
        .map(keywordRepository::save);
  }

  /**
   * キーワードを削除する。
   *
   * @param keywordId キーワード ID
   */
  @Transactional
  public void delete(String keywordId) {
    keywordRepository.deleteById(keywordId);
  }

  private Keyword applyUpdate(Keyword existing, KeywordUpdateCommand command) {
    String nextChapterId = existing.chapterId();
    if (command.chapterIdSpecified()) {
      String candidate = command.chapterId();
      if (!StringUtils.hasText(candidate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterId must not be blank");
      }
      validateChapterExists(candidate);
      nextChapterId = candidate;
    }

    String nextKeyword = existing.keyword();
    if (command.keywordSpecified()) {
      String candidate = command.keyword();
      ensureKeywordValue(candidate);
      nextKeyword = candidate;
    }

    int nextPosition = existing.keywordPosition();
    if (command.keywordPositionSpecified()) {
      Integer candidate = command.keywordPosition();
      if (candidate == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "keywordPosition must not be null");
      }
      ensurePosition(candidate);
      nextPosition = candidate;
    }

    Instant updatedAt = timeProvider.nowInstant();
    return new Keyword(
        existing.keywordId(),
        nextChapterId,
        nextKeyword,
        nextPosition,
        existing.createdAt(),
        updatedAt);
  }

  private void validateChapterExists(String chapterId) {
    boolean exists = chapterRepository.findById(chapterId).isPresent();
    if (!exists) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterId does not exist: " + chapterId);
    }
  }

  private void ensureKeywordValue(String keywordValue) {
    if (!StringUtils.hasText(keywordValue)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword must not be blank");
    }
  }

  private void ensurePosition(int keywordPosition) {
    if (keywordPosition < 1) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "keywordPosition must be greater than or equal to 1");
    }
  }
}
