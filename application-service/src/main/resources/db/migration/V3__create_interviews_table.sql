CREATE TABLE IF NOT EXISTS interviews (
    id VARCHAR(36) PRIMARY KEY,
    application_id VARCHAR(36) NOT NULL,
    candidate_id VARCHAR(36) NOT NULL,
    recruiter_id VARCHAR(36) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    meeting_link VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interviews_application_id ON interviews (application_id);
CREATE INDEX idx_interviews_candidate_id ON interviews (candidate_id);
CREATE INDEX idx_interviews_recruiter_id ON interviews (recruiter_id);
