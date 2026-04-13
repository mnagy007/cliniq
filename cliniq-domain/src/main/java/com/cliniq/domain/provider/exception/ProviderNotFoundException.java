package com.cliniq.domain.provider.exception;

import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.DomainException;

public class ProviderNotFoundException extends DomainException {

    public ProviderNotFoundException(ProviderId id) {
        super("PROVIDER_NOT_FOUND", "Provider not found: " + id.value());
    }
}