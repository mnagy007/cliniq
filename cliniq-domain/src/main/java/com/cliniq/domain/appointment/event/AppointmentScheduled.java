package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.AppointmentType;
import com.cliniq.domain.appointment.TimeSlot;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AppointmentScheduled(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        AppointmentId appointmentId,
        PatientId patientId,
        ProviderId providerId,
        TimeSlot timeSlot,
        AppointmentType type
) implements AppointmentEvent {

    public static AppointmentScheduled of(TenantId tenantId, AppointmentId appointmentId,
                                           PatientId patientId, ProviderId providerId,
                                           TimeSlot timeSlot, AppointmentType type) {
        return new AppointmentScheduled(UUID.randomUUID(), Instant.now(), tenantId,
                appointmentId, patientId, providerId, timeSlot, type);
    }
}