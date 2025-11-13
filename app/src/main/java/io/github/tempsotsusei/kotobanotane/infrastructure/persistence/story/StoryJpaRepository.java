package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** stories テーブルにアクセスする Spring Data JPA リポジトリ。 */
public interface StoryJpaRepository extends JpaRepository<StoryEntity, String> {

  List<StoryEntity> findByAuth0UserIdOrderByCreatedAtDesc(String auth0UserId);
}
