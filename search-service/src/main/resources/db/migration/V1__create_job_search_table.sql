CREATE TABLE IF NOT EXISTS job_search_records (
    id VARCHAR(36) PRIMARY KEY,
    job_id VARCHAR(36) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    company VARCHAR(150) NOT NULL,
    location VARCHAR(100) NOT NULL,
    salary VARCHAR(50),
    description TEXT,
    recruiter_id VARCHAR(36),
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
