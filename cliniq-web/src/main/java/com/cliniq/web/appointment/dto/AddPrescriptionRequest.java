package com.cliniq.web.appointment.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPrescriptionRequest(
        @NotBlank String ndcCode,
        String brandName,
        String genericName,
        @NotBlank String dosage,
        @NotBlank String instructions
) {}