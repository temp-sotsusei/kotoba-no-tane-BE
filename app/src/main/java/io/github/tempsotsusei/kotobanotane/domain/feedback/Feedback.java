package io.github.tempsotsusei.kotobanotane.domain.feedback;

import java.time.Instant;

/**
 * フィードバック(feedback)情報を表すドメインオブジェクト。
 *
 * <p>紐付く章 ID・本文・作成/更新日時などを集約する。
 */
public record Feedback(
    String feedbackId, String chapterId, String feedback, Instant createdAt, Instant updatedAt) {}
