package com.cliniq.application.notification;

import com.cliniq.application.notification.command.ScheduleReminderCommand;
import com.cliniq.application.notification.port.in.QueryReminderUseCase;
import com.cliniq.application.notification.port.in.ScheduleReminderUseCase;
import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.application.notification.port.out.ReminderRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.OutboxEntry;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.shared.domain.TenantId;

import java.util.List;

public class ReminderService implements ScheduleReminderUseCase, QueryReminderUseCase {

    private final ReminderRepository reminderRepository;
    private final OutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;

    public ReminderService(ReminderRepository reminderRepository,
                          OutboxRepository outboxRepository,
                          DomainEventPublisher eventPublisher) {
        this.reminderRepository = reminderRepository;
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReminderId schedule(ScheduleReminderCommand command) {
        Reminder reminder = Reminder.schedule(
                command.tenantId(),
                command.appointmentId(),
                command.patientId(),
                command.channel());

        reminderRepository.save(reminder);

        OutboxEntry outboxEntry = OutboxEntry.create(
                command.tenantId(),
                "Reminder",
                reminder.getId().value().toString(),
                "ReminderScheduled",
                reminder.getId().value().toString());
        outboxRepository.save(outboxEntry);
        reminder.pullDomainEvents();

        return reminder.getId();
    }

    @Override
    public List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId) {
        return reminderRepository.findByAppointment(tenantId, appointmentId);
    }
}