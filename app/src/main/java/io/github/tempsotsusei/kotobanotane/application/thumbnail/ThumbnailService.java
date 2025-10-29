package io.github.tempsotsusei.kotobanotane.application.thumbnail;

import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** サムネイル周辺のユースケースを取りまとめるアプリケーションサービス。 */
@Service
public class ThumbnailService {

  /** 永続化処理を委譲するサムネイルリポジトリ。 */
  private final ThumbnailRepository thumbnailRepository;

  /** 時系列ソート可能な UUID を生成する内部サービス。 */
  private final UuidGeneratorService uuidGeneratorService;

  /**
   * DI により依存を受け取り、サービス層の処理に活用する。
   *
   * @param thumbnailRepository サムネイルリポジトリ
   * @param uuidGeneratorService UUID 生成サービス
   */
  public ThumbnailService(
      ThumbnailRepository thumbnailRepository, UuidGeneratorService uuidGeneratorService) {
    this.thumbnailRepository = thumbnailRepository;
    this.uuidGeneratorService = uuidGeneratorService;
  }

  /**
   * すべてのサムネイルを取得する。
   *
   * @return サムネイル一覧
   */
  public List<Thumbnail> findAll() {
    return thumbnailRepository.findAll();
  }

  /**
   * サムネイルを ID 指定で取得する。
   *
   * @param thumbnailId サムネイル ID
   * @return 見つかったサムネイル（存在しない場合は空）
   */
  public Optional<Thumbnail> findById(String thumbnailId) {
    return thumbnailRepository.findById(thumbnailId);
  }

  /**
   * サムネイルを新規作成する。
   *
   * @param thumbnailPath 保存するファイルパス
   * @return 作成されたサムネイル
   */
  @Transactional
  public Thumbnail create(String thumbnailPath) {
    Instant now = Instant.now();
    // UUID v7 を利用して時系列順に並べられる ID を採番する
    String thumbnailId = uuidGeneratorService.generateV7();
    Thumbnail thumbnail = new Thumbnail(thumbnailId, thumbnailPath, now, now);
    return thumbnailRepository.save(thumbnail);
  }

  /**
   * 既存サムネイルのパスを更新する。
   *
   * @param thumbnailId サムネイル ID
   * @param thumbnailPath 更新後のパス
   * @return 更新後のサムネイル（存在しない場合は空）
   */
  @Transactional
  public Optional<Thumbnail> update(String thumbnailId, String thumbnailPath) {
    return thumbnailRepository
        .findById(thumbnailId)
        // createdAt は維持しつつ、新しいパスと更新日時を反映させる
        .map(
            existing ->
                new Thumbnail(thumbnailId, thumbnailPath, existing.createdAt(), Instant.now()))
        .map(thumbnailRepository::save);
  }

  /**
   * サムネイルを削除する。
   *
   * @param thumbnailId サムネイル ID
   */
  @Transactional
  public void delete(String thumbnailId) {
    thumbnailRepository.deleteById(thumbnailId);
  }
}
