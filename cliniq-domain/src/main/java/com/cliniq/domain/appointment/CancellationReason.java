package com.cliniq.domain.appointment;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record CancellationReason(String code, String description) implements ValueObject {
    public CancellationReason {
        Preconditions.requireNotBlank(code, "Cancellation reason code");
        Preconditions.requireNotBlank(description, "Cancellation reason description");
    }
}
