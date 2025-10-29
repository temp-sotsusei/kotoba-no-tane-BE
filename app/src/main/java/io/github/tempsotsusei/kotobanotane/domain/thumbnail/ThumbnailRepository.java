package io.github.tempsotsusei.kotobanotane.domain.thumbnail;

import java.util.List;
import java.util.Optional;

/**
 * サムネイル永続化に関する操作を集約するリポジトリインターフェース。
 *
 * <p>実装はインフラ層に委ね、ドメイン層からは抽象化された操作のみを利用する。
 */
public interface ThumbnailRepository {

  /**
   * サムネイルを識別子で検索する。
   *
   * @param thumbnailId サムネイル ID
   * @return 該当レコード（存在しない場合は空）
   */
  Optional<Thumbnail> findById(String thumbnailId);

  /**
   * 登録されているサムネイルをすべて取得する。
   *
   * @return サムネイルの一覧
   */
  List<Thumbnail> findAll();

  /**
   * サムネイルを新規作成または更新する。
   *
   * @param thumbnail 保存対象のドメインモデル
   * @return 保存後の状態
   */
  Thumbnail save(Thumbnail thumbnail);

  /**
   * サムネイルを識別子で削除する。
   *
   * @param thumbnailId サムネイル ID
   */
  void deleteById(String thumbnailId);
}
