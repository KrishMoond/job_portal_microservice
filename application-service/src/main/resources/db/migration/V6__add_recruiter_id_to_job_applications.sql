-- Store recruiter_id to avoid cross-service lookups for authorization checks.
ALTER TABLE job_applications
  ADD COLUMN IF NOT EXISTS recruiter_id VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_job_applications_recruiter_id ON job_applications(recruiter_id);

