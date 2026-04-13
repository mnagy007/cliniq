package com.cliniq.application.appointment.command;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

public record CompleteAppointmentCommand(
        TenantId tenantId,
        AppointmentId appointmentId
) {}