package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.story;

import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** StoryRepository を JPA で実装したクラス。 */
@Repository
@Transactional(readOnly = true)
public class StoryRepositoryImpl implements StoryRepository {

  private final StoryJpaRepository storyJpaRepository;

  public StoryRepositoryImpl(StoryJpaRepository storyJpaRepository) {
    this.storyJpaRepository = storyJpaRepository;
  }

  @Override
  public Optional<Story> findById(String storyId) {
    return storyJpaRepository.findById(storyId).map(StoryMapper::toDomain);
  }

  @Override
  public List<Story> findAll() {
    return storyJpaRepository.findAll().stream().map(StoryMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public Story save(Story story) {
    StoryEntity entity =
        storyJpaRepository
            .findById(story.storyId())
            .map(
                existing ->
                    StoryMapper.toEntityForUpdate(
                        existing, story.storyTitle(), story.thumbnailId(), story.updatedAt()))
            .orElse(StoryMapper.toEntity(story));

    StoryEntity saved = storyJpaRepository.save(entity);
    return StoryMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String storyId) {
    storyJpaRepository.deleteById(storyId);
  }
}
