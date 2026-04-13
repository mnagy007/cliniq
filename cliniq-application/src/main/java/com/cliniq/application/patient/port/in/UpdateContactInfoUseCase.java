package com.cliniq.application.patient.port.in;

import com.cliniq.application.patient.command.UpdateContactInfoCommand;

public interface UpdateContactInfoUseCase {
    void updateContactInfo(UpdateContactInfoCommand command);
}