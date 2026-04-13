package com.cliniq.domain.notification;

public sealed interface ReminderStatus
        permits ReminderStatus.Pending, ReminderStatus.Dispatched,
                ReminderStatus.Delivered, ReminderStatus.Failed {

    record Pending() implements ReminderStatus {}

    record Dispatched() implements ReminderStatus {}

    record Delivered() implements ReminderStatus {}

    record Failed(FailureReason reason) implements ReminderStatus {}
}