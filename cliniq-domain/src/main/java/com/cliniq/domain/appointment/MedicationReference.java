package com.cliniq.domain.appointment;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record MedicationReference(String ndcCode, String brandName, String genericName) implements ValueObject {

    public MedicationReference {
       Preconditions.requireNotBlank(ndcCode, "NDC code cannot be null");
    }
}
