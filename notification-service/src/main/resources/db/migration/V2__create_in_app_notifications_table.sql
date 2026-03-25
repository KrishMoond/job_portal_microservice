CREATE TABLE IF NOT EXISTS in_app_notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_in_app_notifications_user_id ON in_app_notifications (user_id);
CREATE INDEX idx_in_app_notifications_user_unread ON in_app_notifications (user_id, is_read);
