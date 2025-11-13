package io.github.tempsotsusei.kotobanotane.domain.story;

import java.util.List;
import java.util.Optional;

/** ストーリー永続化へアクセスするための抽象リポジトリ。 */
public interface StoryRepository {

  Optional<Story> findById(String storyId);

  List<Story> findAll();

  /**
   * 指定ユーザーが作成したストーリーを作成日時の降順で取得する。
   *
   * @param auth0UserId ユーザー ID
   * @return 作成日時が新しい順に並んだストーリー一覧
   */
  List<Story> findAllByAuth0UserIdOrderByCreatedAtDesc(String auth0UserId);

  Story save(Story story);

  void deleteById(String storyId);
}
