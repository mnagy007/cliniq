package com.cliniq.domain.appointment.exception;

import com.cliniq.domain.appointment.AppointmentId;

public class AppointmentNotFoundException extends AppointmentDomainException {

    public AppointmentNotFoundException(AppointmentId id) {
        super("APPOINTMENT_NOT_FOUND", "Appointment not found: " + id.value());
    }
}