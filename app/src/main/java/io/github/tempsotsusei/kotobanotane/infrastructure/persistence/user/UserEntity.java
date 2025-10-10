package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** users テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @Column(name = "auth0_user_id", length = 255, nullable = false)
  private String auth0UserId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  /** JPA が利用するデフォルトコンストラクタ。 */
  protected UserEntity() {}

  public UserEntity(String auth0UserId, Instant createdAt, Instant updatedAt) {
    this.auth0UserId = auth0UserId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getAuth0UserId() {
    return auth0UserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void touch(Instant newUpdatedAt) {
    this.updatedAt = newUpdatedAt;
  }
}
