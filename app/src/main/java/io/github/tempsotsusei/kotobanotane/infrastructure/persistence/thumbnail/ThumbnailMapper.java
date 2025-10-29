package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail;

import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import java.time.Instant;

/**
 * サムネイルのドメインモデルと JPA エンティティを相互変換するユーティリティ。
 *
 * <p>変換ロジックを 1 箇所に集約し、実装の重複や取り違えを防止するためのクラス。
 */
public final class ThumbnailMapper {

  /** インスタンス化を禁止するためのプライベートコンストラクタ。 */
  private ThumbnailMapper() {}

  /**
   * エンティティからドメインモデルへ変換する。
   *
   * @param entity 永続化層のエンティティ
   * @return ドメイン層で利用するレコード
   */
  public static Thumbnail toDomain(ThumbnailEntity entity) {
    return new Thumbnail(
        entity.getThumbnailId(),
        entity.getThumbnailPath(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param thumbnail ドメインレコード
   * @return JPA に保存可能なエンティティ
   */
  public static ThumbnailEntity toEntity(Thumbnail thumbnail) {
    return new ThumbnailEntity(
        thumbnail.thumbnailId(),
        thumbnail.thumbnailPath(),
        thumbnail.createdAt(),
        thumbnail.updatedAt());
  }

  /**
   * 既存エンティティを更新用に変換する。
   *
   * <p>mutable なエンティティの状態を書き換えたうえで、そのまま永続化処理へ渡す。
   *
   * @param entity 更新対象のエンティティ
   * @param newThumbnailPath 新しいパス
   * @param updatedAt 更新日時
   * @return 更新後のエンティティ
   */
  public static ThumbnailEntity toEntityForUpdate(
      ThumbnailEntity entity, String newThumbnailPath, Instant updatedAt) {
    entity.updatePath(newThumbnailPath, updatedAt);
    return entity;
  }
}
