package io.github.tempsotsusei.kotobanotane.application.story;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterJsonTextService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterTextAnalysis;
import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * `/api/story/chapter/next` から呼び出され、章 JSON を整形して LLM に渡すアプリケーションサービス。
 *
 * <p>JSON をプレーンテキストに変換し、文字数（0 &lt;= length &lt;= 200）を検証した上で `KeywordListsGenerationService` を呼び出す。
 */
@Service
public class StoryChapterNextService {

  private static final int MAX_TEXT_LENGTH = 200;

  private final ChapterJsonTextService chapterJsonTextService;
  private final KeywordListsGenerationService keywordListsGenerationService;

  /** 章 JSON の解析サービスと LLM 呼び出しサービスを受け取る。 */
  public StoryChapterNextService(
      ChapterJsonTextService chapterJsonTextService,
      KeywordListsGenerationService keywordListsGenerationService) {
    this.chapterJsonTextService = chapterJsonTextService;
    this.keywordListsGenerationService = keywordListsGenerationService;
  }

  /**
   * TipTap JSON を受け取り、文字列化→バリデーション→LLM 呼び出しまでを実行する。
   *
   * @param chapterJson フロントエンドから渡される章 JSON
   * @return LLM が生成した 4 語 × 3 セットのリスト
   */
  public List<List<String>> generateNextChapterKeywords(JsonNode chapterJson) {
    ChapterTextAnalysis analysis = chapterJsonTextService.analyze(chapterJson);
    String plainText = analysis.plainText();
    validateTextLength(plainText);
    return keywordListsGenerationService.generate(plainText);
  }

  /**
   * 文字列化した本文が 1〜200 文字の範囲に収まっているか検証する。
   *
   * @param text 章本文
   */
  private void validateTextLength(String text) {
    if (!StringUtils.hasText(text)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson text length must be between 1 and 200");
    }
    if (text.length() > MAX_TEXT_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson text length must be between 1 and 200");
    }
  }
}
