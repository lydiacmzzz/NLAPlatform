CREATE TABLE centres (
    id                  BIGSERIAL PRIMARY KEY,
    centre_id           VARCHAR(20)  NOT NULL UNIQUE,
    licence_number      VARCHAR(50)  NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    centre_type         VARCHAR(50)  NOT NULL,
    address             TEXT         NOT NULL,
    postal_code         VARCHAR(10)  NOT NULL,
    operating_hours     VARCHAR(100),
    capacity            INTEGER      NOT NULL,
    licence_status      VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    licence_issue_date  DATE,
    licence_expiry_date DATE,
    renewal_due_date    DATE,
    application_stage   VARCHAR(100),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(100)
);

CREATE TABLE kah_details (
    id                      BIGSERIAL PRIMARY KEY,
    centre_id               BIGINT       NOT NULL REFERENCES centres(id) ON DELETE CASCADE,
    principal_name          VARCHAR(255) NOT NULL,
    nric                    VARCHAR(20)  NOT NULL,
    email                   VARCHAR(255),
    phone                   VARCHAR(30),
    licence_conditions      TEXT,
    appointment_start_date  DATE         NOT NULL,
    appointment_end_date    DATE,
    is_current              BOOLEAN      NOT NULL DEFAULT TRUE,
    pending_approval        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE centre_contacts (
    id                  BIGSERIAL PRIMARY KEY,
    centre_id           BIGINT       NOT NULL REFERENCES centres(id) ON DELETE CASCADE,
    contact_type        VARCHAR(30)  NOT NULL,
    contact_name        VARCHAR(255) NOT NULL,
    role                VARCHAR(100),
    email               VARCHAR(255),
    phone               VARCHAR(30),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE centre_lifecycle_events (
    id           BIGSERIAL PRIMARY KEY,
    centre_id    BIGINT       NOT NULL REFERENCES centres(id) ON DELETE CASCADE,
    event_type   VARCHAR(100) NOT NULL,
    description  TEXT         NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    recorded_by  VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    full_name     VARCHAR(255),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_centres_licence_status  ON centres(licence_status);
CREATE INDEX idx_centres_centre_type     ON centres(centre_type);
CREATE INDEX idx_centres_postal_code     ON centres(postal_code);
CREATE INDEX idx_centres_renewal_due     ON centres(renewal_due_date);
CREATE INDEX idx_kah_centre_current      ON kah_details(centre_id, is_current);
CREATE INDEX idx_lifecycle_centre        ON centre_lifecycle_events(centre_id, occurred_at);

INSERT INTO users (username, email, password_hash, role, full_name)
VALUES
  ('admin', 'admin@ecda.gov.sg',
   '$2b$10$P.BNKpn5aoLb/1IziYTEZeBJKLtGUbT8pRTHWEgS.tL1Hy5mqZBVK',
   'HQ_ADMIN', 'HQ Administrator'),
  ('officer1', 'officer1@ecda.gov.sg',
   '$2b$10$P.BNKpn5aoLb/1IziYTEZeBJKLtGUbT8pRTHWEgS.tL1Hy5mqZBVK',
   'ECDA_OFFICER', 'Officer One'),
  ('leader1', 'leader@testcentre.sg',
   '$2b$10$P.BNKpn5aoLb/1IziYTEZeBJKLtGUbT8pRTHWEgS.tL1Hy5mqZBVK',
   'CENTRE_LEADER', 'Centre Leader One');
