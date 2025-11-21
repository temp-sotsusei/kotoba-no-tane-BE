package io.github.tempsotsusei.kotobanotane.application.chapter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * TipTap 形式の章 JSON を解析し、LLM へ渡すプレーンテキストやカスタム単語の位置を抽出するサービス。
 *
 * <p>章 JSON の構造が不正な場合は 400(BAD_REQUEST) を通知する。`customWord` ノードの attrs.text
 * を本文へそのまま挿入し、`collectWordsWithOffset` 用のリストにも格納する。
 */
@Service
public class ChapterJsonTextService {

  private static final String TYPE_DOC = "doc";
  private static final String TYPE_PARAGRAPH = "paragraph";
  private static final String TYPE_TEXT = "text";
  private static final String TYPE_CUSTOM_WORD = "customWord";
  private static final String TYPE_HARD_BREAK = "hardBreak";

  /**
   * TipTap JSON を走査し、本文のプレーンテキストと `customWord` の出現位置をまとめて返す。
   *
   * @param chapterJson 章 JSON
   * @return 文字列化結果と単語位置の解析情報
   */
  public ChapterTextAnalysis analyze(JsonNode chapterJson) {
    JsonNode root = Objects.requireNonNull(chapterJson, "chapterJson must not be null");
    ensureDocType(root);

    StringBuilder builder = new StringBuilder();
    List<KeywordPosition> keywords = new ArrayList<>();
    JsonNode blocks = root.path("content");
    if (blocks.isMissingNode()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson must contain content array");
    }
    if (!blocks.isArray()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson content must be an array");
    }

    boolean firstBlock = true;
    for (JsonNode block : blocks) {
      if (!firstBlock) {
        builder.append('\n');
      }
      appendBlock(block, builder, keywords);
      firstBlock = false;
    }

    return new ChapterTextAnalysis(builder.toString(), List.copyOf(keywords));
  }

  /**
   * ルートノードが TipTap の doc であることを検証する。
   *
   * @param root ルート JSON
   */
  private void ensureDocType(JsonNode root) {
    if (!root.isObject()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson must be a JSON object of TipTap doc");
    }
    String type = root.path("type").asText();
    if (!TYPE_DOC.equals(type)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "chapterJson root type must be 'doc'");
    }
  }

  /**
   * paragraph などのブロックノードを文字列に変換する。
   *
   * @param block 現在のブロック
   * @param builder 出力先文字列
   * @param keywords 単語位置リスト
   */
  private void appendBlock(JsonNode block, StringBuilder builder, List<KeywordPosition> keywords) {
    if (block == null || block.isNull() || block.isMissingNode()) {
      return;
    }
    if (!TYPE_PARAGRAPH.equals(block.path("type").asText())) {
      appendInlineNodes(block, builder, keywords);
      return;
    }
    appendInlineNodes(block.path("content"), builder, keywords);
  }

  /**
   * インラインノードを再帰的に連結する。
   *
   * @param node 処理対象ノード
   * @param builder 出力先文字列
   * @param keywords 単語位置リスト
   */
  private void appendInlineNodes(
      JsonNode node, StringBuilder builder, List<KeywordPosition> keywords) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return;
    }
    if (node.isArray()) {
      for (JsonNode child : node) {
        appendInlineNodes(child, builder, keywords);
      }
      return;
    }

    String type = node.path("type").asText();
    switch (type) {
      case TYPE_TEXT -> builder.append(node.path("text").asText(""));
      case TYPE_CUSTOM_WORD -> appendCustomWord(node, builder, keywords);
      case TYPE_HARD_BREAK -> builder.append('\n');
      default -> appendInlineNodes(node.path("content"), builder, keywords);
    }
  }

  /**
   * `customWord` ノードを文字列に追加し、位置情報を記録する。
   *
   * @param node `customWord` ノード
   * @param builder 出力先文字列
   * @param keywords 単語位置リスト
   */
  private void appendCustomWord(
      JsonNode node, StringBuilder builder, List<KeywordPosition> keywords) {
    JsonNode attrs = node.path("attrs");
    if (!attrs.isObject()) {
      return;
    }
    String keyword = attrs.path("text").asText("");
    if (keyword.isEmpty()) {
      return;
    }
    keywords.add(new KeywordPosition(keyword, builder.length()));
    builder.append(keyword);
  }
}
