package com.cliniq.domain.patient;

import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

import java.time.LocalDate;

public record PersonalInfo(String givenName, String familyName, LocalDate dateOfBirth, Gender gender)
        implements ValueObject {

    public PersonalInfo {
        Preconditions.requireNonNull(givenName, "givenName");
        Preconditions.requireNonNull(familyName, "familyName");
        Preconditions.requireNonNull(dateOfBirth, "dateOfBirth");
        Preconditions.requireNonNull(gender, "gender");
    }
}