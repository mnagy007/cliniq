package com.cliniq.domain.notification.event;

import com.cliniq.shared.domain.DomainEvent;
import com.cliniq.shared.domain.TenantId;

public sealed interface NotificationEvent extends DomainEvent
        permits ReminderScheduled, ReminderDispatched, ReminderFailed {
}