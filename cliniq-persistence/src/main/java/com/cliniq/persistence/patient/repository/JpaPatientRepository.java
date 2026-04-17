package com.cliniq.persistence.patient.repository;

import com.cliniq.persistence.patient.entity.JpaPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaPatientRepository extends JpaRepository<JpaPatient, UUID> {
    Optional<JpaPatient> findByIdAndTenantId(UUID id, UUID tenantId);
}
