package com.example.app.interfaces.api.dev;

import com.example.app.application.user.UserService;
import com.example.app.domain.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

/** 開発環境でのみ公開するユーザー CRUD API。テスト用に /api/crud 以下に配置する。 */
@RestController
@RequestMapping("/api/crud")
@Profile("dev")
@Validated
public class DevUserCrudController {

  private final UserService userService;

  public DevUserCrudController(UserService userService) {
    this.userService = userService;
  }

  /** 全ユーザーを取得する。 */
  @GetMapping("/users")
  @PreAuthorize("permitAll()")
  public List<UserResponse> list() {
    return userService.findAll().stream().map(UserResponse::from).toList();
  }

  /** 単一ユーザーを取得する。 */
  @GetMapping("/user/{auth0Id}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<UserResponse> get(@PathVariable String auth0Id) {
    return userService
        .findById(auth0Id)
        .map(UserResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** ユーザーを新規作成する。 */
  @PostMapping("/user")
  @PreAuthorize("permitAll()")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
    User created = userService.create(request.auth0Id());
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
  }

  /** ユーザーの更新日時を更新する。 */
  @PutMapping("/user/{auth0Id}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<UserResponse> update(@PathVariable String auth0Id) {
    return userService
        .update(auth0Id)
        .map(UserResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** ユーザーを削除する。 */
  @DeleteMapping("/user/{auth0Id}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> delete(@PathVariable String auth0Id) {
    userService.delete(auth0Id);
    return ResponseEntity.noContent().build();
  }

  /** ユーザー作成時のリクエストボディ。 */
  public record UserRequest(@NotBlank String auth0Id) {}

  /** レスポンス表現。 */
  public record UserResponse(String auth0Id, Instant createdAt, Instant updatedAt) {

    static UserResponse from(User user) {
      return new UserResponse(user.auth0Id(), user.createdAt(), user.updatedAt());
    }
  }
}
