package io.github.tempsotsusei.kotobanotane.application.story;

/**
 * ストーリー更新で利用するコマンドオブジェクト。
 *
 * @param storyTitleSpecified タイトルがリクエストに含まれているか
 * @param storyTitle 値（null またはブランクの場合は更新しない）
 * @param thumbnailIdSpecified サムネイル ID が含まれているか
 * @param thumbnailId 更新に使用するサムネイル ID（空文字も仕様上許容）
 */
public record StoryUpdateCommand(
    boolean storyTitleSpecified,
    String storyTitle,
    boolean thumbnailIdSpecified,
    String thumbnailId) {}
