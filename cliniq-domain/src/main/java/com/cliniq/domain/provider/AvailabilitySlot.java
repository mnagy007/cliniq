package com.cliniq.domain.provider;

import com.cliniq.domain.provider.exception.ProviderDomainException;
import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilitySlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) implements ValueObject {

    public AvailabilitySlot {
        Preconditions.requireNonNull(dayOfWeek, "dayOfWeek");
        Preconditions.requireNonNull(startTime, "startTime");
        Preconditions.requireNonNull(endTime, "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new ProviderDomainException("INVALID_AVAILABILITY_SLOT",
                    "AvailabilitySlot end must be after start");
        }
    }
}