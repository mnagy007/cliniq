package com.cliniq.application.patient.command;

import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.MedicalRecordNumber;
import com.cliniq.domain.patient.NotificationPreference;
import com.cliniq.domain.patient.PersonalInfo;
import com.cliniq.shared.domain.TenantId;

public record RegisterPatientCommand(
        TenantId tenantId,
        PersonalInfo personalInfo,
        ContactInfo contactInfo,
        MedicalRecordNumber medicalRecordNumber,
        NotificationPreference notificationPreference
) {}