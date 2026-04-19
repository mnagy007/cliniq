package com.cliniq.application.notification.port.out;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.shared.domain.TenantId;

import java.util.List;
import java.util.Optional;

public interface ReminderRepository {
    void save(Reminder reminder);
    List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId);
    Optional<Reminder> findById(TenantId tenantId, ReminderId id);
}