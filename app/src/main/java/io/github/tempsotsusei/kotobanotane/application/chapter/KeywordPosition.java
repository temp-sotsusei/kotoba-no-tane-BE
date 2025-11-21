package io.github.tempsotsusei.kotobanotane.application.chapter;

/**
 * テキスト内の単語と、その単語が何文字目に登場するかの情報を表す DTO。
 *
 * @param keyword 章 JSON から抽出した単語
 * @param position 文字列先頭からのオフセット（0 origin）
 */
public record KeywordPosition(String keyword, int position) {}
