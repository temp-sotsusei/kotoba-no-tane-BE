package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import org.springframework.data.jpa.repository.JpaRepository;

/** chapters テーブルへアクセスする Spring Data JPA リポジトリ。 */
public interface ChapterJpaRepository extends JpaRepository<ChapterEntity, String> {}
