package com.cliniq.application.appointment;

import com.cliniq.application.appointment.command.*;
import com.cliniq.application.appointment.port.in.*;
import com.cliniq.application.appointment.port.out.AppointmentRepository;
import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.application.provider.port.out.ProviderRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.PrescriptionId;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;

import java.time.LocalDate;
import java.util.List;

public class AppointmentService implements ScheduleAppointmentUseCase,
        ConfirmAppointmentUseCase,
        CancelAppointmentUseCase,
        CompleteAppointmentUseCase,
        MarkNoShowUseCase,
        AddPrescriptionUseCase,
        QueryAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final OutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ProviderRepository providerRepository,
                              OutboxRepository outboxRepository,
                              DomainEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.providerRepository = providerRepository;
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public void confirm(ConfirmAppointmentCommand command) {

    }

    @Override
    public PrescriptionId addPrescription(AddPrescriptionCommand command) {
        return null;
    }

    @Override
    public void cancel(CancelAppointmentCommand command) {

    }

    @Override
    public void complete(CompleteAppointmentCommand command) {

    }

    @Override
    public void markNoShow(MarkNoShowCommand command) {

    }

    @Override
    public Appointment findById(TenantId tenantId, AppointmentId id) {
        return null;
    }

    @Override
    public List<Appointment> findByPatient(TenantId tenantId, PatientId patientId) {
        return List.of();
    }

    @Override
    public List<Appointment> findByProvider(TenantId tenantId, ProviderId providerId, LocalDate date) {
        return List.of();
    }

    @Override
    public AppointmentId schedule(ScheduleAppointmentCommand command) {
        return null;
    }
}
