package com.cliniq.persistence.appointment.repository;

import com.cliniq.persistence.appointment.entity.JpaAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAppointmentRepository extends JpaRepository<JpaAppointment, UUID> {
    Optional<JpaAppointment> findByIdAndTenantId(UUID id, UUID tenantId);

    List<JpaAppointment> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);

    List<JpaAppointment> findByTenantIdAndProviderIdAndSlotDate(UUID tenantId, UUID providerId, LocalDate slotDate);
}
