package io.github.tempsotsusei.kotobanotane.application.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * JWT から Auth0 の subject を抽出したり、Bearer トークン文字列を検証して subject を取り出す共通サービス。
 *
 * <p>本番 API ではすべての認証済みリクエストでこのサービスを経由し、subject（=auth0Id）が空の場合は 400 (Bad Request)
 * を返して早期に不正なトークンを検知する。
 */
@Service
public class AuthenticatedTokenService {

  private final JwtDecoder jwtDecoder;

  public AuthenticatedTokenService(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  /**
   * 既に検証済みの {@link Jwt} から subject を取り出して返す。
   *
   * @param jwt Spring Security が解決した JWT
   * @return subject を表す auth0Id
   */
  public String extractAuth0Id(Jwt jwt) {
    String subject = jwt == null ? null : jwt.getSubject();
    if (!StringUtils.hasText(subject)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT subject is missing.");
    }
    return subject;
  }

  /**
   * Bearer トークン文字列を検証し、subject を取り出して返す。
   *
   * @param bearerToken Authorization ヘッダーで受け取ったトークン
   * @return subject を表す auth0Id
   */
  public String validateAndExtract(String bearerToken) {
    if (!StringUtils.hasText(bearerToken)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required.");
    }
    Jwt jwt = jwtDecoder.decode(bearerToken);
    return extractAuth0Id(jwt);
  }
}
