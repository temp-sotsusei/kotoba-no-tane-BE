package io.github.tempsotsusei.kotobanotane.interfaces.api;

import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateQueryService;
import io.github.tempsotsusei.kotobanotane.application.thumbnailtemplate.ThumbnailTemplateSummary;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * サムネイルテンプレート一覧を返す本番向け API。
 *
 * <p>thumbnail_templates に登録されているものだけを返却し、テンプレート以外のサムネイルは含めない。
 */
@RestController
@RequestMapping("/api/thumbnail-templates")
public class ThumbnailTemplateController {

  private final AuthenticatedTokenService authenticatedTokenService;
  private final ThumbnailTemplateQueryService thumbnailTemplateQueryService;

  public ThumbnailTemplateController(
      AuthenticatedTokenService authenticatedTokenService,
      ThumbnailTemplateQueryService thumbnailTemplateQueryService) {
    this.authenticatedTokenService = authenticatedTokenService;
    this.thumbnailTemplateQueryService = thumbnailTemplateQueryService;
  }

  /**
   * テンプレートとして登録されているサムネイル一覧を返す。
   *
   * @return thumbnailId / thumbnailPath の配列（createdAt 降順）
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ThumbnailTemplateResponse> list(JwtAuthenticationToken authentication) {
    authenticatedTokenService.requireExistingAuth0Id(
        authenticatedTokenService.extractAuth0Id(authentication.getToken()));
    return thumbnailTemplateQueryService.listTemplates().stream().map(this::toResponse).toList();
  }

  /** `ThumbnailTemplateSummary` を API レスポンスに変換する。 */
  private ThumbnailTemplateResponse toResponse(ThumbnailTemplateSummary summary) {
    return new ThumbnailTemplateResponse(summary.thumbnailId(), summary.thumbnailPath());
  }

  /** `/api/thumbnail-templates` のレスポンス DTO。 */
  public record ThumbnailTemplateResponse(String thumbnailId, String thumbnailPath) {}
}
