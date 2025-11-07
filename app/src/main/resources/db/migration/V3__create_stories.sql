CREATE TABLE IF NOT EXISTS stories (
    story_id VARCHAR(255) PRIMARY KEY,
    auth0_user_id VARCHAR(255) NOT NULL,
    story_title VARCHAR(255) NOT NULL,
    thumbnail_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_stories_users FOREIGN KEY (auth0_user_id) REFERENCES users (auth0_user_id),
    CONSTRAINT fk_stories_thumbnails FOREIGN KEY (thumbnail_id) REFERENCES thumbnails (thumbnail_id)
);

CREATE OR REPLACE FUNCTION set_stories_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_stories_updated_at ON stories;
CREATE TRIGGER trg_stories_updated_at
    BEFORE UPDATE ON stories
    FOR EACH ROW EXECUTE FUNCTION set_stories_updated_at();
