package com.cliniq.persistence.notification;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.FailureReason;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.domain.notification.ReminderStatus;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

public final class JpaReminderMapper {

    private JpaReminderMapper() {
    }

    public static JpaReminder toJpa(Reminder reminder) {
        ReminderStatus status = reminder.getStatus();
        String failureCode = null;
        String failureMessage = null;

        if (status instanceof ReminderStatus.Failed failed) {
            failureCode = failed.reason().code();
            failureMessage = failed.reason().message();
        }

        return new JpaReminder(
                reminder.getId().value(),
                reminder.getTenantId().value(),
                reminder.getAppointmentId().value(),
                reminder.getPatientId().value(),
                channelToString(reminder.getChannel()),
                statusToString(status),
                failureCode,
                failureMessage);
    }

    public static Reminder toDomain(JpaReminder jpa) {
        ReminderId id = ReminderId.of(jpa.getId());
        TenantId tenantId = TenantId.of(jpa.getTenantId());
        AppointmentId appointmentId = AppointmentId.of(jpa.getAppointmentId());
        PatientId patientId = PatientId.of(jpa.getPatientId());
        Channel channel = stringToChannel(jpa.getChannel());
        ReminderStatus status = stringToStatus(jpa.getStatus(), jpa.getFailureCode(), jpa.getFailureMessage());

        return Reminder.reconstruct(id, tenantId, appointmentId, patientId, channel, status);
    }

    static String channelToString(Channel channel) {
        if (channel instanceof Channel.Email) return "EMAIL";
        return "SMS";
    }

    static Channel stringToChannel(String value) {
        if ("EMAIL".equals(value)) return Channel.Email.INSTANCE;
        return Channel.Sms.INSTANCE;
    }

    static String statusToString(ReminderStatus status) {
        if (status instanceof ReminderStatus.Dispatched) return "DISPATCHED";
        if (status instanceof ReminderStatus.Delivered) return "DELIVERED";
        if (status instanceof ReminderStatus.Failed) return "FAILED";
        return "PENDING";
    }

    static ReminderStatus stringToStatus(String value, String failureCode, String failureMessage) {
        return switch (value) {
            case "DISPATCHED" -> new ReminderStatus.Dispatched();
            case "DELIVERED" -> new ReminderStatus.Delivered();
            case "FAILED" -> new ReminderStatus.Failed(new FailureReason(failureCode, failureMessage));
            default -> new ReminderStatus.Pending();
        };
    }
}