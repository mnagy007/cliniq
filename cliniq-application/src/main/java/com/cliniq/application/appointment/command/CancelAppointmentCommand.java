package com.cliniq.application.appointment.command;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.CancellationReason;
import com.cliniq.shared.domain.TenantId;

public record CancelAppointmentCommand(
        TenantId tenantId,
        AppointmentId appointmentId,
        CancellationReason reason
) {}