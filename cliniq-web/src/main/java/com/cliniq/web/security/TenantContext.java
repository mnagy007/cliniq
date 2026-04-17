package com.cliniq.web.security;

import com.cliniq.shared.domain.TenantId;

public final class TenantContext {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(TenantId id) {
        CURRENT.set(id);
    }

    public static TenantId require() {
        TenantId id = CURRENT.get();
        if (id == null) throw new TenantResolutionException("No tenant in context");
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
