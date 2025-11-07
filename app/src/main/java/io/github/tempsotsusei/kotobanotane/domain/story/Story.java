package io.github.tempsotsusei.kotobanotane.domain.story;

import java.time.Instant;

/**
 * ストーリー情報を表すドメインモデル。
 *
 * <p>ユーザー ID・タイトル・サムネイル ID を保持し、アプリケーション層とのデータ授受に利用する。
 */
public record Story(
    String storyId,
    String auth0UserId,
    String storyTitle,
    String thumbnailId,
    Instant createdAt,
    Instant updatedAt) {}
