package com.cliniq.web.appointment;

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
import com.cliniq.domain.appointment.Appointment;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.AppointmentStatus;
import com.cliniq.domain.appointment.AppointmentType;
import com.cliniq.domain.appointment.CancellationReason;
import com.cliniq.domain.appointment.MedicationReference;
import com.cliniq.domain.appointment.Prescription;
import com.cliniq.domain.appointment.TimeSlot;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.appointment.dto.AddPrescriptionRequest;
import com.cliniq.web.appointment.dto.AppointmentResponse;
import com.cliniq.web.appointment.dto.CancelAppointmentRequest;
import com.cliniq.web.appointment.dto.PrescriptionResponse;
import com.cliniq.web.appointment.dto.ScheduleAppointmentRequest;
import com.cliniq.web.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final ScheduleAppointmentUseCase scheduleUseCase;
    private final ConfirmAppointmentUseCase confirmUseCase;
    private final CancelAppointmentUseCase cancelUseCase;
    private final CompleteAppointmentUseCase completeUseCase;
    private final MarkNoShowUseCase markNoShowUseCase;
    private final AddPrescriptionUseCase addPrescriptionUseCase;
    private final QueryAppointmentUseCase queryUseCase;

    public AppointmentController(ScheduleAppointmentUseCase scheduleUseCase,
                                 ConfirmAppointmentUseCase confirmUseCase,
                                 CancelAppointmentUseCase cancelUseCase,
                                 CompleteAppointmentUseCase completeUseCase,
                                 MarkNoShowUseCase markNoShowUseCase,
                                 AddPrescriptionUseCase addPrescriptionUseCase,
                                 QueryAppointmentUseCase queryUseCase) {
        this.scheduleUseCase = scheduleUseCase;
        this.confirmUseCase = confirmUseCase;
        this.cancelUseCase = cancelUseCase;
        this.completeUseCase = completeUseCase;
        this.markNoShowUseCase = markNoShowUseCase;
        this.addPrescriptionUseCase = addPrescriptionUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> schedule(@Valid @RequestBody ScheduleAppointmentRequest request) {
        TenantId tenantId = TenantContext.require();
        TimeSlot timeSlot = new TimeSlot(request.date(), request.startTime(), request.endTime());
        AppointmentType type = AppointmentType.valueOf(request.type());

        ScheduleAppointmentCommand command = new ScheduleAppointmentCommand(
                tenantId, PatientId.of(request.patientId()), ProviderId.of(request.providerId()),
                timeSlot, type);

        AppointmentId id = scheduleUseCase.schedule(command);
        return ResponseEntity.created(null)
                .body(Map.of("appointmentId", id.value()));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        confirmUseCase.confirm(new ConfirmAppointmentCommand(tenantId, AppointmentId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id, @Valid @RequestBody CancelAppointmentRequest request) {
        TenantId tenantId = TenantContext.require();
        CancellationReason reason = new CancellationReason(request.code(), request.description());
        cancelUseCase.cancel(new CancelAppointmentCommand(tenantId, AppointmentId.of(id), reason));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        completeUseCase.complete(new CompleteAppointmentCommand(tenantId, AppointmentId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        markNoShowUseCase.markNoShow(new MarkNoShowCommand(tenantId, AppointmentId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/prescriptions")
    public ResponseEntity<Map<String, UUID>> addPrescription(@PathVariable UUID id,
                                                             @Valid @RequestBody AddPrescriptionRequest request) {
        TenantId tenantId = TenantContext.require();
        MedicationReference medication = new MedicationReference(
                request.ndcCode(), request.brandName(), request.genericName());

        var prescriptionId = addPrescriptionUseCase.addPrescription(
                new AddPrescriptionCommand(tenantId, AppointmentId.of(id), medication,
                        request.dosage(), request.instructions()));

        return ResponseEntity.created(null)
                .body(Map.of("prescriptionId", prescriptionId.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        Appointment appointment = queryUseCase.findById(tenantId, AppointmentId.of(id));
        return ResponseEntity.ok(toAppointmentResponse(appointment));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> queryAppointments(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID providerId,
            @RequestParam(required = false) LocalDate date) {
        TenantId tenantId = TenantContext.require();

        List<Appointment> appointments;
        if (providerId != null && date != null) {
            appointments = queryUseCase.findByProvider(tenantId, ProviderId.of(providerId), date);
        } else {
            appointments = queryUseCase.findByPatient(tenantId, PatientId.of(patientId));
        }

        return ResponseEntity.ok(appointments.stream().map(this::toAppointmentResponse).toList());
    }

    private AppointmentResponse toAppointmentResponse(Appointment appointment) {
        String cancellationCode = null;
        String cancellationDescription = null;
        if (appointment.getAppointmentStatus() instanceof AppointmentStatus.Cancelled cancelled) {
            cancellationCode = cancelled.reason().code();
            cancellationDescription = cancelled.reason().description();
        }

        List<PrescriptionResponse> prescriptions = appointment.getPrescriptions().stream()
                .map(this::toPrescriptionResponse)
                .toList();

        return new AppointmentResponse(
                appointment.getId().value(),
                appointment.getTenantId().value(),
                appointment.getPatientId().value(),
                appointment.getProviderId().value(),
                appointment.getTimeSlot().date(),
                appointment.getTimeSlot().startTime(),
                appointment.getTimeSlot().endTime(),
                appointment.getAppointmentType().name(),
                statusName(appointment.getAppointmentStatus()),
                cancellationCode,
                cancellationDescription,
                prescriptions);
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription p) {
        return new PrescriptionResponse(
                p.getId().value(),
                p.getMedication().ndcCode(),
                p.getMedication().brandName(),
                p.getMedication().genericName(),
                p.getDosage(),
                p.getInstructions());
    }

    private static String statusName(AppointmentStatus status) {
        if (status instanceof AppointmentStatus.Scheduled) return "SCHEDULED";
        if (status instanceof AppointmentStatus.Confirmed) return "CONFIRMED";
        if (status instanceof AppointmentStatus.Completed) return "COMPLETED";
        if (status instanceof AppointmentStatus.NoShow) return "NO_SHOW";
        if (status instanceof AppointmentStatus.Cancelled) return "CANCELLED";
        return "SCHEDULED";
    }
}