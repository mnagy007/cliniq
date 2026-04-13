package com.cliniq.domain.patient;

import com.cliniq.domain.patient.exception.PatientDomainException;
import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record PhoneNumber(String value) implements ValueObject {

    private static final String E164_PATTERN = "^\\+[1-9]\\d{1,14}$";

    public PhoneNumber {
        Preconditions.requireNotBlank(value, "phone number");
        if (!value.matches(E164_PATTERN)) {
            throw new PatientDomainException("INVALID_PHONE_NUMBER",
                    "Phone number must be in E.164 format: " + value);
        }
    }
}