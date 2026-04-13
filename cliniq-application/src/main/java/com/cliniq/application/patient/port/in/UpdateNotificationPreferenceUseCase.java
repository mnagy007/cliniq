package com.cliniq.application.patient.port.in;

import com.cliniq.application.patient.command.UpdateNotificationPreferenceCommand;

public interface UpdateNotificationPreferenceUseCase {
    void updatePreference(UpdateNotificationPreferenceCommand command);
}