CREATE TABLE waiver_history (
    id                       BIGSERIAL    PRIMARY KEY,
    centre_id                BIGINT       NOT NULL REFERENCES centres(id) ON DELETE CASCADE,
    waiver_type              VARCHAR(100) NOT NULL,
    waiver_title             VARCHAR(255) NOT NULL,
    waiver_description       TEXT,
    waiver_status            VARCHAR(30)  NOT NULL,
    approval_date            DATE,
    expiry_date              DATE,
    approved_by              VARCHAR(100),
    officer_remarks          TEXT,
    supporting_document_name VARCHAR(255),
    supporting_document_url  TEXT,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_waiver_history_centre ON waiver_history(centre_id);
