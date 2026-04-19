package com.cliniq.domain.notification;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.event.ReminderDispatched;
import com.cliniq.domain.notification.event.ReminderFailed;
import com.cliniq.domain.notification.event.ReminderScheduled;
import com.cliniq.domain.notification.exception.NotificationDomainException;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.AggregateRoot;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.shared.validation.Preconditions;

public class Reminder extends AggregateRoot<ReminderId> {

    private final ReminderId id;
    private final TenantId tenantId;
    private final AppointmentId appointmentId;
    private final PatientId patientId;
    private final Channel channel;
    private ReminderStatus status;

    private Reminder(ReminderId id, TenantId tenantId, AppointmentId appointmentId,
                     PatientId patientId, Channel channel, ReminderStatus status) {
        this.id = id;
        this.tenantId = tenantId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.channel = channel;
        this.status = status;
    }

    public static Reminder schedule(TenantId tenantId, AppointmentId appointmentId,
                                     PatientId patientId, Channel channel) {
        Preconditions.requireNonNull(tenantId, "tenantId");
        Preconditions.requireNonNull(appointmentId, "appointmentId");
        Preconditions.requireNonNull(patientId, "patientId");
        Preconditions.requireNonNull(channel, "channel");

        ReminderId id = ReminderId.generate();
        Reminder reminder = new Reminder(id, tenantId, appointmentId, patientId,
                channel, new ReminderStatus.Pending());
        reminder.registerEvent(ReminderScheduled.of(tenantId, id, appointmentId, patientId, channel));
        return reminder;
    }

    public void dispatch() {
        if (!(this.status instanceof ReminderStatus.Pending)) {
            throw new NotificationDomainException("INVALID_DISPATCH",
                    "Cannot dispatch reminder not in Pending status");
        }
        this.status = new ReminderStatus.Dispatched();
        this.registerEvent(ReminderDispatched.of(tenantId, id));
    }

    public void markDelivered() {
        if (!(this.status instanceof ReminderStatus.Dispatched)) {
            throw new NotificationDomainException("INVALID_DELIVERY",
                    "Cannot mark delivered when status is not Dispatched");
        }
        this.status = new ReminderStatus.Delivered();
    }

    public void markFailed(FailureReason reason) {
        Preconditions.requireNonNull(reason, "reason");
        if (!(this.status instanceof ReminderStatus.Dispatched)) {
            throw new NotificationDomainException("INVALID_FAILURE",
                    "Cannot mark failed when status is not Dispatched");
        }
        this.status = new ReminderStatus.Failed(reason);
        this.registerEvent(ReminderFailed.of(tenantId, id, reason));
    }

    @Override
    public ReminderId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public AppointmentId getAppointmentId() {
        return appointmentId;
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public Channel getChannel() {
        return channel;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public static Reminder reconstruct(ReminderId id, TenantId tenantId, AppointmentId appointmentId,
                                        PatientId patientId, Channel channel, ReminderStatus status) {
        return new Reminder(id, tenantId, appointmentId, patientId, channel, status);
    }
}