package io.github.tempsotsusei.kotobanotane.domain.chapter;

import java.util.List;
import java.util.Optional;

/** chapter テーブルへアクセスするための抽象リポジトリ。 */
public interface ChapterRepository {

  Optional<Chapter> findById(String chapterId);

  List<Chapter> findAll();

  Chapter save(Chapter chapter);

  void deleteById(String chapterId);
}
