package io.github.tempsotsusei.kotobanotane.domain.user;

import java.time.Instant;

/** ユーザーのドメインモデル。Auth0 の subject と作成・更新時刻のみを保持する。 */
public record User(String auth0Id, Instant createdAt, Instant updatedAt) {}
