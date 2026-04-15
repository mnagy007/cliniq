package com.cliniq.domain.patient.exception;

import com.cliniq.domain.patient.PatientId;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(PatientId patientId) {
        super("Patient not found: " + patientId.value());
    }
}
