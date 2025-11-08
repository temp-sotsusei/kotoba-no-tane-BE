-- フィードバック(feedbacks)テーブルを作成するマイグレーション
CREATE TABLE IF NOT EXISTS feedbacks (
    feedback_id VARCHAR(255) PRIMARY KEY,
    chapter_id VARCHAR(255) NOT NULL,
    feedback TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_feedbacks_chapters FOREIGN KEY (chapter_id) REFERENCES chapters (chapter_id)
);

-- chapters 参照を保ちつつ updated_at を自動更新するトリガー
CREATE OR REPLACE FUNCTION set_feedbacks_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_feedbacks_updated_at ON feedbacks;
CREATE TRIGGER trg_feedbacks_updated_at
    BEFORE UPDATE ON feedbacks
    FOR EACH ROW EXECUTE FUNCTION set_feedbacks_updated_at();
