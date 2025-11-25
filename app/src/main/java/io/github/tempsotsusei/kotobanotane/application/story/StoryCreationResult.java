package io.github.tempsotsusei.kotobanotane.application.story;

import java.util.List;

/**
 * Story と Chapter の登録結果をまとめる DTO。
 *
 * @param storyId 登録した Story の ID
 * @param feedbackTargets フィードバック生成に利用する章情報
 */
public record StoryCreationResult(String storyId, List<ChapterFeedbackTarget> feedbackTargets) {}
