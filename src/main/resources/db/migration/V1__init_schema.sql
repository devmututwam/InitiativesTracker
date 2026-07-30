-- V1: Core schema for Initiatives Tracker

CREATE TABLE units (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE initiatives (
    id                  BIGSERIAL       PRIMARY KEY,
    project_code        VARCHAR(50)     NOT NULL UNIQUE,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    source_department   VARCHAR(100),
    priority            VARCHAR(20)     NOT NULL,
    status              VARCHAR(30)     NOT NULL,
    start_date          DATE,
    expected_end_date   DATE,
    actual_end_date     DATE,
    year                INTEGER         NOT NULL,
    quarter             INTEGER,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_initiative_project_code ON initiatives (project_code);
CREATE INDEX idx_initiative_start_date   ON initiatives (start_date);

CREATE TABLE budgets (
    id              BIGSERIAL       PRIMARY KEY,
    initiative_id   BIGINT          NOT NULL REFERENCES initiatives (id),
    amount          NUMERIC(17, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    approved_date   DATE,
    budget_source   VARCHAR(100)
);

CREATE TABLE cost_entries (
    id              BIGSERIAL       PRIMARY KEY,
    initiative_id   BIGINT          NOT NULL REFERENCES initiatives (id),
    cost_type       VARCHAR(20)     NOT NULL,
    amount          NUMERIC(17, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    recorded_date   DATE            NOT NULL,
    notes           TEXT
);

CREATE TABLE initiative_units (
    initiative_id           BIGINT          NOT NULL REFERENCES initiatives (id),
    unit_id                 BIGINT          NOT NULL REFERENCES units (id),
    role                    VARCHAR(20)     NOT NULL,
    contribution_percent    NUMERIC(5, 2),
    PRIMARY KEY (initiative_id, unit_id)
);

CREATE TABLE status_histories (
    id              BIGSERIAL       PRIMARY KEY,
    initiative_id   BIGINT          NOT NULL REFERENCES initiatives (id),
    old_status      VARCHAR(30),
    new_status      VARCHAR(30)     NOT NULL,
    changed_by      VARCHAR(100),
    changed_at      TIMESTAMP       NOT NULL,
    comment         TEXT
);
