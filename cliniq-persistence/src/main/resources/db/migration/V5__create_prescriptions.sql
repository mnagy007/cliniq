CREATE TABLE IF NOT EXISTS prescriptions (
    id              UUID         NOT NULL,
    appointment_id  UUID         NOT NULL REFERENCES appointments(id),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id),
    ndc_code        VARCHAR(50),
    brand_name      VARCHAR(255),
    generic_name    VARCHAR(255),
    dosage          VARCHAR(255) NOT NULL,
    instructions    VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id)
);