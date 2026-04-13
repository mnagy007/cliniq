package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.CancellationReason;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record AppointmentCancelled(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        AppointmentId appointmentId,
        CancellationReason reason
) implements AppointmentEvent {

    public static AppointmentCancelled of(TenantId tenantId, AppointmentId appointmentId,
                                            CancellationReason reason) {
        return new AppointmentCancelled(UUID.randomUUID(), Instant.now(), tenantId, appointmentId, reason);
    }
}