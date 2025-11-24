package io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate;

/**
 * テンプレートとして公開するサムネイルの簡易情報。
 *
 * @param thumbnailId テンプレートに紐づくサムネイル ID
 * @param thumbnailPath テンプレート画像のパス
 */
public record ThumbnailTemplateSummary(String thumbnailId, String thumbnailPath) {}
