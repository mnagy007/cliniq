package com.cliniq.persistence.provider;

import com.cliniq.application.provider.port.out.ProviderRepository;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public class JpaProviderAdapter implements ProviderRepository {

    private final JpaProviderRepository jpaProviderRepository;

    public JpaProviderAdapter(JpaProviderRepository jpaProviderRepository) {
        this.jpaProviderRepository = jpaProviderRepository;
    }

    @Override
    public void save(Provider provider) {
        JpaProvider jpaProvider = JpaProviderMapper.toJpa(provider);
        jpaProviderRepository.save(jpaProvider);
    }

    @Override
    public Optional<Provider> findById(TenantId tenantId, ProviderId id) {
        return jpaProviderRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(JpaProviderMapper::toDomain);
    }
}