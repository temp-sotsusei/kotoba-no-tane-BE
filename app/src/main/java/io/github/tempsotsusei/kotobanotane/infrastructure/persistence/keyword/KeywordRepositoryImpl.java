package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.keyword;

import io.github.tempsotsusei.kotobanotane.domain.keyword.Keyword;
import io.github.tempsotsusei.kotobanotane.domain.keyword.KeywordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** KeywordRepository を JPA で実現する実装クラス。 */
@Repository
@Transactional(readOnly = true)
public class KeywordRepositoryImpl implements KeywordRepository {

  private final KeywordJpaRepository keywordJpaRepository;

  public KeywordRepositoryImpl(KeywordJpaRepository keywordJpaRepository) {
    this.keywordJpaRepository = keywordJpaRepository;
  }

  @Override
  public Optional<Keyword> findById(String keywordId) {
    return keywordJpaRepository.findById(keywordId).map(KeywordMapper::toDomain);
  }

  @Override
  public List<Keyword> findAll() {
    return keywordJpaRepository.findAll().stream().map(KeywordMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public Keyword save(Keyword keyword) {
    KeywordEntity entity =
        keywordJpaRepository
            .findById(keyword.keywordId())
            .map(
                existing ->
                    KeywordMapper.toEntityForUpdate(
                        existing,
                        keyword.chapterId(),
                        keyword.keyword(),
                        keyword.keywordPosition(),
                        keyword.updatedAt()))
            .orElse(KeywordMapper.toEntity(keyword));

    KeywordEntity saved = keywordJpaRepository.save(entity);
    return KeywordMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String keywordId) {
    keywordJpaRepository.deleteById(keywordId);
  }
}
