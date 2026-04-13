package com.cliniq.application.provider;

import com.cliniq.application.provider.command.SyncProviderCommand;
import com.cliniq.application.provider.port.in.QueryProviderUseCase;
import com.cliniq.application.provider.port.in.SyncProviderUseCase;
import com.cliniq.application.provider.port.out.ProviderRepository;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.exception.ProviderNotFoundException;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public class ProviderService implements QueryProviderUseCase, SyncProviderUseCase {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public Provider findById(TenantId tenantId, ProviderId providerId) {
        return providerRepository.findById(tenantId, providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
    }

    @Override
    public void sync(SyncProviderCommand command) {
        Optional<Provider> providerOptional = providerRepository.findById(command.tenantId(), command.providerId());
        if (providerOptional.isPresent()) {
            Provider provider = providerOptional.get();
            provider.syncFrom(command.name(), command.specialty(), command.availabilitySlots());
            providerRepository.save(provider);
        } else {
            Provider provider = Provider.create(command.tenantId(), command.name(),
                    command.specialty(), command.availabilitySlots());
            providerRepository.save(provider);
        }
    }
}
