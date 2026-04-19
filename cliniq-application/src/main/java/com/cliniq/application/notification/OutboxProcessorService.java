package com.cliniq.application.notification;

import com.cliniq.application.notification.command.ScheduleReminderCommand;
import com.cliniq.application.notification.port.out.NotificationDispatchPort;
import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.application.notification.port.out.ReminderRepository;
import com.cliniq.application.patient.port.out.PatientRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.FailureReason;
import com.cliniq.domain.notification.OutboxEntry;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class OutboxProcessorService {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessorService.class);

    private final OutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;
    private final ReminderRepository reminderRepository;
    private final ReminderService reminderService;
    private final NotificationDispatchPort notificationDispatchPort;
    private final PatientRepository patientRepository;

    public OutboxProcessorService(OutboxRepository outboxRepository,
                                  DomainEventPublisher eventPublisher,
                                  ReminderRepository reminderRepository,
                                  ReminderService reminderService,
                                  NotificationDispatchPort notificationDispatchPort,
                                  PatientRepository patientRepository) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.reminderRepository = reminderRepository;
        this.reminderService = reminderService;
        this.notificationDispatchPort = notificationDispatchPort;
        this.patientRepository = patientRepository;
    }

    public void processNext(int batchSize) {
        List<OutboxEntry> entries = outboxRepository.findUnprocessed(batchSize);
        log.info("Processing {} outbox entries", entries.size());

        for (OutboxEntry entry : entries) {
            try {
                processEntry(entry);
            } catch (Exception e) {
                log.error("Error processing outbox entry {}: {}", entry.getId().value(), e.getMessage(), e);
            }
        }
    }

    private void processEntry(OutboxEntry entry) {
        String eventType = entry.getEventType();
        TenantId tenantId = entry.getTenantId();

        if (eventType.contains("AppointmentScheduled")) {
            handleAppointmentScheduled(tenantId, entry);
        } else if (eventType.contains("ReminderScheduled")) {
            handleReminderScheduled(tenantId, entry);
        }

        entry.markProcessed();
        outboxRepository.save(entry);
    }

    private void handleAppointmentScheduled(TenantId tenantId, OutboxEntry entry) {
        UUID patientId = UUID.fromString(entry.getPayload());
        PatientId patientIdObj = PatientId.of(patientId);

        Patient patient = patientRepository.findById(tenantId, patientIdObj)
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + patientIdObj.value()));

        ScheduleReminderCommand command =
                new ScheduleReminderCommand(tenantId,
                        AppointmentId.of(UUID.fromString(entry.getAggregateId())),
                        patientIdObj,
                        patient.getNotificationPreference().preferredChannel());
        reminderService.schedule(command);
    }

    private void handleReminderScheduled(TenantId tenantId, OutboxEntry entry) {
        UUID reminderId = UUID.fromString(entry.getPayload());
        ReminderId reminderIdObj = ReminderId.of(reminderId);

        Reminder reminder = reminderRepository.findById(tenantId, reminderIdObj)
                .orElseThrow(() -> new IllegalStateException("Reminder not found: " + reminderIdObj.value()));

        PatientId patientId = reminder.getPatientId();
        Patient patient = patientRepository.findById(tenantId, patientId)
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + patientId.value()));

        ContactInfo contactInfo = patient.getContactInfo();

        var dispatchResult = notificationDispatchPort.dispatch(reminder, contactInfo);

        if (dispatchResult.success()) {
            reminder.markDelivered();
            reminderRepository.save(reminder);
            eventPublisher.publish(reminder.pullDomainEvents());
        } else {
            String errorMsg = dispatchResult.errorMessage() != null ? dispatchResult.errorMessage() : "unknown error";
            reminder.markFailed(new FailureReason("DISPATCH_ERROR", errorMsg));
            reminderRepository.save(reminder);
            eventPublisher.publish(reminder.pullDomainEvents());
        }
    }
}