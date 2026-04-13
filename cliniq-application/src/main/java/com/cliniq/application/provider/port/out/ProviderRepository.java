package com.cliniq.application.provider.port.out;

import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public interface ProviderRepository {
    void save(Provider provider);
    Optional<Provider> findById(TenantId tenantId, ProviderId id);
}