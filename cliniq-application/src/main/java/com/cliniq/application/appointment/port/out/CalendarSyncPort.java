package com.cliniq.application.appointment.port.out;

import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.shared.domain.TenantId;

public interface CalendarSyncPort {
    String syncAppointment(Appointment appointment);
    void deleteEvent(String eventId, TenantId tenantId);
}