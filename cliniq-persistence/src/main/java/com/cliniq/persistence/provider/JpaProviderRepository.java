package com.cliniq.persistence.provider;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProviderRepository extends JpaRepository<JpaProvider, UUID> {
    Optional<JpaProvider> findByIdAndTenantId(UUID id, UUID tenantId);
}