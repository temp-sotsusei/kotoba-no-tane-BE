package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.feedback;

import io.github.tempsotsusei.kotobanotane.domain.feedback.Feedback;
import io.github.tempsotsusei.kotobanotane.domain.feedback.FeedbackRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** FeedbackRepository を JPA で実現する実装クラス。 */
@Repository
@Transactional(readOnly = true)
public class FeedbackRepositoryImpl implements FeedbackRepository {

  private final FeedbackJpaRepository feedbackJpaRepository;

  public FeedbackRepositoryImpl(FeedbackJpaRepository feedbackJpaRepository) {
    this.feedbackJpaRepository = feedbackJpaRepository;
  }

  @Override
  public Optional<Feedback> findById(String feedbackId) {
    return feedbackJpaRepository.findById(feedbackId).map(FeedbackMapper::toDomain);
  }

  @Override
  public List<Feedback> findAll() {
    return feedbackJpaRepository.findAll().stream().map(FeedbackMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public Feedback save(Feedback feedback) {
    FeedbackEntity entity =
        feedbackJpaRepository
            .findById(feedback.feedbackId())
            .map(
                existing ->
                    FeedbackMapper.toEntityForUpdate(
                        existing, feedback.chapterId(), feedback.feedback(), feedback.updatedAt()))
            .orElse(FeedbackMapper.toEntity(feedback));

    FeedbackEntity saved = feedbackJpaRepository.save(entity);
    return FeedbackMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(String feedbackId) {
    feedbackJpaRepository.deleteById(feedbackId);
  }
}
