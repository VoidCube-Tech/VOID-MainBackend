CREATE SCHEMA IF NOT EXISTS company;

CREATE TABLE IF NOT EXISTS company.companies (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    corporate_name VARCHAR(255) NOT NULL,
    trade_name  VARCHAR(255) NOT NULL,
    document    VARCHAR(20)  NOT NULL,
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    settings    JSONB        NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ  NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_companies_document
    ON company.companies (document)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_companies_status
    ON company.companies (status);

CREATE TABLE IF NOT EXISTS company.roles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID         NOT NULL REFERENCES company.companies(id) ON DELETE CASCADE,
    name        VARCHAR(50)  NOT NULL,
    permissions JSONB        NOT NULL DEFAULT '[]',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ  NULL
);

CREATE INDEX IF NOT EXISTS idx_roles_company_id
    ON company.roles (company_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_roles_company_name
    ON company.roles (company_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS company.company_memberships (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id       UUID        NOT NULL REFERENCES company.companies(id) ON DELETE CASCADE,
    user_identity_id UUID        NOT NULL,
    role_id          UUID        NOT NULL REFERENCES company.roles(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_memberships_company_user
    ON company.company_memberships (company_id, user_identity_id)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_memberships_company_id
    ON company.company_memberships (company_id);

CREATE INDEX IF NOT EXISTS idx_memberships_user_identity_id
    ON company.company_memberships (user_identity_id);
