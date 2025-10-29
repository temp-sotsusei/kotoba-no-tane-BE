package io.github.tempsotsusei.kotobanotane.interfaces.api.dev;

import io.github.tempsotsusei.kotobanotane.application.llm.AsyncLlmJobService;
import io.github.tempsotsusei.kotobanotane.application.user.UserService;
import io.github.tempsotsusei.kotobanotane.config.time.TimeProvider;
import io.github.tempsotsusei.kotobanotane.domain.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 非同期ジョブの動作検証のための開発用エンドポイント。
 *
 * <p>LLM 呼び出しと {@code users.updated_at} 更新をジョブ側で行い、API からは即時レスポンスする。
 */
@RestController
@RequestMapping("/api/test/async-jobs")
@Profile("dev")
@Validated
public class DevAsyncJobController {

  private static final int MAX_JOBS_PER_REQUEST = 5;

  private final UserService userService;
  private final AsyncLlmJobService asyncLlmJobService;
  private final TimeProvider timeProvider;

  public DevAsyncJobController(
      UserService userService, AsyncLlmJobService asyncLlmJobService, TimeProvider timeProvider) {
    this.userService = userService;
    this.asyncLlmJobService = asyncLlmJobService;
    this.timeProvider = timeProvider;
  }

  /**
   * 非同期ジョブをキックし、レスポンスではユーザーの更新時刻のみ返す。
   *
   * <p>ジョブ完了の可否は、呼び出し後の {@code users.updated_at} を確認することで判断する。
   */
  @PostMapping
  @PreAuthorize("permitAll()")
  public ResponseEntity<JobTriggerResponse> triggerJobs(
      @Valid @RequestBody JobTriggerRequest request) {
    String auth0Id = request.auth0Id();
    // ユーザーが存在しない場合は作成し、現在の updated_at だけをレスポンスに含める
    User user = userService.findOrCreate(auth0Id);

    int requestedJobs = request.resolvedJobCount();
    int queuedJobs = Math.min(requestedJobs, MAX_JOBS_PER_REQUEST);
    for (int i = 1; i <= queuedJobs; i++) {
      asyncLlmJobService.runJob(auth0Id, i);
    }

    JobTriggerResponse response =
        new JobTriggerResponse(auth0Id, timeProvider.formatIso(user.updatedAt()), queuedJobs);
    return ResponseEntity.ok(response);
  }

  /** 非同期ジョブ起動リクエスト。 */
  public record JobTriggerRequest(
      @NotBlank String auth0Id, @Min(1) @Max(MAX_JOBS_PER_REQUEST) Integer jobCount) {

    public int resolvedJobCount() {
      return jobCount == null ? 1 : jobCount;
    }
  }

  /** 非同期ジョブ起動レスポンス。 */
  public record JobTriggerResponse(String auth0Id, String updatedAt, int queuedJobs) {}
}
