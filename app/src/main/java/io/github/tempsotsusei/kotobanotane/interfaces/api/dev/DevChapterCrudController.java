package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterService;
import io.github.tempsotsusei.kotobanotane.application.chapter.ChapterUpdateCommand;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.chapter.Chapter;
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
 * 章(chapter)向けの開発用 CRUD API を提供するコントローラ。
 *
 * <p>stories CRUD と同様、dev プロファイル限定で動作させ、手動確認や Postman から利用する。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevChapterCrudController {

  private final ChapterService chapterService;
  private final TimeProvider timeProvider;

  public DevChapterCrudController(ChapterService chapterService, TimeProvider timeProvider) {
    this.chapterService = chapterService;
    this.timeProvider = timeProvider;
  }

  /** 章一覧を返す。 */
  @GetMapping("/chapters")
  @PreAuthorize("permitAll()")
  public List<ChapterResponse> list() {
    return chapterService.findAll().stream().map(this::toResponse).toList();
  }

  /** 指定した章を取得する。 */
  @GetMapping("/chapter/{chapterId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ChapterResponse> get(@PathVariable String chapterId) {
    return chapterService
        .findById(chapterId)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** 章を新規作成する。 */
  @PostMapping("/chapter")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ChapterResponse> create(@Valid @RequestBody ChapterCreateRequest request) {
    Chapter created =
        chapterService.create(request.storyId(), request.chapterNum(), request.chapterText());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  /** 章の内容を更新する。 */
  @PutMapping("/chapter/{chapterId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ChapterResponse> update(
      @PathVariable String chapterId, @Valid @RequestBody ChapterUpdateRequest request) {
    if (request.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    ChapterUpdateCommand command =
        new ChapterUpdateCommand(
            request.storyIdSpecified(),
            request.storyId(),
            request.chapterNumSpecified(),
            request.chapterNum(),
            request.chapterTextSpecified(),
            request.chapterText());

    return chapterService
        .update(chapterId, command)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** 章を削除する。 */
  @DeleteMapping("/chapter/{chapterId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String chapterId) {
    chapterService.delete(chapterId);
    return ResponseEntity.noContent().build();
  }

  private ChapterResponse toResponse(Chapter chapter) {
    return new ChapterResponse(
        chapter.chapterId(),
        chapter.storyId(),
        chapter.chapterNum(),
        chapter.chapterText(),
        timeProvider.formatIso(chapter.createdAt()),
        timeProvider.formatIso(chapter.updatedAt()));
  }

  /** 章作成リクエスト。 */
  public record ChapterCreateRequest(
      @NotBlank String storyId, @Min(1) int chapterNum, @NotBlank String chapterText) {}

  /** 章更新リクエスト。 */
  public static class ChapterUpdateRequest {

    private String storyId;
    private boolean storyIdSpecified;
    private Integer chapterNum;
    private boolean chapterNumSpecified;
    private String chapterText;
    private boolean chapterTextSpecified;

    @JsonProperty("storyId")
    public void setStoryId(String storyId) {
      this.storyId = storyId;
      this.storyIdSpecified = true;
    }

    @JsonProperty("chapterNum")
    public void setChapterNum(Integer chapterNum) {
      this.chapterNum = chapterNum;
      this.chapterNumSpecified = true;
    }

    @JsonProperty("chapterText")
    public void setChapterText(String chapterText) {
      this.chapterText = chapterText;
      this.chapterTextSpecified = true;
    }

    public String storyId() {
      return storyId;
    }

    public Integer chapterNum() {
      return chapterNum;
    }

    public String chapterText() {
      return chapterText;
    }

    public boolean storyIdSpecified() {
      return storyIdSpecified;
    }

    public boolean chapterNumSpecified() {
      return chapterNumSpecified;
    }

    public boolean chapterTextSpecified() {
      return chapterTextSpecified;
    }

    boolean isEmpty() {
      return !storyIdSpecified && !chapterNumSpecified && !chapterTextSpecified;
    }
  }

  /** 章レスポンス DTO。 */
  public record ChapterResponse(
      String chapterId,
      String storyId,
      int chapterNum,
      String chapterText,
      String createdAt,
      String updatedAt) {}
}
