package com.cliniq.domain.patient.exception;

import com.cliniq.domain.patient.PatientId;

public class PatientNotFoundException extends PatientDomainException {

    public PatientNotFoundException(PatientId id) {
        super("PATIENT_NOT_FOUND", "Patient not found: " + id.value());
    }
}