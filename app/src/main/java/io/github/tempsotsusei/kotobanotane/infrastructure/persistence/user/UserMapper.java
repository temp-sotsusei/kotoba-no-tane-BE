package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.user;

import io.github.tempsotsusei.kotobanotane.domain.user.User;
import java.time.Instant;

/** User エンティティとドメインモデルの相互変換を担うマッパー。 */
public final class UserMapper {

  private UserMapper() {}

  public static User toDomain(UserEntity entity) {
    return new User(entity.getAuth0UserId(), entity.getCreatedAt(), entity.getUpdatedAt());
  }

  public static UserEntity toEntity(User user) {
    return new UserEntity(user.auth0Id(), user.createdAt(), user.updatedAt());
  }

  public static UserEntity toEntityForUpdate(UserEntity entity, Instant updatedAt) {
    entity.touch(updatedAt);
    return entity;
  }
}
