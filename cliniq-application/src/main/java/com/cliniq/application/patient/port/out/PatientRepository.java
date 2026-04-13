package com.cliniq.application.patient.port.out;

import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public interface PatientRepository {
    void save(Patient patient);
    Optional<Patient> findById(TenantId tenantId, PatientId id);
}