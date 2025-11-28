package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** chapters テーブルへアクセスする Spring Data JPA リポジトリ。 */
public interface ChapterJpaRepository extends JpaRepository<ChapterEntity, String> {

  List<ChapterEntity> findAllByStoryIdOrderByChapterNum(String storyId);
}
