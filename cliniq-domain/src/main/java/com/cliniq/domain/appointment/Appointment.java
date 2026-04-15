package com.cliniq.domain.appointment;

import com.cliniq.domain.appointment.event.*;
import com.cliniq.domain.appointment.exception.AppointmentDomainException;
import com.cliniq.domain.appointment.exception.InvalidStatusTransitionException;
import com.cliniq.domain.appointment.exception.SlotUnavailableException;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.AggregateRoot;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.shared.validation.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Appointment extends AggregateRoot<AppointmentId> {

    private AppointmentId id;
    private final TenantId tenantId;
    private final PatientId patientId;
    private final ProviderId providerId;
    private final TimeSlot timeSlot;
    private final AppointmentType appointmentType;
    private AppointmentStatus appointmentStatus;
    private List<Prescription> prescriptions;
    private long version;

    private Appointment(AppointmentId id,
                        TenantId tenantId,
                        PatientId patientId,
                        ProviderId providerId,
                        TimeSlot timeSlot,
                        AppointmentType appointmentType,
                        AppointmentStatus appointmentStatus,
                        List<Prescription> prescriptions,
                        long version) {
        this.id = id;
        this.tenantId = tenantId;
        this.patientId = patientId;
        this.providerId = providerId;
        this.timeSlot = timeSlot;
        this.appointmentType = appointmentType;
        this.appointmentStatus = appointmentStatus;
        this.prescriptions = prescriptions;
        this.version = version;
    }

    public static Appointment schedule(TenantId tenantId, PatientId patientId, ProviderId providerId,
                                       TimeSlot timeSlot, AppointmentType appointmentType, Provider provider) {
        Preconditions.requireNonNull(tenantId, "tenantId");
        Preconditions.requireNonNull(patientId, "patientId");
        Preconditions.requireNonNull(providerId, "providerId");
        Preconditions.requireNonNull(timeSlot, "timeSlot");
        Preconditions.requireNonNull(appointmentType, "appointmentType");
        Preconditions.requireNonNull(provider, "provider");

        if (!provider.isAvailable(timeSlot)) {
            throw new SlotUnavailableException(timeSlot);
        }

        Appointment appointment = new Appointment(
                AppointmentId.generate(), tenantId, patientId, providerId, timeSlot, appointmentType,
                new AppointmentStatus.Scheduled(), new ArrayList<>(), 0L);
        appointment.registerEvent(AppointmentScheduled.of(tenantId, appointment.id, patientId, providerId,
                timeSlot, appointmentType));
        return appointment;
    }

    public void confirm() {
        if (!(this.appointmentStatus instanceof AppointmentStatus.Scheduled)) {
            throw new InvalidStatusTransitionException(this.appointmentStatus, "confirm");
        }
        this.appointmentStatus = new AppointmentStatus.Confirmed();
        this.registerEvent(AppointmentConfirmed.of(tenantId, id));
    }

    public void cancel(CancellationReason cancellationReason) {
        if (this.appointmentStatus instanceof AppointmentStatus.Scheduled
                || this.appointmentStatus instanceof AppointmentStatus.Confirmed) {
            Preconditions.requireNonNull(cancellationReason, "cancellationReason");
            this.appointmentStatus = new AppointmentStatus.Cancelled(cancellationReason);
            this.registerEvent(AppointmentCancelled.of(tenantId, id, cancellationReason));
        } else {
        throw new InvalidStatusTransitionException(this.appointmentStatus,
                "Invalid appointment status for cancel");
        }
    }

    public void complete() {
        if (!(this.appointmentStatus instanceof AppointmentStatus.Confirmed)) {
            throw new InvalidStatusTransitionException(this.appointmentStatus, "complete");
        }
        this.appointmentStatus = new AppointmentStatus.Completed();
        this.registerEvent(AppointmentCompleted.of(tenantId, id));
    }

    public void markNoShow() {
        if (!(this.appointmentStatus instanceof AppointmentStatus.Confirmed)) {
            throw new InvalidStatusTransitionException(this.appointmentStatus, "markNoShow");
        }
        this.appointmentStatus = new AppointmentStatus.NoShow();
        this.registerEvent(AppointmentMarkedNoShow.of(tenantId, id));
    }

    public PrescriptionId addPrescription(MedicationReference medication, String dosage, String instructions) {
        if (!(this.appointmentStatus instanceof AppointmentStatus.Confirmed)
                && !(this.appointmentStatus instanceof AppointmentStatus.Completed)) {
            throw new AppointmentDomainException("INVALID_PRESCRIPTION_ADD",
                    "Cannot add prescription to appointment in status: " + appointmentStatus.getClass().getSimpleName());
        }
        Preconditions.requireNonNull(medication, "medication");
        Preconditions.requireNotBlank(dosage, "dosage");
        Preconditions.requireNotBlank(instructions, "instructions");
        Prescription prescription = Prescription.create(medication, dosage, instructions);
        prescriptions.add(prescription);
        return prescription.getId();
    }

    @Override
    public AppointmentId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public ProviderId getProviderId() {
        return providerId;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public List<Prescription> getPrescriptions() {
        return Collections.unmodifiableList(prescriptions);
    }

    public long getVersion() {
        return version;
    }

    public static Appointment reconstruct(AppointmentId id, TenantId tenantId, PatientId patientId,
                                          ProviderId providerId, TimeSlot timeSlot,
                                          AppointmentType appointmentType,
                                          AppointmentStatus appointmentStatus,
                                          List<Prescription> prescriptions, long version) {
        return new Appointment(id, tenantId, patientId, providerId, timeSlot, appointmentType,
                appointmentStatus, prescriptions, version);
    }
}
