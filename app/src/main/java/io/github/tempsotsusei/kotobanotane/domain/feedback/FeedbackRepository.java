package io.github.tempsotsusei.kotobanotane.domain.feedback;

import java.util.List;
import java.util.Optional;

/** feedbacks テーブルへアクセスするためのリポジトリ。 */
public interface FeedbackRepository {

  Optional<Feedback> findById(String feedbackId);

  List<Feedback> findAll();

  /** 章ID一覧に紐づくフィードバックをまとめて取得する。 */
  List<Feedback> findAllByChapterIdIn(Iterable<String> chapterIds);

  Feedback save(Feedback feedback);

  void deleteById(String feedbackId);
}
