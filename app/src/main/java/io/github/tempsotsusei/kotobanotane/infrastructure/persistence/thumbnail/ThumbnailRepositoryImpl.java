package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.thumbnail;

import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** ThumbnailRepository を Spring Data JPA で実装したクラス。 */
@Repository
@Transactional(readOnly = true)
public class ThumbnailRepositoryImpl implements ThumbnailRepository {

  private final ThumbnailJpaRepository thumbnailJpaRepository;

  public ThumbnailRepositoryImpl(ThumbnailJpaRepository thumbnailJpaRepository) {
    this.thumbnailJpaRepository = thumbnailJpaRepository;
  }

  /** ID 指定でサムネイルを検索する。 */
  @Override
  public Optional<Thumbnail> findById(String thumbnailId) {
    return thumbnailJpaRepository.findById(thumbnailId).map(ThumbnailMapper::toDomain);
  }

  /** テーブル内のサムネイルをすべて読み出す。 */
  @Override
  public List<Thumbnail> findAll() {
    return thumbnailJpaRepository.findAll().stream().map(ThumbnailMapper::toDomain).toList();
  }

  /**
   * サムネイルを永続化する。
   *
   * <p>既存レコードがあれば値を更新し、存在しなければ新規作成する。
   */
  @Override
  @Transactional
  public Thumbnail save(Thumbnail thumbnail) {
    ThumbnailEntity entity =
        thumbnailJpaRepository
            .findById(thumbnail.thumbnailId())
            .map(
                existing ->
                    ThumbnailMapper.toEntityForUpdate(
                        existing, thumbnail.thumbnailPath(), thumbnail.updatedAt()))
            .orElse(ThumbnailMapper.toEntity(thumbnail));

    // JPA リポジトリで保存し、ドメインモデルへ変換して返却する
    ThumbnailEntity saved = thumbnailJpaRepository.save(entity);
    return ThumbnailMapper.toDomain(saved);
  }

  /** サムネイルを識別子で削除する。 */
  @Override
  @Transactional
  public void deleteById(String thumbnailId) {
    thumbnailJpaRepository.deleteById(thumbnailId);
  }
}
