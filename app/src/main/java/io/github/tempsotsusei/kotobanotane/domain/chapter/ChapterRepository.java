package io.github.tempsotsusei.kotobanotane.domain.chapter;

import java.util.List;
import java.util.Optional;

/** chapter テーブルへアクセスするための抽象リポジトリ。 */
public interface ChapterRepository {

  Optional<Chapter> findById(String chapterId);

  List<Chapter> findAll();

  /** 指定した Story に紐づく章を章番号順に取得する。 */
  List<Chapter> findAllByStoryIdOrderByChapterNum(String storyId);

  Chapter save(Chapter chapter);

  void deleteById(String chapterId);
}
