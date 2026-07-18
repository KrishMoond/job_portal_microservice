ALTER TABLE users DROP COLUMN IF EXISTS verification_token;
ALTER TABLE users DROP COLUMN IF EXISTS verification_token_expiry;
ALTER TABLE users ADD COLUMN verification_otp VARCHAR(6);
ALTER TABLE users ADD COLUMN verification_otp_expiry TIMESTAMP;
