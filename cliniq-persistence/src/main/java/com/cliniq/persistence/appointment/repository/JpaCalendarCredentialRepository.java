package com.cliniq.persistence.appointment.repository;

import com.cliniq.persistence.appointment.entity.JpaCalendarCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCalendarCredentialRepository extends JpaRepository<JpaCalendarCredential, UUID> {
}
