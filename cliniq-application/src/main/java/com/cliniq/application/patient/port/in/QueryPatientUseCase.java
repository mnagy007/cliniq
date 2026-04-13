package com.cliniq.application.patient.port.in;

import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

public interface QueryPatientUseCase {
    Patient findById(TenantId tenantId, PatientId id);
}