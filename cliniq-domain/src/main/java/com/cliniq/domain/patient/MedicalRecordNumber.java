package com.cliniq.domain.patient;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record MedicalRecordNumber(String value) implements ValueObject {

    public MedicalRecordNumber {
        Preconditions.requireNotBlank(value, "medical record number");
    }
}