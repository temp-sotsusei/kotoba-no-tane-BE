package io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate;

/**
 * サムネイルテンプレート更新時に利用するコマンド。
 *
 * @param thumbnailIdSpecified thumbnailId がリクエストに含まれていたか
 * @param thumbnailId 更新後のサムネイル ID
 */
public record ThumbnailTemplateUpdateCommand(boolean thumbnailIdSpecified, String thumbnailId) {}
