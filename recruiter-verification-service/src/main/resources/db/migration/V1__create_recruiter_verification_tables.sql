CREATE TABLE recruiter_submissions (
  id BIGSERIAL PRIMARY KEY,
  company_name VARCHAR(255) NOT NULL,
  work_email VARCHAR(255) NOT NULL,
  email_domain VARCHAR(255) NOT NULL,
  company_website VARCHAR(255) NOT NULL,
  registration_number VARCHAR(120) NOT NULL,
  contact_name VARCHAR(255) NOT NULL,
  phone VARCHAR(80),
  document_url TEXT NOT NULL,
  document_type VARCHAR(120) NOT NULL,
  status VARCHAR(40) NOT NULL,
  submitted_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE INDEX idx_recruiter_submissions_status_submitted
  ON recruiter_submissions(status, submitted_at);

CREATE INDEX idx_recruiter_submissions_company_email
  ON recruiter_submissions(LOWER(company_name), LOWER(work_email));

CREATE TABLE recruiter_review_audit_logs (
  id BIGSERIAL PRIMARY KEY,
  submission_id BIGINT NOT NULL REFERENCES recruiter_submissions(id),
  reviewer_id VARCHAR(255) NOT NULL,
  decision VARCHAR(40) NOT NULL,
  reason TEXT,
  reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recruiter_review_audit_submission
  ON recruiter_review_audit_logs(submission_id, reviewed_at DESC);
