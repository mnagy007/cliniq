package com.cliniq.domain.appointment;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record PrescriptionId(UUID value) implements ValueObject {

    public PrescriptionId {
        Preconditions.requireNonNull(value, "PrescriptionId value");
    }

    public static PrescriptionId of(UUID value) {
        return new PrescriptionId(value);
    }

    public static PrescriptionId generate() {
        return new PrescriptionId(UUID.randomUUID());
    }
}