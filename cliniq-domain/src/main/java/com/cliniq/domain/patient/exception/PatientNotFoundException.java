package com.cliniq.domain.patient.exception;

import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.DomainException;

public class PatientNotFoundException extends DomainException {
    private static final String ERROR_CODE = "PATIENT_NOT_FOUND";
    public PatientNotFoundException(PatientId patientId) {
        super(ERROR_CODE, "Patient not found: " + patientId.value());
    }
}
