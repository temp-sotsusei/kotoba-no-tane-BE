package io.github.tempsotsusei.kotobanotane.domain.story;

import java.util.List;
import java.util.Optional;

/** ストーリー永続化へアクセスするための抽象リポジトリ。 */
public interface StoryRepository {

  Optional<Story> findById(String storyId);

  List<Story> findAll();

  Story save(Story story);

  void deleteById(String storyId);
}
