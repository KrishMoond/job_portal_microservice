ALTER TABLE interviews
    ADD CONSTRAINT uq_interview_application_time UNIQUE (application_id, scheduled_at);
