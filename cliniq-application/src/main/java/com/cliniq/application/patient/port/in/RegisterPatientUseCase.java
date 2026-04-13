package com.cliniq.application.patient.port.in;

import com.cliniq.application.patient.command.RegisterPatientCommand;
import com.cliniq.domain.patient.PatientId;

public interface RegisterPatientUseCase {
    PatientId register(RegisterPatientCommand command);
}