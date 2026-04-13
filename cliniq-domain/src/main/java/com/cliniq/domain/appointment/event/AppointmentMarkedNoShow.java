package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AppointmentMarkedNoShow(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        AppointmentId appointmentId
) implements AppointmentEvent {

    public static AppointmentMarkedNoShow of(TenantId tenantId, AppointmentId appointmentId) {
        return new AppointmentMarkedNoShow(UUID.randomUUID(), Instant.now(), tenantId, appointmentId);
    }
}