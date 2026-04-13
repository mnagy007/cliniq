package com.cliniq.domain.patient;

import com.cliniq.domain.patient.exception.PatientDomainException;
import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record EmailAddress(String value) implements ValueObject {

    public EmailAddress {
        Preconditions.requireNotBlank(value, "email");
        int atIndex = value.indexOf('@');
        if (atIndex < 0 || atIndex != value.lastIndexOf('@') || value.lastIndexOf('.') <= atIndex) {
            throw new PatientDomainException("INVALID_EMAIL_ADDRESS",
                    "Invalid email address: " + value);
        }
    }
}