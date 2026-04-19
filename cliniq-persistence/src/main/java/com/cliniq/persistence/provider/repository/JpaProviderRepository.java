package com.cliniq.persistence.provider.repository;

import com.cliniq.persistence.provider.entity.JpaProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaProviderRepository extends JpaRepository<JpaProvider, UUID> {
    Optional<JpaProvider> findByIdAndTenantId(UUID id, UUID tenantId);
}
