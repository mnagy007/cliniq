package com.cliniq.persistence.appointment;

import com.cliniq.domain.appointment.*;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JpaAppointmentMapper {

    private JpaAppointmentMapper() {
    }

    public static JpaAppointment toJpa(Appointment appointment) {
        AppointmentStatus status = appointment.getAppointmentStatus();
        String cancellationCode = null;
        String cancellationDescription = null;

        if (status instanceof AppointmentStatus.Cancelled cancelled) {
            cancellationCode = cancelled.reason().code();
            cancellationDescription = cancelled.reason().description();
        }

        String statusName = statusToName(status);

        TimeSlot slot = appointment.getTimeSlot();
        JpaAppointment jpa = new JpaAppointment(
                appointment.getId().value(),
                appointment.getTenantId().value(),
                appointment.getPatientId().value(),
                appointment.getProviderId().value(),
                slot.date(),
                slot.startTime(),
                slot.endTime(),
                appointment.getAppointmentType().name(),
                statusName,
                cancellationCode,
                cancellationDescription);
        return jpa;
    }

    public static Appointment toDomain(JpaAppointment jpa, List<JpaPrescription> jpaPrescriptions) {
        AppointmentId id = AppointmentId.of(jpa.getId());
        TenantId tenantId = TenantId.of(jpa.getTenantId());
        PatientId patientId = PatientId.of(jpa.getPatientId());
        ProviderId providerId = ProviderId.of(jpa.getProviderId());
        TimeSlot timeSlot = new TimeSlot(jpa.getSlotDate(), jpa.getSlotStart(), jpa.getSlotEnd());
        AppointmentType type = AppointmentType.valueOf(jpa.getAppointmentType());
        AppointmentStatus status = nameToStatus(jpa.getStatus(), jpa.getCancellationCode(), jpa.getCancellationDescription());

        List<Prescription> prescriptions = new ArrayList<>();
        if (jpaPrescriptions != null) {
            for (JpaPrescription jp : jpaPrescriptions) {
                prescriptions.add(Prescription.reconstruct(
                        PrescriptionId.of(jp.getId()),
                        new MedicationReference(jp.getNdcCode(), jp.getBrandName(), jp.getGenericName()),
                        jp.getDosage(),
                        jp.getInstructions()));
            }
        }

        return Appointment.reconstruct(id, tenantId, patientId, providerId, timeSlot, type, status, prescriptions, jpa.getVersion() != null ? jpa.getVersion() : 0L);
    }

    public static JpaPrescription toJpaPrescription(Prescription prescription, Appointment appointment) {
        return new JpaPrescription(
                prescription.getId().value(),
                appointment.getId().value(),
                appointment.getTenantId().value(),
                prescription.getMedication().ndcCode(),
                prescription.getMedication().brandName(),
                prescription.getMedication().genericName(),
                prescription.getDosage(),
                prescription.getInstructions());
    }

    static String statusToName(AppointmentStatus status) {
        if (status instanceof AppointmentStatus.Scheduled) return "SCHEDULED";
        if (status instanceof AppointmentStatus.Confirmed) return "CONFIRMED";
        if (status instanceof AppointmentStatus.Completed) return "COMPLETED";
        if (status instanceof AppointmentStatus.NoShow) return "NO_SHOW";
        if (status instanceof AppointmentStatus.Cancelled) return "CANCELLED";
        return "SCHEDULED";
    }

    static AppointmentStatus nameToStatus(String name, String cancellationCode, String cancellationDescription) {
        return switch (name) {
            case "CONFIRMED" -> new AppointmentStatus.Confirmed();
            case "COMPLETED" -> new AppointmentStatus.Completed();
            case "NO_SHOW" -> new AppointmentStatus.NoShow();
            case "CANCELLED" -> new AppointmentStatus.Cancelled(
                    new CancellationReason(cancellationCode, cancellationDescription));
            default -> new AppointmentStatus.Scheduled();
        };
    }
}