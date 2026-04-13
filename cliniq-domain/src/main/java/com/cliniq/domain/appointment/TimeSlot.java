package com.cliniq.domain.appointment;

import com.cliniq.domain.appointment.exception.AppointmentDomainException;
import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) implements ValueObject {

    public TimeSlot {
        Preconditions.requireNonNull(date, "date cannot be null");
        Preconditions.requireNonNull(startTime, "startTime cannot be null");
        Preconditions.requireNonNull(endTime, "endTime cannot be null");

        if (endTime.isBefore(startTime)) {
            throw new AppointmentDomainException("INVALID_TIME_SLOT", "endTime cannot be before start time");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new AppointmentDomainException("INVALID_TIME_SLOT", "date cannot be in the past");
        }
    }
}
