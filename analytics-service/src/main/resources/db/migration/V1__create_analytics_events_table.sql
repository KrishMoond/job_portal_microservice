CREATE TABLE IF NOT EXISTS analytics_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    job_id VARCHAR(36),
    user_id VARCHAR(36),
    metadata VARCHAR(500),
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
