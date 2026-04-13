package com.cliniq.application.appointment.port.out;

import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

public interface CalendarSyncPort {
    void syncAppointment(Appointment appointment);
    void deleteEvent(AppointmentId appointmentId, TenantId tenantId);
}