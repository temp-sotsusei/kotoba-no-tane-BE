package io.github.tempsotsusei.kotobanotane.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** OpenAI API の接続設定をまとめたコンフィグクラス。 */
@ConfigurationProperties(prefix = "app.openai")
public record OpenAiProperties(
    String openaiBaseUrl,
    String openaiApiKey,
    String openaiModel,
    int openaiTimeout,
    int defaultMaxOutputTokens,
    int maxAttempts) {}
