package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.ConfirmAppointmentCommand;

public interface ConfirmAppointmentUseCase {
    void confirm(ConfirmAppointmentCommand command);
}