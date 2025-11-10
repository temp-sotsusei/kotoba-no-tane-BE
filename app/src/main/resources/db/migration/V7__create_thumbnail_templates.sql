-- サムネイルテンプレート(thumbnail_templates)テーブルを作成するマイグレーション
CREATE TABLE IF NOT EXISTS thumbnail_templates (
    thumbnail_template_id VARCHAR(255) PRIMARY KEY,
    thumbnail_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_thumbnail_templates_thumbnails FOREIGN KEY (thumbnail_id) REFERENCES thumbnails (thumbnail_id)
);

-- thumbnails への参照を保ちつつ updated_at を自動更新するトリガー
CREATE OR REPLACE FUNCTION set_thumbnail_templates_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_thumbnail_templates_updated_at ON thumbnail_templates;
CREATE TRIGGER trg_thumbnail_templates_updated_at
    BEFORE UPDATE ON thumbnail_templates
    FOR EACH ROW EXECUTE FUNCTION set_thumbnail_templates_updated_at();
