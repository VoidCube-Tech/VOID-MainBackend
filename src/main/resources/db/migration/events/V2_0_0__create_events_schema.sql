
CREATE SCHEMA IF NOT EXISTS events;

CREATE TABLE IF NOT EXISTS events.user_classes (
    id          UUID         PRIMARY KEY,
    company_id  UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ  NULL
);

CREATE INDEX IF NOT EXISTS idx_user_classes_company_id
    ON events.user_classes (company_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_classes_company_name
    ON events.user_classes (company_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS events.service_users (
    id                UUID         PRIMARY KEY,
    company_id        UUID         NOT NULL,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    credential_hash   VARCHAR(255) NULL,
    temporary_password BOOLEAN     NOT NULL DEFAULT true,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ  NULL
);

CREATE INDEX IF NOT EXISTS idx_service_users_company_id
    ON events.service_users (company_id);

CREATE INDEX IF NOT EXISTS idx_service_users_status
    ON events.service_users (company_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_users_company_email
    ON events.service_users (company_id, email)
    WHERE deleted_at IS NULL AND email IS NOT NULL;

CREATE TABLE IF NOT EXISTS events.legacy_user_references (
    id                UUID         PRIMARY KEY,
    company_id        UUID         NOT NULL,
    service_user_id   UUID         NOT NULL REFERENCES events.service_users(id) ON DELETE CASCADE,
    registration_code VARCHAR(100) NOT NULL,
    source_type       VARCHAR(50)  NOT NULL DEFAULT 'LEGACY_MATRICULA',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_legacy_refs_company_registration
    ON events.legacy_user_references (company_id, registration_code);

CREATE INDEX IF NOT EXISTS idx_legacy_refs_service_user_id
    ON events.legacy_user_references (service_user_id);

CREATE TABLE IF NOT EXISTS events.user_class_memberships (
    service_user_id UUID        NOT NULL REFERENCES events.service_users(id) ON DELETE CASCADE,
    class_id        UUID        NOT NULL REFERENCES events.user_classes(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (service_user_id, class_id)
);

CREATE INDEX IF NOT EXISTS idx_class_memberships_class_id
    ON events.user_class_memberships (class_id);

CREATE TABLE IF NOT EXISTS events.events (
    id               UUID         PRIMARY KEY,
    company_id       UUID         NOT NULL,
    owner_manager_id UUID         NOT NULL,
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    version          BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ  NULL,
    CONSTRAINT chk_events_status CHECK (
        status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'FINISHED', 'ARCHIVED')
    )
);

CREATE INDEX IF NOT EXISTS idx_events_company_id
    ON events.events (company_id);

CREATE INDEX IF NOT EXISTS idx_events_company_status
    ON events.events (company_id, status);

CREATE TABLE IF NOT EXISTS events.event_audience_rules (
    id          UUID        PRIMARY KEY,
    event_id    UUID        NOT NULL REFERENCES events.events(id) ON DELETE CASCADE,
    rule_type   VARCHAR(20) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_audience_rule_type CHECK (rule_type IN ('INCLUDE', 'EXCLUDE')),
    CONSTRAINT chk_audience_target_type CHECK (target_type IN ('CLASS', 'USER', 'SITE'))
);

CREATE INDEX IF NOT EXISTS idx_audience_rules_event_id
    ON events.event_audience_rules (event_id);

CREATE TABLE IF NOT EXISTS events.event_sessions (
    id         UUID         PRIMARY KEY,
    event_id   UUID         NOT NULL REFERENCES events.events(id) ON DELETE CASCADE,
    company_id UUID         NOT NULL,
    title      VARCHAR(255) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    start_at   TIMESTAMPTZ  NOT NULL,
    end_at     TIMESTAMPTZ  NOT NULL,
    capacity   INTEGER      NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ  NULL,
    CONSTRAINT chk_sessions_capacity CHECK (capacity >= 0),
    CONSTRAINT chk_sessions_dates CHECK (end_at > start_at),
    CONSTRAINT chk_sessions_status CHECK (
        status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
    )
);

CREATE INDEX IF NOT EXISTS idx_event_sessions_event_id
    ON events.event_sessions (event_id);

CREATE INDEX IF NOT EXISTS idx_event_sessions_company_id
    ON events.event_sessions (company_id);

CREATE INDEX IF NOT EXISTS idx_event_sessions_start_at
    ON events.event_sessions (company_id, start_at)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS events.reservations (
    id                    UUID        PRIMARY KEY,
    session_id            UUID        NOT NULL REFERENCES events.event_sessions(id) ON DELETE RESTRICT,
    service_user_id       UUID        NOT NULL REFERENCES events.service_users(id) ON DELETE RESTRICT,
    company_id            UUID        NOT NULL,
    status                VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    cancellation_reason   TEXT        NULL,
    cancelled_by_actor_id UUID        NULL,
    version               BIGINT      NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    cancelled_at          TIMESTAMPTZ NULL,
    CONSTRAINT chk_reservations_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservations_active_session_user
    ON events.reservations (session_id, service_user_id)
    WHERE status = 'CONFIRMED';

CREATE INDEX IF NOT EXISTS idx_reservations_session_id
    ON events.reservations (session_id);

CREATE INDEX IF NOT EXISTS idx_reservations_service_user_id
    ON events.reservations (service_user_id);

CREATE INDEX IF NOT EXISTS idx_reservations_company_status
    ON events.reservations (company_id, status);

CREATE TABLE IF NOT EXISTS events.waitlist_entries (
    id              UUID        PRIMARY KEY,
    session_id      UUID        NOT NULL REFERENCES events.event_sessions(id) ON DELETE CASCADE,
    service_user_id UUID        NOT NULL REFERENCES events.service_users(id) ON DELETE CASCADE,
    company_id      UUID        NOT NULL,
    position        INTEGER     NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_waitlist_status CHECK (
        status IN ('WAITING', 'PROMOTED', 'EXPIRED', 'CANCELLED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_waitlist_active_session_user
    ON events.waitlist_entries (session_id, service_user_id)
    WHERE status = 'WAITING';

CREATE INDEX IF NOT EXISTS idx_waitlist_session_id_position
    ON events.waitlist_entries (session_id, position)
    WHERE status = 'WAITING';

CREATE INDEX IF NOT EXISTS idx_waitlist_service_user_id
    ON events.waitlist_entries (service_user_id);
