# temp-sotsusei-BE

Spring Boot (Java 21) backend for the temp-sotsusei project. Auth0 を利用した JWT 認証と Cloudflare R2 / Neon などの外部サービス統合を前提に、モジュール化したアーキテクチャで構築します。

## 現状のセットアップ

- Spring Boot 3.4 + Gradle
- Dockerfile（マルチステージビルド）と `docker-compose.yml`
- Auth0 Resource Server 設定のひな型 (`application.yml`)
- Flyway / Spring Security など基本依存
- Spotless（Java / Markdown / YAML）と Prettier 設定
- `docs/internal/` に作業メモ（Git 追跡対象外）

## ローカル起動手順

1. `.env.example` をコピーして `.env` を作成し、Auth0 ドメインやポートなどを指定
2. `docker compose up --build -d`
3. `curl http://localhost:${APP_PORT:-8080}/healthz` でステータス確認
4. 終了は `docker compose down`（必要なら `-v` 付き）

## フォーマット運用

- 保存時：VS Code で `editor.formatOnSave` を有効化済み
  - Java → Red Hat Java フォーマッタ
  - Markdown / YAML / JSON → Prettier（`.prettierrc`）
- 一括整形：`app\gradlew.bat spotlessApply`
- 保存時と整形コマンドの設定を統一しているため、コミット前に改めて整形しても差分がぶれません
- java 以外一括整形`npx prettier --write . --config .prettierrc --ignore-path .prettierignore`
- java 一括整形
- `.\app\gradlew.bat -p app spotlessApply`

## 今後追加する内容

- Auth0 audience/permissions 設定と保護 API 実装
- Flyway マイグレーション / Seed データ
- Cloudflare R2 連携や画像ストレージ API
- CI/CD フローとテスト方針（docs/internal/test_notes.md 参照）
- API 仕様書（OpenAPI）

## ディレクトリ

- `app/`: Spring Boot アプリ本体（`README` 内に概要記載）
- `docs/internal/`: 作業メモ（Git ignore 対象）
- その他は `docs/internal/directory_overview.md` を参照

必要に応じて項目を詳細化していく予定です。
