package com.cliniq.application.notification.command;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

public record ScheduleReminderCommand(
        TenantId tenantId,
        AppointmentId appointmentId,
        PatientId patientId,
        Channel channel
) {}