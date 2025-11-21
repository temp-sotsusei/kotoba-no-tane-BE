package io.github.tempsotsusei.kotobanotane.application.chapter;

import java.util.List;

/**
 * 章 JSON を文字列表現へ変換した結果と、任意の単語出現位置をまとめた解析結果 DTO。
 *
 * @param plainText TipTap JSON を復元した文字列（改行を含む）
 * @param keywordsWithOffset `collectWordsWithOffset` 用の単語と文字位置リスト
 */
public record ChapterTextAnalysis(String plainText, List<KeywordPosition> keywordsWithOffset) {}
