package io.github.tempsotsusei.kotobanotane.domain.chapter;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/**
 * 章(chapter)情報を保持するドメインオブジェクト。
 *
 * <p>story との紐付けや章番号・本文・作成/更新日時などを集約して扱う。
 */
public record Chapter(
    String chapterId,
    String storyId,
    int chapterNum,
    JsonNode chapterJson,
    Instant createdAt,
    Instant updatedAt) {}
