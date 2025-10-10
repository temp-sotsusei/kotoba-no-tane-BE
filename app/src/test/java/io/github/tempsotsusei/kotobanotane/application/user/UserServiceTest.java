package io.github.tempsotsusei.kotobanotane.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tempsotsusei.kotobanotane.domain.user.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** UserService の基本動作を検証するテスト。 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

  @Autowired private UserService userService;

  @Test
  void createUserAndFetch() {
    User created = userService.create("auth0|test");
    Optional<User> fetched = userService.findById("auth0|test");
    assertThat(fetched).isPresent();
    assertThat(fetched.get().createdAt()).isNotNull();
    assertThat(fetched.get().updatedAt()).isNotNull();
    assertThat(fetched.get().createdAt()).isEqualTo(created.createdAt());
  }

  @Test
  void updateUserUpdatesTimestamp() {
    User created = userService.create("auth0|update");
    Instant before = created.updatedAt();
    Optional<User> updated = userService.update("auth0|update");
    assertThat(updated).isPresent();
    assertThat(updated.get().updatedAt()).isAfter(before);
  }
}
