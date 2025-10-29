-- サムネイル情報を保持するテーブルを作成するマイグレーション
CREATE TABLE IF NOT EXISTS thumbnails (
    thumbnail_id VARCHAR(255) PRIMARY KEY,
    thumbnail_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

-- 更新時に updated_at を自動で書き換えるトリガー用関数
CREATE OR REPLACE FUNCTION set_thumbnails_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 既存トリガーを削除してから再作成し、関数を紐付ける
DROP TRIGGER IF EXISTS trg_thumbnails_updated_at ON thumbnails;
CREATE TRIGGER trg_thumbnails_updated_at
    BEFORE UPDATE ON thumbnails
    FOR EACH ROW EXECUTE FUNCTION set_thumbnails_updated_at();
