package io.github.tempsotsusei.kotobanotane.application.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

/**
 * UuidGeneratorService の実装クラス。
 *
 * <p>uuid-creator ライブラリを利用して UUID v7 を生成する。
 */
@Component
public class UuidGeneratorServiceImpl implements UuidGeneratorService {

  /**
   * 時系列ソート可能な UUID v7 を生成する。
   *
   * @return UUID v7 を表す文字列
   */
  @Override
  public String generateV7() {
    return UuidCreator.getTimeOrderedEpoch().toString();
  }
}
