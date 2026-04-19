package com.cliniq.web.appointment.dto;

import java.util.UUID;

public record PrescriptionResponse(
        UUID prescriptionId,
        String ndcCode,
        String brandName,
        String genericName,
        String dosage,
        String instructions
) {}