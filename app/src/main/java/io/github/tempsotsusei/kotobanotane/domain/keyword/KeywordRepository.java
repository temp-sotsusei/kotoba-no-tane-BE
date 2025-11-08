package io.github.tempsotsusei.kotobanotane.domain.keyword;

import java.util.List;
import java.util.Optional;

/** keywords テーブルにアクセスするためのリポジトリアブストラクション。 */
public interface KeywordRepository {

  Optional<Keyword> findById(String keywordId);

  List<Keyword> findAll();

  Keyword save(Keyword keyword);

  void deleteById(String keywordId);
}
