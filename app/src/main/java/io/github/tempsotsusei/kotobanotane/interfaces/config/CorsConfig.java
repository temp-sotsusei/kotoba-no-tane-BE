package io.github.tempsotsusei.kotobanotane.interfaces.config;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** CORS の許可オリジンを設定する構成クラスです。 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

  /** `/api/story` のみ CORS を許可する設定を組み立てる。 */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
    List<String> allowedOrigins =
        properties.allowedOrigins() == null
            ? List.of()
            : properties.allowedOrigins().stream().filter(StringUtils::hasText).toList();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // 物語詳細取得 API のみ許可し、他の API には適用しない。
    source.registerCorsConfiguration("/api/story", configuration);
    return source;
  }
}
