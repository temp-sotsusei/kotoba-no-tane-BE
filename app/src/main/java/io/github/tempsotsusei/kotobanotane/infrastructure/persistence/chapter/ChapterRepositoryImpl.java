package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.chapter;

import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.chapter.ChapterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** ChapterRepository を JPA で実現する実装クラス。 */
@Repository
@Transactional(readOnly = true)
public class ChapterRepositoryImpl implements ChapterRepository {

  private final ChapterJpaRepository chapterJpaRepository;

  public ChapterRepositoryImpl(ChapterJpaRepository chapterJpaRepository) {
    this.chapterJpaRepository = chapterJpaRepository;
  }

  @Override
  public Optional<Chapter> findById(String chapterId) {
    return chapterJpaRepository.findById(chapterId).map(ChapterMapper::toDomain);
  }

  @Override
  public List<Chapter> findAll() {
    return chapterJpaRepository.findAll().stream().map(ChapterMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public Chapter save(Chapter chapter) {
    ChapterEntity entity =
        chapterJpaRepository
            .findById(chapter.chapterId())
            .map(
                existing ->
                    ChapterMapper.toEntityForUpdate(
                        existing,
                        chapter.storyId(),
                        chapter.chapterNum(),
                        chapter.chapterText(),
                        chapter.updatedAt()))
            .orElse(ChapterMapper.toEntity(chapter));

    ChapterEntity saved = chapterJpaRepository.save(entity);
    return ChapterMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String chapterId) {
    chapterJpaRepository.deleteById(chapterId);
  }
}
