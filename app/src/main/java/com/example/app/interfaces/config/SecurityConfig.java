package com.example.app.interfaces.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;

/** Spring Security の中心設定を担う構成クラスです。 HTTP セキュリティや JWT デコーダー、Bearer トークン解析のビーンをまとめています。 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(Auth0Properties.class)
public class SecurityConfig {

  @Bean
  /** HTTP セキュリティの基本ポリシーを定義する。 ヘルスチェックは匿名許可、それ以外は `@PreAuthorize` などで制御する。 */
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, BearerTokenResolver bearerTokenResolver) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers("/healthz").permitAll().anyRequest().permitAll())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.bearerTokenResolver(bearerTokenResolver).jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  /** Base64 エンコードされたアクセストークンにも対応する Resolver を提供する。 */
  public BearerTokenResolver bearerTokenResolver() {
    return new Base64AwareBearerTokenResolver();
  }

  @Bean
  /** Auth0 の Issuer/Audience 設定を用いて JWT を検証するデコーダーを生成する。 */
  public JwtDecoder jwtDecoder(Auth0Properties properties) {
    Assert.hasText(properties.issuer(), "app.auth0.issuer must be provided");
    Assert.hasText(properties.audience(), "app.auth0.audience must be provided");

    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(properties.issuer());
    OAuth2TokenValidator<Jwt> validator =
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(properties.issuer()),
            new AudienceValidator(properties.audience()));
    decoder.setJwtValidator(validator);
    return decoder;
  }
}
