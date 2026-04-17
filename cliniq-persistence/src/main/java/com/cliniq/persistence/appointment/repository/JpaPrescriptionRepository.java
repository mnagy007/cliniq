package com.cliniq.persistence.appointment.repository;

import com.cliniq.persistence.appointment.entity.JpaPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaPrescriptionRepository extends JpaRepository<JpaPrescription, UUID> {
    List<JpaPrescription> findByAppointmentId(UUID appointmentId);
}
