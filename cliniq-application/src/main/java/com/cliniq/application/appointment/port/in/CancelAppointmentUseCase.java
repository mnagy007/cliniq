package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.CancelAppointmentCommand;

public interface CancelAppointmentUseCase {
    void cancel(CancelAppointmentCommand command);
}