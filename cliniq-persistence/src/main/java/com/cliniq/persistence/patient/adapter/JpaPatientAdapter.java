package com.cliniq.persistence.patient.adapter;

import com.cliniq.application.patient.port.out.PatientRepository;
import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.persistence.patient.entity.JpaPatient;
import com.cliniq.persistence.patient.mapper.JpaPatientMapper;
import com.cliniq.persistence.patient.repository.JpaPatientRepository;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public class JpaPatientAdapter implements PatientRepository {

    private final JpaPatientRepository jpaPatientRepository;

    public JpaPatientAdapter(JpaPatientRepository jpaPatientRepository) {
        this.jpaPatientRepository = jpaPatientRepository;
    }

    @Override
    public void save(Patient patient) {
        JpaPatient jpaPatient = JpaPatientMapper.toJpa(patient);
        jpaPatientRepository.save(jpaPatient);
    }

    @Override
    public Optional<Patient> findById(TenantId tenantId, PatientId id) {
        return jpaPatientRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(JpaPatientMapper::toDomain);
    }
}
