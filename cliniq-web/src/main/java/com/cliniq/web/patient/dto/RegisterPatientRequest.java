package com.cliniq.web.patient.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record RegisterPatientRequest(
    @NotBlank String givenName,
    @NotBlank String familyName,
    LocalDate dateOfBirth,
    @NotBlank String gender,
    @NotBlank String phoneNumber,
    String emailAddress,
    String preferredChannel,
    Boolean optedOut
) {}
