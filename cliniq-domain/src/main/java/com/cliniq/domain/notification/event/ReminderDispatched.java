package com.cliniq.domain.notification.event;

import com.cliniq.domain.notification.ReminderId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ReminderDispatched(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        ReminderId reminderId
) implements NotificationEvent {

    public static ReminderDispatched of(TenantId tenantId, ReminderId reminderId) {
        return new ReminderDispatched(UUID.randomUUID(), Instant.now(), tenantId, reminderId);
    }
}