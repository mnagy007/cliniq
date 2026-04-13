package com.cliniq.application.patient.command;

import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

public record UpdateContactInfoCommand(
        TenantId tenantId,
        PatientId patientId,
        ContactInfo newContactInfo
) {}