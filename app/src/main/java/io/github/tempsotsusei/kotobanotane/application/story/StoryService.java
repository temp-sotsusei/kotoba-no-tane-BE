package io.github.tempsotsusei.kotobanotane.application.story;

import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** ストーリー周辺のユースケースを提供するアプリケーションサービス。 */
@Service
public class StoryService {

  private final StoryRepository storyRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;
  private final ThumbnailRepository thumbnailRepository;

  public StoryService(
      StoryRepository storyRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider,
      ThumbnailRepository thumbnailRepository) {
    this.storyRepository = storyRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
    this.thumbnailRepository = thumbnailRepository;
  }

  /**
   * すべてのストーリーを取得する。
   *
   * @return ストーリー一覧
   */
  public List<Story> findAll() {
    return storyRepository.findAll();
  }

  /**
   * ストーリーを ID 指定で取得する。
   *
   * @param storyId ストーリー ID
   * @return 見つかったストーリー（存在しない場合は空）
   */
  public Optional<Story> findById(String storyId) {
    return storyRepository.findById(storyId);
  }

  /**
   * ストーリーを新規作成する。
   *
   * @param auth0UserId 作成者の Auth0 ID
   * @param storyTitle ストーリータイトル
   * @param thumbnailId 紐付けるサムネイル ID（任意）
   * @return 作成されたストーリー
   */
  @Transactional
  public Story create(String auth0UserId, String storyTitle, String thumbnailId) {
    Instant now = timeProvider.nowInstant();
    Story story =
        new Story(
            uuidGeneratorService.generateV7(), auth0UserId, storyTitle, thumbnailId, now, now);
    return storyRepository.save(story);
  }

  /**
   * 既存ストーリーの内容を更新する。
   *
   * @param storyId ストーリー ID
   * @param storyTitle 更新後のタイトル
   * @param thumbnailId 更新後のサムネイル ID（任意）
   * @return 更新後のストーリー（存在しない場合は空）
   */
  @Transactional
  public Optional<Story> update(String storyId, StoryUpdateCommand command) {
    Instant updatedAt = timeProvider.nowInstant();
    return storyRepository
        .findById(storyId)
        .map(
            existing -> {
              String nextTitle = existing.storyTitle();
              if (command.storyTitleSpecified()) {
                String candidate = command.storyTitle();
                if (candidate != null && !candidate.isBlank()) {
                  nextTitle = candidate;
                }
              }

              String nextThumbnail = existing.thumbnailId();
              if (command.thumbnailIdSpecified()) {
                String candidate = command.thumbnailId();
                if (candidate == null || candidate.isBlank()) {
                  nextThumbnail = null;
                } else {
                  boolean exists = thumbnailRepository.findById(candidate).isPresent();
                  if (!exists) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "thumbnailId does not exist: " + candidate);
                  }
                  nextThumbnail = candidate;
                }
              }

              return new Story(
                  storyId,
                  existing.auth0UserId(),
                  nextTitle,
                  nextThumbnail,
                  existing.createdAt(),
                  updatedAt);
            })
        .map(storyRepository::save);
  }

  /**
   * ストーリーを削除する。
   *
   * @param storyId ストーリー ID
   */
  @Transactional
  public void delete(String storyId) {
    storyRepository.deleteById(storyId);
  }
}
