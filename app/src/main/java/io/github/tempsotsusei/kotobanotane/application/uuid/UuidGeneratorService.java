package io.github.tempsotsusei.kotobanotane.application.uuid;

/**
 * UUID を生成するための内部サービス。
 *
 * <p>時系列ソートに適した UUID v7 を提供する。
 */
public interface UuidGeneratorService {

  /**
   * UUID v7 をテキスト表現で返す。
   *
   * @return UUID v7 文字列
   */
  String generateV7();
}
