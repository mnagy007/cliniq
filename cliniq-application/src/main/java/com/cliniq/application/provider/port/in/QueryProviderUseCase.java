package com.cliniq.application.provider.port.in;

import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

public interface QueryProviderUseCase {
    Provider findById(TenantId tenantId, ProviderId id);
}