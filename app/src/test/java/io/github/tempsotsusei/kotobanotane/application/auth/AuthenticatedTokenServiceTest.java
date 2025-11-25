package io.github.tempsotsusei.kotobanotane.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import io.github.tempsotsusei.kotobanotane.domain.user.User;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.server.ResponseStatusException;

/** AuthenticatedTokenService の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class AuthenticatedTokenServiceTest {

  @Mock private JwtDecoder jwtDecoder;
  @Mock private UserService userService;

  @InjectMocks private AuthenticatedTokenService authenticatedTokenService;

  @Test
  void extractAuth0IdReturnsSubjectWhenPresent() {
    Jwt jwt = jwtWithSubject("auth0|login");

    String result = authenticatedTokenService.extractAuth0Id(jwt);

    assertThat(result).isEqualTo("auth0|login");
  }

  @Test
  void extractAuth0IdThrowsWhenSubjectMissing() {
    Jwt jwt = jwtWithSubject(null);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> authenticatedTokenService.extractAuth0Id(jwt));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
  }

  @Test
  void validateAndExtractReturnsSubjectAfterDecoding() {
    Jwt jwt = jwtWithSubject("auth0|decoded");
    when(jwtDecoder.decode("token")).thenReturn(jwt);

    String result = authenticatedTokenService.validateAndExtract("token");

    assertThat(result).isEqualTo("auth0|decoded");
  }

  @Test
  void validateAndExtractThrowsWhenTokenMissing() {
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> authenticatedTokenService.validateAndExtract("  "));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  @Test
  void requireExistingAuth0IdReturnsWhenUserExists() {
    Instant now = Instant.now();
    when(userService.findById("auth0|exists"))
        .thenReturn(Optional.of(new User("auth0|exists", now, now)));

    String result = authenticatedTokenService.requireExistingAuth0Id("auth0|exists");

    assertThat(result).isEqualTo("auth0|exists");
  }

  @Test
  void requireExistingAuth0IdThrowsWhenUserMissing() {
    when(userService.findById("auth0|missing")).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> authenticatedTokenService.requireExistingAuth0Id("auth0|missing"));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  private Jwt jwtWithSubject(String subject) {
    Instant now = Instant.now();
    Map<String, Object> claims = new HashMap<>();
    claims.put("iss", "issuer");
    if (subject != null) {
      claims.put("sub", subject);
    }
    return new Jwt("token-value", now, now.plusSeconds(60), Map.of("alg", "none"), claims);
  }
}
