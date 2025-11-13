package io.github.tempsotsusei.kotobanotane.application.story;

import java.time.Instant;

/** カレンダービュー向けのストーリーサマリーを表すレコード。 */
public record CalendarStorySummary(
    String storyId, String storyTitle, String thumbnailPath, Instant createdAt) {}
