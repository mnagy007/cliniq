package com.cliniq.application.notification.port.out;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.shared.domain.TenantId;

import java.util.List;

public interface ReminderRepository {
    void save(Reminder reminder);
    List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId);
}