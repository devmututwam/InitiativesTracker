-- V2: Add savings_records table

CREATE TABLE savings_records (
    id                      BIGSERIAL       PRIMARY KEY,
    initiative_id           BIGINT          NOT NULL REFERENCES initiatives (id),
    vendor_budget           NUMERIC(17, 2),
    internal_cost           NUMERIC(17, 2)  NOT NULL,
    incremental_expenses    NUMERIC(17, 2)  NOT NULL,
    saving_amount           NUMERIC(17, 2)  NOT NULL,
    calculated_by           VARCHAR(100),
    calculated_at           TIMESTAMP       NOT NULL,
    notes                   TEXT
);
