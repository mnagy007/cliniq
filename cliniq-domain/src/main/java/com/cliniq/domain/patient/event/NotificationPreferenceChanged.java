package com.cliniq.domain.patient.event;

import com.cliniq.domain.patient.NotificationPreference;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record NotificationPreferenceChanged(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        PatientId patientId,
        NotificationPreference newPreference
) implements PatientEvent {

    public static NotificationPreferenceChanged of(TenantId tenantId, PatientId patientId,
                                                     NotificationPreference newPreference) {
        return new NotificationPreferenceChanged(UUID.randomUUID(), Instant.now(), tenantId, patientId, newPreference);
    }
}