package com.cliniq.domain.provider;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record ProviderId(UUID value) implements ValueObject {

    public ProviderId {
        Preconditions.requireNonNull(value, "ProviderId value");
    }

    public static ProviderId generate() {
        return new ProviderId(UUID.randomUUID());
    }

    public static ProviderId of(UUID value) {
        return new ProviderId(value);
    }
}