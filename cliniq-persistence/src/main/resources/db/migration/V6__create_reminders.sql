CREATE TABLE IF NOT EXISTS reminders (
    id               UUID         NOT NULL,
    tenant_id        UUID         NOT NULL REFERENCES tenants(id),
    appointment_id   UUID         NOT NULL,
    patient_id       UUID         NOT NULL,
    channel          VARCHAR(10)  NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    failure_code     VARCHAR(100),
    failure_message  VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version          BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_reminders_tenant ON reminders(tenant_id, id);