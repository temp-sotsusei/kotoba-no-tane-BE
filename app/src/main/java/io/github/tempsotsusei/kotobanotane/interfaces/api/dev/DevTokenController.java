package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import io.github.tempsotsusei.kotobanotane.domain.user.User;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 開発環境で JWT を検証するテスト用 API 群。 */
@RestController
@RequestMapping("/api/test")
@Profile("dev")
public class DevTokenController {

  private final UserService userService;

  public DevTokenController(UserService userService) {
    this.userService = userService;
  }

  /** JWT の内容をそのまま返却して確認するエンドポイント。 */
  @GetMapping("/test_jwt")
  @PreAuthorize("isAuthenticated()")
  public TokenView testJwt(@AuthenticationPrincipal Jwt jwt) {
    return new TokenView(
        jwt.getTokenValue(),
        jwt.getSubject(),
        jwt.getIssuedAt(),
        jwt.getExpiresAt(),
        jwt.getClaims());
  }

  /** JWT の subject をもとにユーザーを検索し、存在しなければ作成して返すエンドポイント。 */
  @GetMapping("/test_user")
  @PreAuthorize("isAuthenticated()")
  public UserView testUser(@AuthenticationPrincipal Jwt jwt) {
    String auth0Id = jwt.getSubject();
    User user = userService.findOrCreate(auth0Id);
    return UserView.from(user);
  }

  /** JWT のダンプ表現。 */
  public record TokenView(
      String token,
      String subject,
      Instant issuedAt,
      Instant expiresAt,
      Map<String, Object> claims) {}

  /** ユーザー情報のレスポンス表現。 */
  public record UserView(String auth0Id, Instant createdAt, Instant updatedAt) {
    static UserView from(User user) {
      return new UserView(user.auth0Id(), user.createdAt(), user.updatedAt());
    }
  }
}
