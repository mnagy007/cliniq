CREATE TABLE IF NOT EXISTS providers (
    id          UUID         NOT NULL,
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    given_name  VARCHAR(255) NOT NULL,
    family_name VARCHAR(255) NOT NULL,
    specialty   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS provider_availability_slots (
    id          UUID   NOT NULL DEFAULT gen_random_uuid(),
    provider_id UUID   NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,
    start_time  TIME   NOT NULL,
    end_time    TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_providers_tenant ON providers(tenant_id, id);