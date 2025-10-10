package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.user;

import io.github.tempsotsusei.kotobanotane.domain.user.User;
import io.github.tempsotsusei.kotobanotane.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** UserRepository の JPA 実装。 */
@Repository
@Transactional(readOnly = true)
public class UserRepositoryImpl implements UserRepository {

  private final UserJpaRepository userJpaRepository;

  public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public Optional<User> findById(String auth0Id) {
    return userJpaRepository.findById(auth0Id).map(UserMapper::toDomain);
  }

  @Override
  public List<User> findAll() {
    return userJpaRepository.findAll().stream().map(UserMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public User save(User user) {
    UserEntity entity =
        userJpaRepository
            .findById(user.auth0Id())
            .map(existing -> UserMapper.toEntityForUpdate(existing, user.updatedAt()))
            .orElse(UserMapper.toEntity(user));

    UserEntity saved = userJpaRepository.save(entity);
    return UserMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String auth0Id) {
    userJpaRepository.deleteById(auth0Id);
  }
}
