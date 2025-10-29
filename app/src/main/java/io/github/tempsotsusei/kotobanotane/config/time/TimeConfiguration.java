package io.github.tempsotsusei.kotobanotane.config.time;

import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 時刻関連の共通 Bean を提供する設定クラス。 */
@Configuration
public class TimeConfiguration {

  /**
   * アプリケーション全体で利用する ZoneId。
   *
   * @param properties タイムゾーン設定
   * @return 指定された ZoneId
   */
  @Bean
  public ZoneId applicationZoneId(TimeZoneProperties properties) {
    return ZoneId.of(properties.getZone());
  }
}
