package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import io.github.tempsotsusei.kotobanotane.application.thumbnail.ThumbnailService;
import io.github.tempsotsusei.kotobanotane.domain.thumbnail.Thumbnail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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
 * サムネイル用の開発向け CRUD API を提供するコントローラ。
 *
 * <p>ユーザー CRUD と同じルーティング規約で統一し、動作確認を容易にする目的で設置する。
 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevThumbnailCrudController {

  /** サムネイル関連処理を担うサービス。 */
  private final ThumbnailService thumbnailService;

  /** DI されたサービスを利用して処理を実行する。 */
  public DevThumbnailCrudController(ThumbnailService thumbnailService) {
    this.thumbnailService = thumbnailService;
  }

  /** サムネイルを全件取得する。 */
  @GetMapping("/thumbnails")
  @PreAuthorize("permitAll()")
  public List<ThumbnailResponse> list() {
    return thumbnailService.findAll().stream().map(ThumbnailResponse::from).toList();
  }

  /** サムネイルを 1 件取得する。 */
  @GetMapping("/thumbnail/{thumbnailId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailResponse> get(@PathVariable String thumbnailId) {
    return thumbnailService
        .findById(thumbnailId)
        .map(ThumbnailResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** サムネイルを新規登録する。 */
  @PostMapping("/thumbnail")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailResponse> create(@Valid @RequestBody ThumbnailRequest request) {
    Thumbnail created = thumbnailService.create(request.thumbnailPath());
    // 作成結果を 201 応答で返却し、クライアントに新規登録が成功したことを伝える
    return ResponseEntity.status(HttpStatus.CREATED).body(ThumbnailResponse.from(created));
  }

  /** 既存サムネイルを更新する。 */
  @PutMapping("/thumbnail/{thumbnailId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ThumbnailResponse> update(
      @PathVariable String thumbnailId, @Valid @RequestBody ThumbnailRequest request) {
    return thumbnailService
        .update(thumbnailId, request.thumbnailPath())
        .map(ThumbnailResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** サムネイルを削除する。 */
  @DeleteMapping("/thumbnail/{thumbnailId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String thumbnailId) {
    thumbnailService.delete(thumbnailId);
    // 削除成功時はコンテンツ無しレスポンスを返す
    return ResponseEntity.noContent().build();
  }

  /** サムネイルの作成・更新で利用するリクエストボディ。 */
  public record ThumbnailRequest(@NotBlank @Size(max = 255) String thumbnailPath) {}

  /** レスポンスとして返却する DTO。 */
  public record ThumbnailResponse(
      String thumbnailId, String thumbnailPath, Instant createdAt, Instant updatedAt) {

    /** ドメインモデルからレスポンスを生成する。 */
    static ThumbnailResponse from(Thumbnail thumbnail) {
      return new ThumbnailResponse(
          thumbnail.thumbnailId(),
          thumbnail.thumbnailPath(),
          thumbnail.createdAt(),
          thumbnail.updatedAt());
    }
  }
}
