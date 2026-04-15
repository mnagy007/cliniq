package com.cliniq.persistence.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReminderRepository extends JpaRepository<JpaReminder, UUID> {
    List<JpaReminder> findByTenantIdAndAppointmentId(UUID tenantId, UUID appointmentId);
    Optional<JpaReminder> findByIdAndTenantId(UUID id, UUID tenantId);
}