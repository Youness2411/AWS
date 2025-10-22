-- Add aiScore column to theories table
ALTER TABLE theories ADD COLUMN ai_score INTEGER DEFAULT 50;

-- Update existing theories with default AI score
UPDATE theories SET ai_score = 50 WHERE ai_score IS NULL;
