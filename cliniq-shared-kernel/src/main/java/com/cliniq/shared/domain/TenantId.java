package com.cliniq.shared.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of a tenant in the multi-tenant system.
 *
 * <p>Carried explicitly on every aggregate and passed as a method parameter
 * through every repository port. No component in the domain or application
 * layer resolves the current tenant from a thread-local or framework context.
 */
public record TenantId(UUID value) {

    public TenantId {
        Objects.requireNonNull(value, "TenantId value must not be null");
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public static TenantId of(String value) {
        return new TenantId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
