package com.example.app.interfaces.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class Base64AwareBearerTokenResolverTest {

  private final Base64AwareBearerTokenResolver resolver = new Base64AwareBearerTokenResolver();

  @Test
  /** Base64 エンコードされたトークンが元の JWT 文字列に復元されることを検証します。 */
  void decodeBase64EncodedToken() {
    String originalToken = "header.payload.signature";
    String encoded =
        Base64.getEncoder().encodeToString(originalToken.getBytes(StandardCharsets.UTF_8));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + encoded);

    assertThat(resolver.resolve(request)).isEqualTo(originalToken);
  }

  @Test
  /** 既に平文の JWT が渡された場合は変更されないことを検証します。 */
  void keepPlainTokenAsIs() {
    String token = "header.payload.signature";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertThat(resolver.resolve(request)).isEqualTo(token);
  }

  @Test
  /** Authorization ヘッダーが存在しない場合は null を返すことを検証します。 */
  void returnNullWhenTokenIsAbsent() {
    HttpServletRequest request = new MockHttpServletRequest();
    assertThat(resolver.resolve(request)).isNull();
  }
}
