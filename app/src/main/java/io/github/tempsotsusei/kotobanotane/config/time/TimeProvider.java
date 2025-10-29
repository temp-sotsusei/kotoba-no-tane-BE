package io.github.tempsotsusei.kotobanotane.config.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * 現在時刻を取得するための共通コンポーネント。
 *
 * <p>環境変数で指定されたタイムゾーンを使用して時刻を生成する。
 */
@Component
public class TimeProvider {

  private final ZoneId zoneId;
  private final DateTimeFormatter isoFormatter;

  public TimeProvider(ZoneId zoneId) {
    this.zoneId = zoneId;
    this.isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
  }

  /**
   * ZonedDateTime を取得する。
   *
   * @return 現在時刻（指定タイムゾーン）
   */
  public ZonedDateTime now() {
    return ZonedDateTime.now(zoneId);
  }

  /**
   * Instant を取得する。ZonedDateTime から UTC Instant へ変換した値を返す。
   *
   * @return 現在の Instant
   */
  public Instant nowInstant() {
    return now().toInstant();
  }

  /**
   * Instant をアプリ共通のタイムゾーンで ISO 8601 文字列に整形する。
   *
   * @param instant 整形対象の Instant（null 可）
   * @return タイムゾーン込みの ISO 表記（null の場合は null）
   */
  public String formatIso(Instant instant) {
    return instant == null ? null : isoFormatter.format(instant);
  }
}
