package com.cliniq.application.notification.port.in;

import com.cliniq.application.notification.command.ScheduleReminderCommand;
import com.cliniq.domain.notification.ReminderId;

public interface ScheduleReminderUseCase {
    ReminderId schedule(ScheduleReminderCommand command);
}