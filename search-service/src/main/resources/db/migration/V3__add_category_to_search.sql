ALTER TABLE job_search_records
ADD COLUMN IF NOT EXISTS category VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_job_category ON job_search_records (category);
