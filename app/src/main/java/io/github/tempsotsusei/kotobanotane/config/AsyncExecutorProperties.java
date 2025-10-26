package io.github.tempsotsusei.kotobanotane.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async.llm")
public record AsyncExecutorProperties(int corePoolSize, int maxPoolSize, int queueCapacity) {

  private static final int DEFAULT_CORE_POOL_SIZE = 2;
  private static final int DEFAULT_MAX_POOL_SIZE = 4;
  private static final int DEFAULT_QUEUE_CAPACITY = 100;

  public AsyncExecutorProperties {
    int resolvedCore = corePoolSize <= 0 ? DEFAULT_CORE_POOL_SIZE : corePoolSize;
    int resolvedMax = maxPoolSize <= 0 ? DEFAULT_MAX_POOL_SIZE : maxPoolSize;
    int resolvedQueue = queueCapacity <= 0 ? DEFAULT_QUEUE_CAPACITY : queueCapacity;
    if (resolvedMax < resolvedCore) {
      throw new IllegalArgumentException("maxPoolSize must be >= corePoolSize");
    }
    corePoolSize = resolvedCore;
    maxPoolSize = resolvedMax;
    queueCapacity = resolvedQueue;
  }
}
