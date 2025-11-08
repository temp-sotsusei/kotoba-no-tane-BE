package io.github.tempsotsusei.kotobanotane.domain.feedback;

import java.util.List;
import java.util.Optional;

/** feedbacks テーブルへアクセスするためのリポジトリ。 */
public interface FeedbackRepository {

  Optional<Feedback> findById(String feedbackId);

  List<Feedback> findAll();

  Feedback save(Feedback feedback);

  void deleteById(String feedbackId);
}
