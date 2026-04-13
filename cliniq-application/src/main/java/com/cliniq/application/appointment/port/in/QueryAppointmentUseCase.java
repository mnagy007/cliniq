package com.cliniq.application.appointment.port.in;

import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.time.LocalDate;
import java.util.List;

public interface QueryAppointmentUseCase {
    Appointment findById(TenantId tenantId, AppointmentId id);

    List<Appointment> findByPatient(TenantId tenantId, PatientId patientId);

    List<Appointment> findByProvider(TenantId tenantId, ProviderId providerId, LocalDate date);
}