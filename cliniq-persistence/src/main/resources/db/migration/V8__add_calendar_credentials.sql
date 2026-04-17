CREATE TABLE IF NOT EXISTS calendar_credentials (
    tenant_id          UUID         NOT NULL PRIMARY KEY REFERENCES tenants(id),
    encrypted_tokens   TEXT         NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);