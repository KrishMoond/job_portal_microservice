INSERT INTO users (
    id,
    name,
    email,
    password,
    role,
    email_verified,
    verification_otp,
    verification_otp_expiry,
    otp_attempts,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Dev Admin',
    'admin@jobportal.local',
    '$2a$10$vyri77gzJu7qD01BESqcQuI5TTnKmoXKvk2WU0SBPmdIdJUauhI26',
    'ADMIN',
    TRUE,
    NULL,
    NULL,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
