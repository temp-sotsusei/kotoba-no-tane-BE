package io.github.tempsotsusei.kotobanotane.config;

import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 非同期実行のための共通設定。 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncExecutorProperties.class)
public class AsyncConfiguration {

  private final AsyncExecutorProperties properties;

  public AsyncConfiguration(AsyncExecutorProperties properties) {
    this.properties = properties;
  }

  @Bean("llmJobExecutor")
  public Executor llmJobExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.corePoolSize());
    executor.setMaxPoolSize(properties.maxPoolSize());
    executor.setQueueCapacity(properties.queueCapacity());
    executor.setThreadNamePrefix("llm-job-");
    executor.initialize();
    return executor;
  }
}
