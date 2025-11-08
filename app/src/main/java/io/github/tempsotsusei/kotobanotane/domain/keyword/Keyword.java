package io.github.tempsotsusei.kotobanotane.domain.keyword;

import java.time.Instant;

/**
 * キーワード(keyword)情報を保持するドメインオブジェクト。
 *
 * <p>紐付く章 ID・キーワード文字列・表示順などを一括で表現する。
 */
public record Keyword(
    String keywordId,
    String chapterId,
    String keyword,
    int keywordPosition,
    Instant createdAt,
    Instant updatedAt) {}
