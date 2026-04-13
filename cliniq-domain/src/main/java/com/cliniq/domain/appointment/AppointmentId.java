package com.cliniq.domain.appointment;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record AppointmentId(UUID value) implements ValueObject {

    public AppointmentId {
        Preconditions.requireNonNull(value, "AppointmentId value");
    }

    public static AppointmentId of(UUID value) {
        return new AppointmentId(value);
    }

    public static AppointmentId generate() {
        return new AppointmentId(UUID.randomUUID());
    }
}
