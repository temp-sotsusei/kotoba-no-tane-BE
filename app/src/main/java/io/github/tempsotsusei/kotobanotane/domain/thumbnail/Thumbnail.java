package io.github.tempsotsusei.kotobanotane.domain.thumbnail;

import java.time.Instant;

/**
 * サムネイル情報を表現するドメインレコード。
 *
 * <p>アプリケーション層や永続化層へ値を受け渡すためのシンプルなコンテナとして利用する。
 */
public record Thumbnail(
    String thumbnailId, String thumbnailPath, Instant createdAt, Instant updatedAt) {}
