CREATE TABLE IF NOT EXISTS appointments (
    id                      UUID         NOT NULL,
    tenant_id               UUID         NOT NULL REFERENCES tenants(id),
    patient_id              UUID         NOT NULL,
    provider_id             UUID         NOT NULL,
    slot_date               DATE         NOT NULL,
    slot_start              TIME         NOT NULL,
    slot_end                TIME         NOT NULL,
    type                    VARCHAR(50)  NOT NULL,
    status                  VARCHAR(50)  NOT NULL,
    cancellation_code       VARCHAR(100),
    cancellation_description VARCHAR(255),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version                 BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_appointments_tenant ON appointments(tenant_id, id);
CREATE INDEX idx_appointments_patient ON appointments(tenant_id, patient_id);
CREATE INDEX idx_appointments_provider_date ON appointments(tenant_id, provider_id, slot_date);