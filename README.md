# ことばのタネ BE

この README は、本リポジトリのバックエンド環境をローカルで再現し、同じ API 動作を確認するための手順書です。

## 1. この README の目的

- ローカルでバックエンドを再現して起動できること
- 本番 API / 開発用 API の基本動作を確認できること
- 必要な設定値（`.env`）の意味が分かること

## 2. 前提条件

- Docker Desktop（Docker Engine + Docker Compose v2）
- PowerShell（Windows の例を記載）
- `curl`（動作確認で使用）
- Auth0 のアクセストークン（認証必須 API の確認時）
- OpenAI API Key（LLM を使う API を確認する場合）

## 3. クイックスタート（最短）

1. `.env.example` をコピーして `.env` を作成
   ```powershell
   Copy-Item .env.example .env
   ```
2. 必要に応じて `.env` を編集（最低限 Auth0 と DB 設定）
3. コンテナ起動
   ```powershell
   docker compose up --build -d
   ```
4. ヘルスチェック
   ```powershell
   curl http://localhost:8080/healthz
   ```
5. 停止
   ```powershell
   docker compose down
   ```

## 4. 詳細セットアップ（`.env`）

`.env.example` の主な項目は以下です。

### アプリ/プロファイル

- `APP_PORT`: アプリ公開ポート（デフォルト 8080）
- `SPRING_PROFILES_ACTIVE`: `default` / `dev`
  - `default`: 本番 API のみ
  - `dev`: 開発用 API（`/api/crud/**`, `/api/test/**`）も有効

### Auth0

- `APP_AUTH0_ISSUER`
- `APP_AUTH0_AUDIENCE`
- `APP_AUTH0_DOMAIN`

認証必須 API で 401 が出る場合は、まずこの 3 つを確認してください。

### Database（Neon / PostgreSQL）

- `APP_DATASOURCE_HOST`
- `APP_DATASOURCE_PORT`
- `APP_DATASOURCE_DB`
- `APP_DATASOURCE_USERNAME`
- `APP_DATASOURCE_PASSWORD`
- `APP_DATASOURCE_QUERY`

ローカルの `docker compose` をそのまま使う場合は、既定値（`postgres:5432`）で動作します。

### OpenAI

- `OPENAI_API_KEY`
- `OPENAI_BASE_URL`
- `OPENAI_MODEL`
- `OPENAI_TIMEOUT`
- `OPENAI_DEFAULT_MAX_OUTPUT_TOKENS`
- `OPENAI_MAX_ATTEMPTS`

`/api/story/chapter/keywords`、`/api/story/chapter/next`、`/api/story`（非同期 feedback）で利用します。

### CORS

- `APP_CORS_ALLOWED_ORIGINS`
  - `/api/story`（GET）のみ適用
  - カンマ区切りで複数指定可
  - `Access-Control-Allow-Credentials` は `false`（`credentials: "include"` は不可）

### 非同期/時刻

- `LLM_EXECUTOR_CORE_POOL_SIZE`
- `LLM_EXECUTOR_MAX_POOL_SIZE`
- `LLM_EXECUTOR_QUEUE_CAPACITY`
- `APP_TIME_ZONE`（例: `Asia/Tokyo`）

## 5. 起動方法（Docker）

### 起動

```powershell
docker compose up --build -d
```

### ログ確認

```powershell
docker compose logs -f app
```

### 停止

```powershell
docker compose down
```

### DB データも消す場合

```powershell
docker compose down -v
```

## 6. 動作確認（同じ挙動の確認）

事前にトークンを環境変数へ設定してください。

```powershell
$ACCESS_TOKEN = "<Auth0 Access Token>"
```

### 6.1 ヘルスチェック（認証不要）

```powershell
curl http://localhost:8080/healthz
```

### 6.2 ログイン API（ユーザー作成/存在確認）

```powershell
curl -X POST "http://localhost:8080/api/login" `
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 6.3 物語作成（POST /api/story）

```powershell
curl -X POST "http://localhost:8080/api/story" `
  -H "Authorization: Bearer $ACCESS_TOKEN" `
  -H "Content-Type: application/json" `
  -d @"
{
  "storyTitle": "てすと",
  "chapters": [
    {
      "chapterNum": 1,
      "chapterJson": {
        "type": "doc",
        "content": [
          {
            "type": "paragraph",
            "content": [
              { "type": "text", "text": "これはてすとです" }
            ]
          }
        ]
      }
    }
  ]
}
"@
```

### 6.4 物語詳細取得（GET /api/story?storyId=...）

```powershell
curl "http://localhost:8080/api/story?storyId=<storyId>"
```

- JWT なしでも取得可能（匿名扱い）
- JWT ありで所有者の場合のみ feedback が返る

### 6.5 カレンダー一覧（GET /api/calendar/stories）

```powershell
curl "http://localhost:8080/api/calendar/stories" `
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 6.6 キーワード候補（GET /api/story/chapter/keywords）

```powershell
curl "http://localhost:8080/api/story/chapter/keywords" `
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 6.7 次章キーワード（POST /api/story/chapter/next）

```powershell
curl -X POST "http://localhost:8080/api/story/chapter/next" `
  -H "Authorization: Bearer $ACCESS_TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"chapterJson":{"type":"doc","content":[{"type":"paragraph","content":[{"type":"text","text":"つぎのしょう"}]}]}}'
```

## 7. テスト/整形コマンド

### テスト

```powershell
./app/gradlew.bat -p app test
```

### Spotless（Java / Markdown / YAML）

```powershell
./app/gradlew.bat -p app spotlessApply
```

## 8. よくあるエラーと対処

- 401 が出る
  - `APP_AUTH0_ISSUER` / `APP_AUTH0_AUDIENCE` の不一致を確認
  - 先に `POST /api/login` を実行し、ユーザー登録済みか確認
- CORS エラー（`/api/story` GET）
  - `APP_CORS_ALLOWED_ORIGINS` に origin が含まれているか確認
  - フロント側で `credentials: "include"` を使わない（本APIは `allowCredentials=false`）
- LLM 系 API が失敗
  - `OPENAI_API_KEY` とネットワーク到達性を確認

## 9. ディレクトリ概要

- `app/src/main/java/io/github/tempsotsusei/kotobanotane/interfaces/api`
  - 本番 API コントローラー
- `app/src/main/java/io/github/tempsotsusei/kotobanotane/interfaces/api/dev`
  - 開発用 API（`dev` プロファイルのみ）
- `app/src/main/java/io/github/tempsotsusei/kotobanotane/application`
  - ユースケース（Service / Query / Job）
- `app/src/main/java/io/github/tempsotsusei/kotobanotane/domain`
  - ドメインモデルと Repository 抽象
- `app/src/main/java/io/github/tempsotsusei/kotobanotane/infrastructure`
  - 永続化実装 / 外部 API クライアント

## 10. 補足（dev プロファイル API）

- `SPRING_PROFILES_ACTIVE=dev` で起動時のみ利用可能
- 主な dev API
  - `/api/crud/**`: CRUD 操作
  - `/api/test/test_jwt`, `/api/test/test_user`: JWT / ユーザー検証
  - `/api/test/keyword_lists`, `/api/test/async-jobs`: LLM 動作検証
