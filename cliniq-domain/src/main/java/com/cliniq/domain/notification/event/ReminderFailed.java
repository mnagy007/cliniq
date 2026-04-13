package com.cliniq.domain.notification.event;

import com.cliniq.domain.notification.FailureReason;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ReminderFailed(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        ReminderId reminderId,
        FailureReason reason
) implements NotificationEvent {

    public static ReminderFailed of(TenantId tenantId, ReminderId reminderId, FailureReason reason) {
        return new ReminderFailed(UUID.randomUUID(), Instant.now(), tenantId, reminderId, reason);
    }
}