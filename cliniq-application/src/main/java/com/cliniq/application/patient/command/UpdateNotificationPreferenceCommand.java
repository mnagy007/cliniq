package com.cliniq.application.patient.command;

import com.cliniq.domain.patient.NotificationPreference;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

public record UpdateNotificationPreferenceCommand(
        TenantId tenantId,
        PatientId patientId,
        NotificationPreference newPreference
) {}