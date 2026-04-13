package com.cliniq.domain.notification;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.util.UUID;

public record OutboxEntryId(UUID value) implements ValueObject {

    public OutboxEntryId {
        Preconditions.requireNonNull(value, "OutboxEntryId value");
    }

    public static OutboxEntryId generate() {
        return new OutboxEntryId(UUID.randomUUID());
    }

    public static OutboxEntryId of(UUID value) {
        return new OutboxEntryId(value);
    }
}