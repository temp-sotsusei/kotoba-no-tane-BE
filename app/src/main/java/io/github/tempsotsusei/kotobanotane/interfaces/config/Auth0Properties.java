package io.github.tempsotsusei.kotobanotane.interfaces.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Auth0 リソースサーバー連携で使用する設定値をまとめるレコードです。 issuer / audience / domain などの値は環境変数経由で注入されます。 */
@ConfigurationProperties(prefix = "app.auth0")
public record Auth0Properties(String issuer, String audience, String domain) {}
