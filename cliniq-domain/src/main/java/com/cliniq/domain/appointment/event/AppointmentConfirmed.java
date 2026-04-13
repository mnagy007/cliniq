package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AppointmentConfirmed(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        AppointmentId appointmentId
) implements AppointmentEvent {

    public static AppointmentConfirmed of(TenantId tenantId, AppointmentId appointmentId) {
        return new AppointmentConfirmed(UUID.randomUUID(), Instant.now(), tenantId, appointmentId);
    }
}