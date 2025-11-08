-- キーワード(keywords)テーブルを作成するマイグレーション
CREATE TABLE IF NOT EXISTS keywords (
    keyword_id VARCHAR(255) PRIMARY KEY,
    chapter_id VARCHAR(255) NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    keyword_position INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_keywords_chapters FOREIGN KEY (chapter_id) REFERENCES chapters (chapter_id)
);

-- chapters への外部キー整合性と updated_at 自動更新のためのトリガー
CREATE OR REPLACE FUNCTION set_keywords_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_keywords_updated_at ON keywords;
CREATE TRIGGER trg_keywords_updated_at
    BEFORE UPDATE ON keywords
    FOR EACH ROW EXECUTE FUNCTION set_keywords_updated_at();
