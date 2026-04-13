package com.cliniq.domain.patient;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record PatientId(UUID value) implements ValueObject {

    public PatientId {
        Preconditions.requireNonNull(value, "PatientId value");
    }

    public static PatientId generate() {
        return new PatientId(UUID.randomUUID());
    }

    public static PatientId of(UUID value) {
        return new PatientId(value);
    }
}