package com.cliniq.domain.notification;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record ReminderId(UUID value) implements ValueObject {

    public ReminderId {
        Preconditions.requireNonNull(value, "ReminderId value");
    }

    public static ReminderId generate() {
        return new ReminderId(UUID.randomUUID());
    }

    public static ReminderId of(UUID value) {
        return new ReminderId(value);
    }
}