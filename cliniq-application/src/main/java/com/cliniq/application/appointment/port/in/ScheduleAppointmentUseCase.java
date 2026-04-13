package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.ScheduleAppointmentCommand;
import com.cliniq.domain.appointment.AppointmentId;

public interface ScheduleAppointmentUseCase {
    AppointmentId schedule(ScheduleAppointmentCommand command);
}