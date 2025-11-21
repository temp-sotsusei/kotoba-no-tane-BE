package io.github.tempsotsusei.kotobanotane.application.chapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ChapterJsonTextServiceTest {

  private static final String SAMPLE_JSON =
      """
			{
				"type": "doc",
				"content": [
					{
						"type": "paragraph",
						"content": [
							{
								"type": "text",
								"text": "これはテストです"
							}
						]
					},
					{
						"type": "paragraph"
					},
					{
						"type": "paragraph",
						"content": [
							{
								"type": "text",
								"text": "あ"
							}
						]
					},
					{
						"type": "paragraph",
						"content": [
							{
								"type": "text",
								"text": "い"
							},
							{
								"type": "customWord",
								"attrs": {
									"text": "ふね",
									"droppedId": 1
								}
							},
							{
								"type": "text",
								"text": "う"
							}
						]
					},
					{
						"type": "paragraph",
						"content": [
							{
								"type": "customWord",
								"attrs": {
									"text": "そら",
									"droppedId": 1
								}
							}
						]
					},
					{
						"type": "paragraph"
					},
					{
						"type": "paragraph",
						"content": [
							{
								"type": "customWord",
								"attrs": {
									"text": "ともだち",
									"droppedId": 1
								}
							},
							{
								"type": "customWord",
								"attrs": {
									"text": "ぼうけん",
									"droppedId": 1
								}
							},
							{
								"type": "text",
								"text": " "
							}
						]
					}
				]
			}
			""";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ChapterJsonTextService service = new ChapterJsonTextService();

  /** TipTap JSON 全体を渡したとき、文字列化と customWord 抽出が正しく行われることを検証する。 */
  @Test
  void analyzeConvertsJsonToPlainTextAndCollectsKeywords() throws Exception {
    JsonNode chapterJson = objectMapper.readTree(SAMPLE_JSON);

    ChapterTextAnalysis analysis = service.analyze(chapterJson);

    assertThat(analysis.plainText()).isEqualTo("これはテストです\n\nあ\nいふねう\nそら\n\nともだちぼうけん ");
    assertThat(analysis.keywordsWithOffset())
        .containsExactly(
            new KeywordPosition("ふね", analysis.plainText().indexOf("ふね")),
            new KeywordPosition("そら", analysis.plainText().indexOf("そら")),
            new KeywordPosition("ともだち", analysis.plainText().indexOf("ともだち")),
            new KeywordPosition("ぼうけん", analysis.plainText().indexOf("ぼうけん")));
  }

  /** ルート type が doc 以外の場合に 400 エラーへ変換されることを確認する。 */
  @Test
  void analyzeRejectsJsonWithoutDocType() {
    ObjectNode invalid = objectMapper.createObjectNode();
    invalid.put("type", "paragraph");

    assertThatThrownBy(() -> service.analyze(invalid))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("doc");
  }
}
