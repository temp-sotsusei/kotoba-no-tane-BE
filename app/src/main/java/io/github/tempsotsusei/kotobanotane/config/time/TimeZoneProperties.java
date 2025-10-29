package io.github.tempsotsusei.kotobanotane.config.time;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** アプリケーションで利用するタイムゾーンを保持するプロパティ設定。 */
@Component
@ConfigurationProperties(prefix = "app.time")
public class TimeZoneProperties {

  /** 使用するタイムゾーン ID（例: Asia/Tokyo）。 */
  private String zone = "Asia/Tokyo";

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }
}
