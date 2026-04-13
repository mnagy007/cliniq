package com.cliniq.domain.patient.event;

import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record ContactInfoUpdated(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        PatientId patientId,
        ContactInfo newContactInfo
) implements PatientEvent {

    public static ContactInfoUpdated of(TenantId tenantId, PatientId patientId, ContactInfo newContactInfo) {
        return new ContactInfoUpdated(UUID.randomUUID(), Instant.now(), tenantId, patientId, newContactInfo);
    }
}