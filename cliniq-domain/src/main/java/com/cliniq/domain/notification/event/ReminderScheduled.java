package com.cliniq.domain.notification.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ReminderScheduled(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        ReminderId reminderId,
        AppointmentId appointmentId,
        PatientId patientId,
        Channel channel
) implements NotificationEvent {

    public static ReminderScheduled of(TenantId tenantId, ReminderId reminderId,
                                         AppointmentId appointmentId, PatientId patientId,
                                         Channel channel) {
        return new ReminderScheduled(UUID.randomUUID(), Instant.now(), tenantId,
                reminderId, appointmentId, patientId, channel);
    }
}