package com.cliniq.domain.patient.event;

import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.patient.PersonalInfo;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public record PatientRegistered(
        UUID eventId,
        Instant occurredAt,
        TenantId tenantId,
        PatientId patientId,
        PersonalInfo personalInfo,
        ContactInfo contactInfo
) implements PatientEvent {

    public static PatientRegistered of(TenantId tenantId, PatientId patientId, PersonalInfo personalInfo,
                                         ContactInfo contactInfo) {
        return new PatientRegistered(UUID.randomUUID(), Instant.now(), tenantId, patientId, personalInfo, contactInfo);
    }
}