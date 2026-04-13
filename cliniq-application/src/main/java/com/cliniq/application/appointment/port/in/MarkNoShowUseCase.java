package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.MarkNoShowCommand;

public interface MarkNoShowUseCase {
    void markNoShow(MarkNoShowCommand command);
}