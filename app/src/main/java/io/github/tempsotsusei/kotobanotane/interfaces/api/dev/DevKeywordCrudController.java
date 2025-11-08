package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tempsotsusei.kotobanotane.application.keyword.KeywordService;
import io.github.tempsotsusei.kotobanotane.application.keyword.KeywordUpdateCommand;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.keyword.Keyword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * keyword テーブル向けの開発用 CRUD API を提供するコントローラ。
 *
 * <p>dev プロファイルでのみ有効とし、手動検証や Postman から利用する。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevKeywordCrudController {

  private final KeywordService keywordService;
  private final TimeProvider timeProvider;

  public DevKeywordCrudController(KeywordService keywordService, TimeProvider timeProvider) {
    this.keywordService = keywordService;
    this.timeProvider = timeProvider;
  }

  /** キーワード一覧を返す。 */
  @GetMapping("/keywords")
  @PreAuthorize("permitAll()")
  public List<KeywordResponse> list() {
    return keywordService.findAll().stream().map(this::toResponse).toList();
  }

  /** キーワード 1 件を取得する。 */
  @GetMapping("/keyword/{keywordId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<KeywordResponse> get(@PathVariable String keywordId) {
    return keywordService
        .findById(keywordId)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** キーワードを新規作成する。 */
  @PostMapping("/keyword")
  @PreAuthorize("permitAll()")
  public ResponseEntity<KeywordResponse> create(@Valid @RequestBody KeywordCreateRequest request) {
    Keyword created =
        keywordService.create(
            request.chapterId(), request.keyword(), request.keywordPosition());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  /** キーワードを更新する。 */
  @PutMapping("/keyword/{keywordId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<KeywordResponse> update(
      @PathVariable String keywordId, @Valid @RequestBody KeywordUpdateRequest request) {
    if (request.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    KeywordUpdateCommand command =
        new KeywordUpdateCommand(
            request.chapterIdSpecified(),
            request.chapterId(),
            request.keywordSpecified(),
            request.keyword(),
            request.keywordPositionSpecified(),
            request.keywordPosition());

    return keywordService
        .update(keywordId, command)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** キーワードを削除する。 */
  @DeleteMapping("/keyword/{keywordId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String keywordId) {
    keywordService.delete(keywordId);
    return ResponseEntity.noContent().build();
  }

  private KeywordResponse toResponse(Keyword keyword) {
    return new KeywordResponse(
        keyword.keywordId(),
        keyword.chapterId(),
        keyword.keyword(),
        keyword.keywordPosition(),
        timeProvider.formatIso(keyword.createdAt()),
        timeProvider.formatIso(keyword.updatedAt()));
  }

  /** キーワード作成リクエスト。 */
  public record KeywordCreateRequest(
      @NotBlank String chapterId, @NotBlank String keyword, @Min(1) int keywordPosition) {}

  /** キーワード更新リクエスト。 */
  public static class KeywordUpdateRequest {

    private String chapterId;
    private boolean chapterIdSpecified;
    private String keyword;
    private boolean keywordSpecified;
    private Integer keywordPosition;
    private boolean keywordPositionSpecified;

    @JsonProperty("chapterId")
    public void setChapterId(String chapterId) {
      this.chapterId = chapterId;
      this.chapterIdSpecified = true;
    }

    @JsonProperty("keyword")
    public void setKeyword(String keyword) {
      this.keyword = keyword;
      this.keywordSpecified = true;
    }

    @JsonProperty("keywordPosition")
    public void setKeywordPosition(Integer keywordPosition) {
      this.keywordPosition = keywordPosition;
      this.keywordPositionSpecified = true;
    }

    public String chapterId() {
      return chapterId;
    }

    public String keyword() {
      return keyword;
    }

    public Integer keywordPosition() {
      return keywordPosition;
    }

    public boolean chapterIdSpecified() {
      return chapterIdSpecified;
    }

    public boolean keywordSpecified() {
      return keywordSpecified;
    }

    public boolean keywordPositionSpecified() {
      return keywordPositionSpecified;
    }

    boolean isEmpty() {
      return !chapterIdSpecified && !keywordSpecified && !keywordPositionSpecified;
    }
  }

  /** キーワードレスポンス DTO。 */
  public record KeywordResponse(
      String keywordId,
      String chapterId,
      String keyword,
      int keywordPosition,
      String createdAt,
      String updatedAt) {}
}
