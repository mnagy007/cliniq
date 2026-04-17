package com.cliniq.web.medication.dto;

public record MedicationResponse(
        String ndcCode,
        String brandName,
        String genericName
) {}