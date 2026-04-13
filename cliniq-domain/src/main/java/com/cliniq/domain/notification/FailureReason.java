package com.cliniq.domain.notification;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record FailureReason(String code, String message) implements ValueObject {

    public FailureReason {
        Preconditions.requireNotBlank(code, "failure reason code");
        Preconditions.requireNotBlank(message, "failure reason message");
    }
}