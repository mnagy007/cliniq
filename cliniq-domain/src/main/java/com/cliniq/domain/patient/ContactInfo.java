package com.cliniq.domain.patient;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record ContactInfo(PhoneNumber phoneNumber, EmailAddress emailAddress) implements ValueObject {

    public ContactInfo {
        Preconditions.requireNonNull(phoneNumber, "phoneNumber");
        Preconditions.requireNonNull(emailAddress, "emailAddress");
    }
}