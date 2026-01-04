-- Create contact_messages table
CREATE TABLE IF NOT EXISTS contact_messages (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contact_messages_is_read ON contact_messages(is_read);
CREATE INDEX IF NOT EXISTS idx_contact_messages_created_at ON contact_messages(created_at DESC);

-- Add wallet_address to enrollments
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS wallet_address VARCHAR(128);



ALTER TABLE certificates ALTER COLUMN img_url TYPE TEXT;
ALTER TABLE certificates ALTER COLUMN qr_url TYPE TEXT;