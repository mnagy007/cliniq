package com.cliniq.domain.appointment.exception;

import com.cliniq.domain.appointment.AppointmentStatus;

public class InvalidStatusTransitionException extends AppointmentDomainException {

    public InvalidStatusTransitionException(AppointmentStatus current, String attemptedAction) {
        super("INVALID_STATUS_TRANSITION",
                "Cannot " + attemptedAction + " when status is " + current.getClass().getSimpleName());
    }
}