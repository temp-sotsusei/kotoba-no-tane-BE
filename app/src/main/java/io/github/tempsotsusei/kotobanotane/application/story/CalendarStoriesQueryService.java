package io.github.tempsotsusei.kotobanotane.application.story;

import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import io.github.tempsotsusei.kotobanotane.domain.story.StoryRepository;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.ThumbnailRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * カレンダー画面向けに、ユーザー別のストーリー一覧を取得するクエリサービス。
 *
 * <p>ストーリー本体とサムネイルパスを突き合わせて、プレゼンテーション層が扱いやすいサマリーへ変換する。
 */
@Service
public class CalendarStoriesQueryService {

  private final StoryRepository storyRepository;
  private final ThumbnailRepository thumbnailRepository;

  public CalendarStoriesQueryService(
      StoryRepository storyRepository, ThumbnailRepository thumbnailRepository) {
    this.storyRepository = storyRepository;
    this.thumbnailRepository = thumbnailRepository;
  }

  /**
   * 指定ユーザーのストーリーを作成日時降順で取得する。
   *
   * @param auth0UserId ユーザー ID
   * @return サムネイルパスを含むサマリー一覧
   */
  public List<CalendarStorySummary> fetchByAuth0UserId(String auth0UserId) {
    List<Story> stories = storyRepository.findAllByAuth0UserIdOrderByCreatedAtDesc(auth0UserId);

    Map<String, String> thumbnailPathMap = loadThumbnailPaths(stories);

    return stories.stream()
        .map(
            story ->
                new CalendarStorySummary(
                    story.storyId(),
                    story.storyTitle(),
                    story.thumbnailId() == null ? null : thumbnailPathMap.get(story.thumbnailId()),
                    story.createdAt()))
        .toList();
  }

  /** ストーリー一覧から必要なサムネイルを抽出し、ID→パスのマップを生成する。 */
  private Map<String, String> loadThumbnailPaths(List<Story> stories) {
    Set<String> thumbnailIds =
        stories.stream()
            .map(Story::thumbnailId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (thumbnailIds.isEmpty()) {
      return Map.of();
    }

    return thumbnailRepository.findAllByIds(thumbnailIds).stream()
        .collect(Collectors.toMap(Thumbnail::thumbnailId, Thumbnail::thumbnailPath));
  }
}
