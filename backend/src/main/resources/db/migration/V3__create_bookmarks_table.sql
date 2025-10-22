-- Create bookmarks table
CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    theory_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_theory FOREIGN KEY (theory_id) REFERENCES theories(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_theory_bookmark UNIQUE (user_id, theory_id)
);

-- Create indexes for better performance
CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_theory_id ON bookmarks(theory_id);
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);

