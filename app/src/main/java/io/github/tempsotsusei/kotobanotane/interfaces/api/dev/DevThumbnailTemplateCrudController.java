package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateService;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateUpdateCommand;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.thumbnailtemplate.ThumbnailTemplate;
import jakarta.validation.Valid;
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
 * サムネイルテンプレート向けの開発用 CRUD API。
 *
 * <p>dev プロファイルのみで動作し、Postman や curl での検証を容易にする。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevThumbnailTemplateCrudController {

  private final ThumbnailTemplateService thumbnailTemplateService;
  private final TimeProvider timeProvider;

  public DevThumbnailTemplateCrudController(
      ThumbnailTemplateService thumbnailTemplateService, TimeProvider timeProvider) {
    this.thumbnailTemplateService = thumbnailTemplateService;
    this.timeProvider = timeProvider;
  }

  /** テンプレート一覧を返す。 */
  @GetMapping("/thumbnail_templates")
  @PreAuthorize("permitAll()")
  public List<ThumbnailTemplateResponse> list() {
    return thumbnailTemplateService.findAll().stream().map(this::toResponse).toList();
  }

  /** テンプレートを 1 件取得する。 */
  @GetMapping("/thumbnail_template/{thumbnailTemplateId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailTemplateResponse> get(@PathVariable String thumbnailTemplateId) {
    return thumbnailTemplateService
        .findById(thumbnailTemplateId)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** テンプレートを新規作成する。 */
  @PostMapping("/thumbnail_template")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailTemplateResponse> create(
      @Valid @RequestBody ThumbnailTemplateCreateRequest request) {
    ThumbnailTemplate created = thumbnailTemplateService.create(request.thumbnailId());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  /** テンプレートを更新する。 */
  @PutMapping("/thumbnail_template/{thumbnailTemplateId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailTemplateResponse> update(
      @PathVariable String thumbnailTemplateId,
      @Valid @RequestBody ThumbnailTemplateUpdateRequest request) {
    if (request.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    ThumbnailTemplateUpdateCommand command =
        new ThumbnailTemplateUpdateCommand(request.thumbnailIdSpecified(), request.thumbnailId());

    return thumbnailTemplateService
        .update(thumbnailTemplateId, command)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** テンプレートを削除する。 */
  @DeleteMapping("/thumbnail_template/{thumbnailTemplateId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String thumbnailTemplateId) {
    thumbnailTemplateService.delete(thumbnailTemplateId);
    return ResponseEntity.noContent().build();
  }

  private ThumbnailTemplateResponse toResponse(ThumbnailTemplate template) {
    return new ThumbnailTemplateResponse(
        template.thumbnailTemplateId(),
        template.thumbnailId(),
        timeProvider.formatIso(template.createdAt()),
        timeProvider.formatIso(template.updatedAt()));
  }

  /** 作成リクエスト。 */
  public record ThumbnailTemplateCreateRequest(@NotBlank String thumbnailId) {}

  /** 更新リクエスト。 */
  public static class ThumbnailTemplateUpdateRequest {

    private String thumbnailId;
    private boolean thumbnailIdSpecified;

    @JsonProperty("thumbnailId")
    public void setThumbnailId(String thumbnailId) {
      this.thumbnailId = thumbnailId;
      this.thumbnailIdSpecified = true;
    }

    public String thumbnailId() {
      return thumbnailId;
    }

    public boolean thumbnailIdSpecified() {
      return thumbnailIdSpecified;
    }

    boolean isEmpty() {
      return !thumbnailIdSpecified;
    }
  }

  /** レスポンス DTO。 */
  public record ThumbnailTemplateResponse(
      String thumbnailTemplateId, String thumbnailId, String createdAt, String updatedAt) {}
}
