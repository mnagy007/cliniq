package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AppointmentCompleted(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        AppointmentId appointmentId
) implements AppointmentEvent {

    public static AppointmentCompleted of(TenantId tenantId, AppointmentId appointmentId) {
        return new AppointmentCompleted(UUID.randomUUID(), Instant.now(), tenantId, appointmentId);
    }
}