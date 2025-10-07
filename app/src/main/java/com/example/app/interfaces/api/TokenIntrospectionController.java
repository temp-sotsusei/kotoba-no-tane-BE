package com.example.app.interfaces.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test_jwt")
public class TokenIntrospectionController {

  /** 認証済みリクエストを受け取り、検証済み JWT の主要情報をテスト用に返却します。 */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public TokenView getToken(@AuthenticationPrincipal Jwt jwt) {
    return new TokenView(
        jwt.getTokenValue(),
        jwt.getSubject(),
        jwt.getIssuedAt(),
        jwt.getExpiresAt(),
        jwt.getClaims());
  }

  /** JWT のダンプ表現。 */
  public record TokenView(
      String token,
      String subject,
      Instant issuedAt,
      Instant expiresAt,
      Map<String, Object> claims) {}
}
