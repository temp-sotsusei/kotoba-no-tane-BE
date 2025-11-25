package io.github.tempsotsusei.kotobanotane.application.llm;

/** LLM が返す文法修正結果を保持する DTO。 */
public record FeedbackItem(String original, String corrected, String reason) {}
