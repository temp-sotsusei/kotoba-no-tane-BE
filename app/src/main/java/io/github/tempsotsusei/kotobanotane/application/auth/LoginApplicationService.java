package io.github.tempsotsusei.kotobanotane.application.auth;

import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** `/api/login` のビジネスロジックを担当するサービス。 */
@Service
public class LoginApplicationService {

  private final AuthenticatedTokenService authenticatedTokenService;
  private final UserService userService;

  public LoginApplicationService(
      AuthenticatedTokenService authenticatedTokenService, UserService userService) {
    this.authenticatedTokenService = authenticatedTokenService;
    this.userService = userService;
  }

  /**
   * ログイン API を実行し、ユーザーを特定または作成する。
   *
   * @param jwt Spring Security が検証済みの JWT
   */
  public void login(Jwt jwt) {
    String auth0Id = authenticatedTokenService.extractAuth0Id(jwt);
    userService.findOrCreate(auth0Id);
  }
}
