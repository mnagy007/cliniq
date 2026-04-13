package com.cliniq.application.notification.port.in;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.shared.domain.TenantId;

import java.util.List;

public interface QueryReminderUseCase {
    List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId);
}