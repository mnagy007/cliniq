CREATE TABLE IF NOT EXISTS outbox_entries (
    id              UUID          NOT NULL,
    tenant_id       UUID          NOT NULL REFERENCES tenants(id),
    aggregate_type  VARCHAR(100)  NOT NULL,
    aggregate_id    VARCHAR(255)  NOT NULL,
    event_type      VARCHAR(100)  NOT NULL,
    payload         TEXT,
    occurred_at     TIMESTAMPTZ   NOT NULL,
    processed_at    TIMESTAMPTZ   NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_unprocessed ON outbox_entries (occurred_at) WHERE processed_at IS NULL;