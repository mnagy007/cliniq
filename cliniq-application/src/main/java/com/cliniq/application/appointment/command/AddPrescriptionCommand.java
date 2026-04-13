package com.cliniq.application.appointment.command;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.MedicationReference;
import com.cliniq.shared.domain.TenantId;

public record AddPrescriptionCommand(
        TenantId tenantId,
        AppointmentId appointmentId,
        MedicationReference medication,
        String dosage,
        String instructions
) {}