package io.github.tempsotsusei.kotobanotane.application.keyword;

/**
 * キーワード更新時の差分情報を保持するコマンド。
 *
 * @param chapterIdSpecified chapterId がリクエストに含まれていたか
 * @param chapterId 更新後の章 ID
 * @param keywordSpecified keyword が含まれていたか
 * @param keyword 更新後のキーワード文字列
 * @param keywordPositionSpecified keywordPosition が含まれていたか
 * @param keywordPosition 更新後の表示順
 */
public record KeywordUpdateCommand(
    boolean chapterIdSpecified,
    String chapterId,
    boolean keywordSpecified,
    String keyword,
    boolean keywordPositionSpecified,
    Integer keywordPosition) {}
