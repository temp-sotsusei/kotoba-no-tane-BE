package io.github.tempsotsusei.kotobanotane.application.feedback;

import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import io.github.tempsotsusei.kotobanotane.domain.feedback.FeedbackRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * feedbacks テーブルへの CRUD を担うアプリケーションサービス。
 *
 * <p>chapter の存在確認や本文バリデーションなどを担当する。
 */
@Service
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final ChapterRepository chapterRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;

  public FeedbackService(
      FeedbackRepository feedbackRepository,
      ChapterRepository chapterRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider) {
    this.feedbackRepository = feedbackRepository;
    this.chapterRepository = chapterRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
  }

  /** すべてのフィードバックを返す。 */
  public List<Feedback> findAll() {
    return feedbackRepository.findAll();
  }

  /** フィードバック ID で取得する。 */
  public Optional<Feedback> findById(String feedbackId) {
    return feedbackRepository.findById(feedbackId);
  }

  /**
   * フィードバックを新規登録する。
   *
   * @param chapterId 紐付ける章 ID
   * @param feedbackText 本文
   * @return 登録結果
   */
  @Transactional
  public Feedback create(String chapterId, String feedbackText) {
    ensureChapterExists(chapterId);
    ensureFeedbackText(feedbackText);

    Instant now = timeProvider.nowInstant();
    Feedback feedback =
        new Feedback(uuidGeneratorService.generateV7(), chapterId, feedbackText, now, now);
    return feedbackRepository.save(feedback);
  }

  /**
   * フィードバックを更新する。
   *
   * @param feedbackId フィードバック ID
   * @param command 差分コマンド
   * @return 更新結果（存在しない場合は空）
   */
  @Transactional
  public Optional<Feedback> update(String feedbackId, FeedbackUpdateCommand command) {
    return feedbackRepository
        .findById(feedbackId)
        .map(existing -> applyUpdate(existing, command))
        .map(feedbackRepository::save);
  }

  /** フィードバックを削除する。 */
  @Transactional
  public void delete(String feedbackId) {
    feedbackRepository.deleteById(feedbackId);
  }

  private Feedback applyUpdate(Feedback existing, FeedbackUpdateCommand command) {
    String nextChapterId = existing.chapterId();
    if (command.chapterIdSpecified()) {
      String candidate = command.chapterId();
      if (!StringUtils.hasText(candidate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapterId must not be blank");
      }
      ensureChapterExists(candidate);
      nextChapterId = candidate;
    }

    String nextFeedback = existing.feedback();
    if (command.feedbackSpecified()) {
      ensureFeedbackText(command.feedback());
      nextFeedback = command.feedback();
    }

    Instant updatedAt = timeProvider.nowInstant();
    return new Feedback(
        existing.feedbackId(), nextChapterId, nextFeedback, existing.createdAt(), updatedAt);
  }

  private void ensureChapterExists(String chapterId) {
    boolean exists = chapterRepository.findById(chapterId).isPresent();
    if (!exists) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterId does not exist: " + chapterId);
    }
  }

  private void ensureFeedbackText(String feedbackText) {
    if (!StringUtils.hasText(feedbackText)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "feedback must not be blank");
    }
  }
}
