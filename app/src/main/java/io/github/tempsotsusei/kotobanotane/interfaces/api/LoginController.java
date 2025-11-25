package io.github.tempsotsusei.kotobanotane.interfaces.api;

import io.github.tempsotsusei.kotobanotane.application.auth.LoginApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** `/api/login` のエンドポイントを提供するコントローラー。 */
@RestController
@RequestMapping("/api/login")
public class LoginController {

  private final LoginApplicationService loginApplicationService;

  public LoginController(LoginApplicationService loginApplicationService) {
    this.loginApplicationService = loginApplicationService;
  }

  /**
   * 認証済みユーザーの存在確認と作成を行う。
   *
   * @param authentication Spring Security が解決した JWT 認証情報
   * @return 200 OK（ボディなし）
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> login(JwtAuthenticationToken authentication) {
    loginApplicationService.login(authentication.getToken());
    return ResponseEntity.ok().build();
  }
}
