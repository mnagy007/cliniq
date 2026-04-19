CREATE TABLE IF NOT EXISTS patients (
    id                    UUID         NOT NULL,
    tenant_id             UUID         NOT NULL REFERENCES tenants(id),
    given_name            VARCHAR(255) NOT NULL,
    family_name           VARCHAR(255) NOT NULL,
    dob                   DATE,
    gender                VARCHAR(50),
    phone_number          VARCHAR(20),
    email_address         VARCHAR(255),
    medical_record_number VARCHAR(100),
    preferred_channel     VARCHAR(10),
    opted_out             BOOLEAN      NOT NULL DEFAULT false,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version               BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_patients_tenant ON patients(tenant_id, id);
