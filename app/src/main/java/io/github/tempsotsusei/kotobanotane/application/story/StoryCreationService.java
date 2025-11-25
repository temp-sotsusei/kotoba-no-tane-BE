package io.github.tempsotsusei.kotobanotane.application.story;

import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterService;
import io.github.tempsotsusei.kotobanotane.application.uuid.UuidGeneratorService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * Story と Chapter を一括作成するアプリケーションサービス。
 *
 * <p>バリデーション済みの入力を受け取り、すべての章を登録できない場合はロールバックする。
 */
@Service
public class StoryCreationService {

  private static final int MAX_TITLE_LENGTH = 15;
  private static final int MAX_CHAPTERS = 5;

  private final StoryRepository storyRepository;
  private final ChapterService chapterService;
  private final ThumbnailRepository thumbnailRepository;
  private final UuidGeneratorService uuidGeneratorService;
  private final TimeProvider timeProvider;

  public StoryCreationService(
      StoryRepository storyRepository,
      ChapterService chapterService,
      ThumbnailRepository thumbnailRepository,
      UuidGeneratorService uuidGeneratorService,
      TimeProvider timeProvider) {
    this.storyRepository = storyRepository;
    this.chapterService = chapterService;
    this.thumbnailRepository = thumbnailRepository;
    this.uuidGeneratorService = uuidGeneratorService;
    this.timeProvider = timeProvider;
  }

  /**
   * Story と Chapter をトランザクションで一括作成する。
   *
   * @param auth0UserId 作成者 Auth0 ID
   * @param storyTitle タイトル
   * @param thumbnailId サムネイル ID（任意）
   * @param drafts バリデーション済みの章入力（平文化テキスト含む）
   * @return Story ID とフィードバック生成対象の章情報
   */
  @Transactional
  public StoryCreationResult createStoryWithChapters(
      String auth0UserId, String storyTitle, String thumbnailId, List<ChapterDraft> drafts) {
    validateTitle(storyTitle);
    validateChapterCount(drafts);

    String thumbnailToUse = validateThumbnail(thumbnailId);
    Instant now = timeProvider.nowInstant();

    Story story =
        new Story(
            uuidGeneratorService.generateV7(), auth0UserId, storyTitle, thumbnailToUse, now, now);
    Story savedStory = storyRepository.save(story);

    List<ChapterFeedbackTarget> feedbackTargets = new ArrayList<>();
    drafts.stream()
        .sorted(Comparator.comparingInt(ChapterDraft::chapterNum))
        .forEach(
            draft -> {
              Chapter created =
                  chapterService.create(
                      savedStory.storyId(), draft.chapterNum(), draft.chapterJson());
              feedbackTargets.add(
                  new ChapterFeedbackTarget(created.chapterId(), draft.plainText()));
            });

    return new StoryCreationResult(savedStory.storyId(), feedbackTargets);
  }

  /** タイトルが空でなく、最大長を超えないことを検証する。 */
  private void validateTitle(String storyTitle) {
    if (!StringUtils.hasText(storyTitle)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storyTitle must not be blank");
    }
    if (storyTitle.length() > MAX_TITLE_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "storyTitle must be 15 characters or less");
    }
  }

  /** 章件数が 1〜MAX_CHAPTERS であることを検証する。 */
  private void validateChapterCount(List<ChapterDraft> drafts) {
    if (drafts == null || drafts.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapters must not be empty");
    }
    if (drafts.size() > MAX_CHAPTERS) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapters must not exceed 5 items");
    }
  }

  /** サムネイルが指定されている場合のみ存在チェックを行う。 */
  private String validateThumbnail(String thumbnailId) {
    if (!StringUtils.hasText(thumbnailId)) {
      return null;
    }
    boolean exists = thumbnailRepository.findById(thumbnailId).isPresent();
    if (!exists) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "thumbnailId does not exist: " + thumbnailId);
    }
    return thumbnailId;
  }
}
