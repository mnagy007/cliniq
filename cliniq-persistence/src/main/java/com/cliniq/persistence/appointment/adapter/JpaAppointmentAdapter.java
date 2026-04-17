package com.cliniq.persistence.appointment.adapter;

import com.cliniq.application.appointment.port.out.AppointmentRepository;
import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.persistence.appointment.entity.JpaAppointment;
import com.cliniq.persistence.appointment.entity.JpaPrescription;
import com.cliniq.persistence.appointment.mapper.JpaAppointmentMapper;
import com.cliniq.persistence.appointment.repository.JpaAppointmentRepository;
import com.cliniq.persistence.appointment.repository.JpaPrescriptionRepository;
import com.cliniq.shared.domain.TenantId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaAppointmentAdapter implements AppointmentRepository {

    private final JpaAppointmentRepository jpaAppointmentRepository;
    private final JpaPrescriptionRepository jpaPrescriptionRepository;

    public JpaAppointmentAdapter(JpaAppointmentRepository jpaAppointmentRepository,
                                  JpaPrescriptionRepository jpaPrescriptionRepository) {
        this.jpaAppointmentRepository = jpaAppointmentRepository;
        this.jpaPrescriptionRepository = jpaPrescriptionRepository;
    }

    @Override
    public void save(Appointment appointment) {
        JpaAppointment jpaAppointment = JpaAppointmentMapper.toJpa(appointment);
        jpaAppointmentRepository.save(jpaAppointment);

        List<com.cliniq.domain.appointment.Prescription> prescriptions = appointment.getPrescriptions();
        List<JpaPrescription> existingPrescriptions = jpaPrescriptionRepository.findByAppointmentId(appointment.getId().value());
        List<UUID> existingIds = existingPrescriptions.stream().map(JpaPrescription::getId).toList();
        List<UUID> newIds = prescriptions.stream().map(p -> p.getId().value()).toList();

        for (UUID existingId : existingIds) {
            if (!newIds.contains(existingId)) {
                jpaPrescriptionRepository.deleteById(existingId);
            }
        }

        for (com.cliniq.domain.appointment.Prescription prescription : prescriptions) {
            if (!existingIds.contains(prescription.getId().value())) {
                jpaPrescriptionRepository.save(JpaAppointmentMapper.toJpaPrescription(prescription, appointment));
            }
        }
    }

    @Override
    public Optional<Appointment> findById(TenantId tenantId, AppointmentId id) {
        return jpaAppointmentRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(jpa -> {
                    List<JpaPrescription> prescriptions = jpaPrescriptionRepository.findByAppointmentId(jpa.getId());
                    return JpaAppointmentMapper.toDomain(jpa, prescriptions);
                });
    }

    @Override
    public List<Appointment> findByPatient(TenantId tenantId, PatientId patientId) {
        return jpaAppointmentRepository.findByTenantIdAndPatientId(tenantId.value(), patientId.value())
                .stream()
                .map(jpa -> {
                    List<JpaPrescription> prescriptions = jpaPrescriptionRepository.findByAppointmentId(jpa.getId());
                    return JpaAppointmentMapper.toDomain(jpa, prescriptions);
                })
                .toList();
    }

    @Override
    public List<Appointment> findByProviderAndDate(TenantId tenantId, ProviderId providerId, LocalDate date) {
        return jpaAppointmentRepository.findByTenantIdAndProviderIdAndSlotDate(tenantId.value(), providerId.value(), date)
                .stream()
                .map(jpa -> {
                    List<JpaPrescription> prescriptions = jpaPrescriptionRepository.findByAppointmentId(jpa.getId());
                    return JpaAppointmentMapper.toDomain(jpa, prescriptions);
                })
                .toList();
    }
}
