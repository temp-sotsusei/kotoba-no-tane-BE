-- Replace chapter_text with JSON column and drop keywords table

ALTER TABLE chapters
    DROP COLUMN chapter_text;

ALTER TABLE chapters
    ADD COLUMN chapter_json JSONB NOT NULL DEFAULT '{}'::jsonb;

ALTER TABLE chapters
    ALTER COLUMN chapter_json DROP DEFAULT;

-- Remove keywords table (no longer needed)
DROP TABLE IF EXISTS keywords;
