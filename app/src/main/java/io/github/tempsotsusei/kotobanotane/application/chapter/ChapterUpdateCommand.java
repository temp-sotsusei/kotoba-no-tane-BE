package io.github.tempsotsusei.kotobanotane.application.chapter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 章情報を更新するときに利用するコマンドオブジェクト。
 *
 * @param storyIdSpecified storyId がリクエストに含まれていたか
 * @param storyId 更新後の storyId（null や空文字の場合はエラー）
 * @param chapterNumSpecified chapterNum が含まれていたか
 * @param chapterNum 更新後の章番号（null や 1 未満はエラー）
 * @param chapterJsonSpecified chapterJson が含まれていたか
 * @param chapterJson 更新後の JSON（null の場合はエラー）
 */
public record ChapterUpdateCommand(
    boolean storyIdSpecified,
    String storyId,
    boolean chapterNumSpecified,
    Integer chapterNum,
    boolean chapterJsonSpecified,
    JsonNode chapterJson) {}
