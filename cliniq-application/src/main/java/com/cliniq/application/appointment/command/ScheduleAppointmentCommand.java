package com.cliniq.application.appointment.command;

import com.cliniq.domain.appointment.AppointmentType;
import com.cliniq.domain.appointment.TimeSlot;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

public record ScheduleAppointmentCommand(
        TenantId tenantId,
        PatientId patientId,
        ProviderId providerId,
        TimeSlot timeSlot,
        AppointmentType type
) {}