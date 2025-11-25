package io.github.tempsotsusei.kotobanotane.application.story;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 章の登録に必要な入力情報を保持する DTO。
 *
 * @param chapterNum 章番号（1 origin）
 * @param chapterJson TipTap 形式の章本文 JSON
 * @param plainText 章本文を平文化した文字列（LLM 用／バリデーション用）
 */
public record ChapterDraft(int chapterNum, JsonNode chapterJson, String plainText) {}
