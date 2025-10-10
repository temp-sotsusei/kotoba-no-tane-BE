package io.github.tempsotsusei.kotobanotane.interfaces.config;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * JWT の aud クレームに期待する Audience が含まれているかを検証するクラスです。 Auth0 側で発行した API Identifier と一致しないトークンは 401
 * になります。
 */
class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  private final String expectedAudience;

  AudienceValidator(String expectedAudience) {
    this.expectedAudience = Objects.requireNonNull(expectedAudience);
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    Collection<String> audiences = token.getAudience();
    if (audiences != null && audiences.contains(expectedAudience)) {
      return OAuth2TokenValidatorResult.success();
    }
    OAuth2Error error =
        new OAuth2Error(
            OAuth2ErrorCodes.INVALID_TOKEN,
            "JWT audience does not contain the required value",
            null);
    return OAuth2TokenValidatorResult.failure(error);
  }
}
