package io.github.tempsotsusei.kotobanotane.domain.user;

import java.util.List;
import java.util.Optional;

/** ユーザー永続化に関するドメインリポジトリ。 */
public interface UserRepository {

  Optional<User> findById(String auth0Id);

  List<User> findAll();

  User save(User user);

  void deleteById(String auth0Id);
}
