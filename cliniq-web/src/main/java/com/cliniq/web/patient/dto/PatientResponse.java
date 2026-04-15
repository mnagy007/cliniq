package com.cliniq.web.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    UUID tenantId,
    String givenName,
    String familyName,
    LocalDate dateOfBirth,
    String gender,
    String phoneNumber,
    String emailAddress,
    String medicalRecordNumber,
    String preferredChannel,
    Boolean optedOut
) {}
