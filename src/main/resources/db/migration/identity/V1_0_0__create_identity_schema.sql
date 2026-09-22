CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE IF NOT EXISTS identity.user_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_identities_email 
    ON identity.user_identities (email) 
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_user_identities_status 
    ON identity.user_identities (status);

CREATE TABLE IF NOT EXISTS identity.credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_identity_id UUID NOT NULL REFERENCES identity.user_identities(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    mfa_secret VARCHAR(255) NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    must_change_password BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_credentials_user_identity_id 
    ON identity.credentials (user_identity_id);

CREATE TABLE IF NOT EXISTS identity.refresh_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_identity_id UUID NOT NULL REFERENCES identity.user_identities(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_sessions_user_identity_id 
    ON identity.refresh_sessions (user_identity_id);

CREATE INDEX IF NOT EXISTS idx_refresh_sessions_expires_revoked 
    ON identity.refresh_sessions (expires_at, revoked_at);
