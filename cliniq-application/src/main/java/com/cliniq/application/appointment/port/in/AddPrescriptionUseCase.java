package com.cliniq.application.appointment.port.in;

import com.cliniq.application.appointment.command.AddPrescriptionCommand;
import com.cliniq.domain.appointment.PrescriptionId;

public interface AddPrescriptionUseCase {
    PrescriptionId addPrescription(AddPrescriptionCommand command);
}