package io.github.tempsotsusei.kotobanotane.application.feedback;

/**
 * フィードバック更新時の差分情報を保持するコマンド。
 *
 * @param chapterIdSpecified chapterId がリクエストに含まれていたか
 * @param chapterId 更新後の章 ID
 * @param feedbackSpecified feedback が含まれていたか
 * @param feedback 更新後の本文
 */
public record FeedbackUpdateCommand(
    boolean chapterIdSpecified, String chapterId, boolean feedbackSpecified, String feedback) {}
