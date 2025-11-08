package io.github.tempsotsusei.kotobanotane.application.chapter;

/**
 * 章情報を更新するときに利用するコマンドオブジェクト。
 *
 * @param storyIdSpecified storyId がリクエストに含まれていたか
 * @param storyId 更新後の storyId（null や空文字の場合はエラー）
 * @param chapterNumSpecified chapterNum が含まれていたか
 * @param chapterNum 更新後の章番号（null や 1 未満はエラー）
 * @param chapterTextSpecified chapterText が含まれていたか
 * @param chapterText 更新後の本文（null / 空文字はエラー）
 */
public record ChapterUpdateCommand(
    boolean storyIdSpecified,
    String storyId,
    boolean chapterNumSpecified,
    Integer chapterNum,
    boolean chapterTextSpecified,
    String chapterText) {}
