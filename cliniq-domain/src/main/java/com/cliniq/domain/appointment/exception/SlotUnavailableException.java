package com.cliniq.domain.appointment.exception;

import com.cliniq.domain.appointment.TimeSlot;

public class SlotUnavailableException extends AppointmentDomainException {

    public SlotUnavailableException(TimeSlot slot) {
        super("SLOT_UNAVAILABLE", "Time slot is not available: " + slot);
    }
}