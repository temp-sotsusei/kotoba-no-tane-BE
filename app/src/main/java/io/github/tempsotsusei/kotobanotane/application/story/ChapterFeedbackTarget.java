package io.github.tempsotsusei.kotobanotane.application.story;

/**
 * フィードバック生成ジョブに渡す章情報。
 *
 * @param chapterId 生成済み章の ID
 * @param plainText LLM へ渡す平文化済み本文
 */
public record ChapterFeedbackTarget(String chapterId, String plainText) {}
