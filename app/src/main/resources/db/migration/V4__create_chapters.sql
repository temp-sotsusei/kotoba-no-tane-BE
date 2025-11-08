-- 章(chapters)テーブルを作成するマイグレーション
CREATE TABLE IF NOT EXISTS chapters (
    chapter_id VARCHAR(255) PRIMARY KEY,
    story_id VARCHAR(255) NOT NULL,
    chapter_num INT NOT NULL,
    chapter_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_chapters_stories FOREIGN KEY (story_id) REFERENCES stories (story_id)
);

-- stories との整合性確保と更新日時の自動更新に関するトリガー定義
CREATE OR REPLACE FUNCTION set_chapters_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_chapters_updated_at ON chapters;
CREATE TRIGGER trg_chapters_updated_at
    BEFORE UPDATE ON chapters
    FOR EACH ROW EXECUTE FUNCTION set_chapters_updated_at();
