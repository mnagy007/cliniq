package com.cliniq.domain.patient.exception;

import com.cliniq.shared.domain.DomainException;

public class PatientDomainException extends DomainException {

    public PatientDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}