package io.github.tempsotsusei.kotobanotane.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** OpenAI 関連の設定を読み込むためのコンフィグ。 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfiguration {}
