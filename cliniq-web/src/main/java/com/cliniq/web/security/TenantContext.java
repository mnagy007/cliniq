package com.cliniq.web.security;

import com.cliniq.shared.domain.TenantId;

import java.util.UUID;

/**
 * Stub tenant context - will be replaced with proper implementation (T115).
 */
public class TenantContext {

    private TenantContext() {}

    public static TenantId require() {
        // TODO: Replace with actual tenant resolution from security context (T115)
        return TenantId.of(UUID.randomUUID());
    }
}
