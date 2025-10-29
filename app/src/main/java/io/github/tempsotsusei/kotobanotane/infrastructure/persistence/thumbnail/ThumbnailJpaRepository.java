package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA を利用したサムネイル向けの基本 CRUD リポジトリ。 */
public interface ThumbnailJpaRepository extends JpaRepository<ThumbnailEntity, String> {}
