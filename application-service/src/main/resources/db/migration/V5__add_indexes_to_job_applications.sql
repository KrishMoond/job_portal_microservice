-- Speed up recruiter/job seeker queries and duplicate checks.
CREATE INDEX IF NOT EXISTS idx_job_applications_job_id ON job_applications(job_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_candidate_id ON job_applications(candidate_id);

-- Fast "already applied" check + enforce data integrity.
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk_job_applications_job_candidate'
  ) THEN
    ALTER TABLE job_applications
      ADD CONSTRAINT uk_job_applications_job_candidate UNIQUE (job_id, candidate_id);
  END IF;
END $$;

