package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import io.github.tempsotsusei.kotobanotane.application.llm.KeywordListsGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 開発環境向けに KeywordListsGenerationService を試すための API。 */
@RestController
@RequestMapping(path = "/api/test", produces = MediaType.APPLICATION_JSON_VALUE)
@Profile("dev")
@Validated
public class DevKeywordListsController {

  private final KeywordListsGenerationService keywordSetGenerationService;

  public DevKeywordListsController(KeywordListsGenerationService keywordSetGenerationService) {
    this.keywordSetGenerationService = keywordSetGenerationService;
  }

  /** 章テキストからキーワードセットを生成して返却する。 */
  @PostMapping("/keyword_lists")
  public List<List<String>> generate(@Valid @RequestBody KeywordSetRequest request) {
    return keywordSetGenerationService.generate(request.chapterText());
  }

  /** リクエストボディ。 */
  public record KeywordSetRequest(@NotBlank String chapterText) {}
}
