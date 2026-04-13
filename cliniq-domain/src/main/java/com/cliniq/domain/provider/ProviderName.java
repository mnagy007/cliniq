package com.cliniq.domain.provider;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record ProviderName(String givenName, String familyName) implements ValueObject {

    public ProviderName {
        Preconditions.requireNotBlank(givenName, "givenName");
        Preconditions.requireNotBlank(familyName, "familyName");
    }
}