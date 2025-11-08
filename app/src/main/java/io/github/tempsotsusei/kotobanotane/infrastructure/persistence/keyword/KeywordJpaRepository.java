package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.keyword;

import org.springframework.data.jpa.repository.JpaRepository;

/** keywords テーブルにアクセスする Spring Data JPA リポジトリ。 */
public interface KeywordJpaRepository extends JpaRepository<KeywordEntity, String> {}
