package io.github.tempsotsusei.kotobanotane.interfaces.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CORS 設定値をまとめるレコードです。 allowed-origins はカンマ区切りを想定します。 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {}
