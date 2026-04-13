package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.CompleteAppointmentCommand;

public interface CompleteAppointmentUseCase {
    void complete(CompleteAppointmentCommand command);
}