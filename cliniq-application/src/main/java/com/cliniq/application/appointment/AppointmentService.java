package com.cliniq.application.appointment;

import com.cliniq.application.appointment.command.AddPrescriptionCommand;
import com.cliniq.application.appointment.command.CancelAppointmentCommand;
import com.cliniq.application.appointment.command.CompleteAppointmentCommand;
import com.cliniq.application.appointment.command.ConfirmAppointmentCommand;
import com.cliniq.application.appointment.command.MarkNoShowCommand;
import com.cliniq.application.appointment.command.ScheduleAppointmentCommand;
import com.cliniq.application.appointment.port.in.AddPrescriptionUseCase;
import com.cliniq.application.appointment.port.in.CancelAppointmentUseCase;
import com.cliniq.application.appointment.port.in.CompleteAppointmentUseCase;
import com.cliniq.application.appointment.port.in.ConfirmAppointmentUseCase;
import com.cliniq.application.appointment.port.in.MarkNoShowUseCase;
import com.cliniq.application.appointment.port.in.QueryAppointmentUseCase;
import com.cliniq.application.appointment.port.in.ScheduleAppointmentUseCase;
import com.cliniq.application.appointment.port.out.AppointmentRepository;
import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.application.provider.port.out.ProviderRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.PrescriptionId;
import com.cliniq.domain.appointment.exception.AppointmentNotFoundException;
import com.cliniq.domain.notification.OutboxEntry;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.exception.ProviderNotFoundException;
import com.cliniq.shared.domain.TenantId;

import java.time.LocalDate;
import java.util.List;

public class AppointmentService implements
        ScheduleAppointmentUseCase,
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
    public AppointmentId schedule(ScheduleAppointmentCommand command) {
        Provider provider = providerRepository
                .findById(command.tenantId(), command.providerId())
                .orElseThrow(() -> new ProviderNotFoundException(command.providerId()));

        Appointment appointment = Appointment.schedule(
                command.tenantId(),
                command.patientId(),
                command.providerId(),
                command.timeSlot(),
                command.type(),
                provider);

        appointmentRepository.save(appointment);

        // AppointmentScheduled downstream work (reminder creation) is handled
        // asynchronously via the outbox — do NOT also publish synchronously here,
        // or any future DomainEventPublisher adapter would process the event twice.
        OutboxEntry outboxEntry = OutboxEntry.create(
                command.tenantId(),
                "Appointment",
                appointment.getId().value().toString(),
                "AppointmentScheduled",
                appointment.getId().value().toString());
        outboxRepository.save(outboxEntry);
        appointment.pullDomainEvents(); // drain aggregate events; delivery is via outbox

        return appointment.getId();
    }

    @Override
    public void confirm(ConfirmAppointmentCommand command) {
        Appointment appointment = loadAppointment(command.tenantId(), command.appointmentId());
        appointment.confirm();
        appointmentRepository.save(appointment);
        eventPublisher.publish(appointment.pullDomainEvents());
    }

    @Override
    public void cancel(CancelAppointmentCommand command) {
        Appointment appointment = loadAppointment(command.tenantId(), command.appointmentId());
        appointment.cancel(command.reason());
        appointmentRepository.save(appointment);
        eventPublisher.publish(appointment.pullDomainEvents());
    }

    @Override
    public void complete(CompleteAppointmentCommand command) {
        Appointment appointment = loadAppointment(command.tenantId(), command.appointmentId());
        appointment.complete();
        appointmentRepository.save(appointment);
        eventPublisher.publish(appointment.pullDomainEvents());
    }

    @Override
    public void markNoShow(MarkNoShowCommand command) {
        Appointment appointment = loadAppointment(command.tenantId(), command.appointmentId());
        appointment.markNoShow();
        appointmentRepository.save(appointment);
        eventPublisher.publish(appointment.pullDomainEvents());
    }

    @Override
    public PrescriptionId addPrescription(AddPrescriptionCommand command) {
        Appointment appointment = loadAppointment(command.tenantId(), command.appointmentId());
        PrescriptionId prescriptionId = appointment.addPrescription(
                command.medication(), command.dosage(), command.instructions());
        appointmentRepository.save(appointment);
        return prescriptionId;
    }

    @Override
    public Appointment findById(TenantId tenantId, AppointmentId id) {
        return appointmentRepository.findById(tenantId, id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Override
    public List<Appointment> findByPatient(TenantId tenantId, PatientId patientId) {
        return appointmentRepository.findByPatient(tenantId, patientId);
    }

    @Override
    public List<Appointment> findByProvider(TenantId tenantId, ProviderId providerId, LocalDate date) {
        return appointmentRepository.findByProviderAndDate(tenantId, providerId, date);
    }

    private Appointment loadAppointment(TenantId tenantId, AppointmentId appointmentId) {
        return appointmentRepository.findById(tenantId, appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }
}
