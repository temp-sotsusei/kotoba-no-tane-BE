package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tempsotsusei.kotobanotane.application.story.StoryService;
import io.github.tempsotsusei.kotobanotane.application.story.StoryUpdateCommand;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.story.Story;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 * ストーリー向けの開発用 CRUD API を提供するコントローラ。
 *
 * <p>ユーザー CRUD と同じ命名規則でルーティングし、動作確認を容易にする。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevStoryCrudController {

  private final StoryService storyService;
  private final TimeProvider timeProvider;

  public DevStoryCrudController(StoryService storyService, TimeProvider timeProvider) {
    this.storyService = storyService;
    this.timeProvider = timeProvider;
  }

  /** ストーリーを全件取得する。 */
  @GetMapping("/stories")
  @PreAuthorize("permitAll()")
  public List<StoryResponse> list() {
    return storyService.findAll().stream().map(this::toResponse).toList();
  }

  /** ストーリーを 1 件取得する。 */
  @GetMapping("/story/{storyId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<StoryResponse> get(@PathVariable String storyId) {
    return storyService
        .findById(storyId)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** ストーリーを新規作成する。 */
  @PostMapping("/story")
  @PreAuthorize("permitAll()")
  public ResponseEntity<StoryResponse> create(@Valid @RequestBody StoryCreateRequest request) {
    Story created =
        storyService.create(request.auth0Id(), request.storyTitle(), request.thumbnailId());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  /** 既存ストーリーを更新する。 */
  @PutMapping("/story/{storyId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<StoryResponse> update(
      @PathVariable String storyId, @Valid @RequestBody StoryUpdateRequest request) {
    if (request.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    StoryUpdateCommand command =
        new StoryUpdateCommand(
            request.storyTitleSpecified(),
            request.storyTitle(),
            request.thumbnailIdSpecified(),
            request.thumbnailId());

    return storyService
        .update(storyId, command)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** ストーリーを削除する。 */
  @DeleteMapping("/story/{storyId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String storyId) {
    storyService.delete(storyId);
    return ResponseEntity.noContent().build();
  }

  private StoryResponse toResponse(Story story) {
    return new StoryResponse(
        story.storyId(),
        story.auth0UserId(),
        story.storyTitle(),
        story.thumbnailId(),
        timeProvider.formatIso(story.createdAt()),
        timeProvider.formatIso(story.updatedAt()));
  }

  /** ストーリー作成リクエスト。 */
  public record StoryCreateRequest(
      @NotBlank String auth0Id,
      @NotBlank @Size(max = 255) String storyTitle,
      @Size(max = 255) String thumbnailId) {}

  /** ストーリー更新リクエスト。 */
  public static class StoryUpdateRequest {

    @Size(max = 255)
    private String storyTitle;

    private boolean storyTitleSpecified;

    @Size(max = 255)
    private String thumbnailId;

    private boolean thumbnailIdSpecified;

    @JsonProperty("storyTitle")
    public void setStoryTitle(String storyTitle) {
      this.storyTitle = storyTitle;
      this.storyTitleSpecified = true;
    }

    @JsonProperty("thumbnailId")
    public void setThumbnailId(String thumbnailId) {
      this.thumbnailId = thumbnailId;
      this.thumbnailIdSpecified = true;
    }

    public String storyTitle() {
      return storyTitle;
    }

    public String thumbnailId() {
      return thumbnailId;
    }

    public boolean storyTitleSpecified() {
      return storyTitleSpecified;
    }

    public boolean thumbnailIdSpecified() {
      return thumbnailIdSpecified;
    }

    boolean isEmpty() {
      return !storyTitleSpecified && !thumbnailIdSpecified;
    }
  }

  /** ストーリーのレスポンス DTO。 */
  public record StoryResponse(
      String storyId,
      String auth0Id,
      String storyTitle,
      String thumbnailId,
      String createdAt,
      String updatedAt) {}
}
