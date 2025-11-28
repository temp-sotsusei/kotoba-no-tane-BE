package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.feedback;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** feedbacks テーブルへアクセスする Spring Data JPA リポジトリ。 */
public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, String> {

  List<FeedbackEntity> findAllByChapterIdIn(Iterable<String> chapterIds);
}
