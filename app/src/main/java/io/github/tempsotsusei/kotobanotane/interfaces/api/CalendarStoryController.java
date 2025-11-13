package io.github.tempsotsusei.kotobanotane.interfaces.api;

import io.github.tempsotsusei.kotobanotane.application.auth.AuthenticatedTokenService;
import io.github.tempsotsusei.kotobanotane.application.story.CalendarStoriesQueryService;
import io.github.tempsotsusei.kotobanotane.application.story.CalendarStorySummary;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * カレンダー画面のストーリー一覧を提供する本番 API。
 *
 * <p>認証済みユーザーが作成したストーリーを作成日時降順で返却する。
 */
@RestController
@RequestMapping("/api/calendar/stories")
public class CalendarStoryController {

  private final AuthenticatedTokenService authenticatedTokenService;
  private final CalendarStoriesQueryService calendarStoriesQueryService;
  private final TimeProvider timeProvider;

  public CalendarStoryController(
      AuthenticatedTokenService authenticatedTokenService,
      CalendarStoriesQueryService calendarStoriesQueryService,
      TimeProvider timeProvider) {
    this.authenticatedTokenService = authenticatedTokenService;
    this.calendarStoriesQueryService = calendarStoriesQueryService;
    this.timeProvider = timeProvider;
  }

  /**
   * 認証済みユーザーのストーリー一覧を取得する。
   *
   * @param authentication Spring Security が検証した JWT 認証情報
   * @return 作成日時降順で並んだレスポンス
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<CalendarStoryResponse> list(JwtAuthenticationToken authentication) {
    String auth0Id = authenticatedTokenService.extractAuth0Id(authentication.getToken());
    return calendarStoriesQueryService.fetchByAuth0UserId(auth0Id).stream()
        .map(this::toResponse)
        .toList();
  }

  private CalendarStoryResponse toResponse(CalendarStorySummary summary) {
    return new CalendarStoryResponse(
        summary.storyId(),
        summary.storyTitle(),
        summary.thumbnailPath(),
        timeProvider.formatIso(summary.createdAt()));
  }

  /** `/api/calendar/stories` のレスポンス DTO。 */
  public record CalendarStoryResponse(
      String storyId, String storyTitle, String thumbnailPath, String createdAt) {}
}
