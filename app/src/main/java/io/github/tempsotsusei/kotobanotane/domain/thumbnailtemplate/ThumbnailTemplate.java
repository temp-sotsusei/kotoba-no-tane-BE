package io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate;

import java.time.Instant;

/**
 * サムネイルテンプレート情報を保持するドメインオブジェクト。
 *
 * <p>テンプレート自体の ID と紐付くサムネイル ID、作成・更新日時を取り扱う。
 */
public record ThumbnailTemplate(
    String thumbnailTemplateId, String thumbnailId, Instant createdAt, Instant updatedAt) {}
