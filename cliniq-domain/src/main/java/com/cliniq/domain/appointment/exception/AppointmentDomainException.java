package com.cliniq.domain.appointment.exception;

import com.cliniq.shared.domain.DomainException;

public class AppointmentDomainException extends DomainException {
    public AppointmentDomainException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
