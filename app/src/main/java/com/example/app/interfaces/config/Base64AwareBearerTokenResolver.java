package com.example.app.interfaces.config;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.StringUtils;

/**
 * フロントエンド側で Base64 エンコードされたアクセストークンを受け取り、 必要に応じて元の JWT 文字列へ戻してから Spring Security に引き渡す Resolver です。
 */
class Base64AwareBearerTokenResolver implements BearerTokenResolver {

  // 標準の BearerTokenResolver。通常の処理はすべて委譲する。
  private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

  @Override
  public String resolve(HttpServletRequest request) {
    String token = delegate.resolve(request);
    if (!StringUtils.hasText(token)) {
      return token;
    }
    return decodeIfBase64(token);
  }

  /** Bearer Token が Base64 エンコード済みであれば復号して返す。 JWT でない場合や復号に失敗した場合は元の文字列をそのまま返す。 */
  private String decodeIfBase64(String token) {
    try {
      byte[] decoded = Base64.getDecoder().decode(token);
      String candidate = new String(decoded, StandardCharsets.UTF_8);
      if (candidate.chars().filter(ch -> ch == '.').count() == 2L) {
        return candidate;
      }
      return token;
    } catch (IllegalArgumentException ex) {
      return token;
    }
  }
}
