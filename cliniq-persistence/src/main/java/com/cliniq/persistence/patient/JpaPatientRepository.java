package com.cliniq.persistence.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaPatientRepository extends JpaRepository<JpaPatient, UUID> {
    Optional<JpaPatient> findByIdAndTenantId(UUID id, UUID tenantId);
}
