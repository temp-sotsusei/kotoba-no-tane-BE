package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

/** users テーブルにアクセスする JPA リポジトリ。 */
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {}
