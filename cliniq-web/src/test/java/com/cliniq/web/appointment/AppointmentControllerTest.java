package com.cliniq.web.appointment;

import com.cliniq.application.appointment.command.AddPrescriptionCommand;
import com.cliniq.application.appointment.command.CancelAppointmentCommand;
import com.cliniq.application.appointment.command.CompleteAppointmentCommand;
import com.cliniq.application.appointment.command.ConfirmAppointmentCommand;
import com.cliniq.application.appointment.command.MarkNoShowCommand;
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
import com.cliniq.domain.appointment.PrescriptionId;
import com.cliniq.domain.appointment.TimeSlot;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.appointment.dto.AddPrescriptionRequest;
import com.cliniq.web.appointment.dto.CancelAppointmentRequest;
import com.cliniq.web.appointment.dto.ScheduleAppointmentRequest;
import com.cliniq.web.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private static final UUID TENANT_UUID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID APPT_UUID     = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PATIENT_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID PROVIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID PRESC_UUID    = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final TenantId TENANT    = TenantId.of(TENANT_UUID);

    @Mock private ScheduleAppointmentUseCase scheduleUseCase;
    @Mock private ConfirmAppointmentUseCase  confirmUseCase;
    @Mock private CancelAppointmentUseCase   cancelUseCase;
    @Mock private CompleteAppointmentUseCase completeUseCase;
    @Mock private MarkNoShowUseCase          markNoShowUseCase;
    @Mock private AddPrescriptionUseCase     addPrescriptionUseCase;
    @Mock private QueryAppointmentUseCase    queryUseCase;

    private AppointmentController controller;

    @BeforeEach
    void setUp() {
        controller = new AppointmentController(
                scheduleUseCase, confirmUseCase, cancelUseCase,
                completeUseCase, markNoShowUseCase, addPrescriptionUseCase, queryUseCase);
    }

    @Test
    @DisplayName("schedule returns 201 Created with appointmentId")
    void schedule_validRequest_returnsCreated() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(scheduleUseCase.schedule(any())).thenReturn(AppointmentId.of(APPT_UUID));

            var request = new ScheduleAppointmentRequest(
                    PATIENT_UUID, PROVIDER_UUID,
                    LocalDate.of(2026, 5, 1),
                    LocalTime.of(9, 0), LocalTime.of(9, 30), "GENERAL");

            var response = controller.schedule(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).containsEntry("appointmentId", APPT_UUID);
            verify(scheduleUseCase).schedule(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.patientId().equals(PatientId.of(PATIENT_UUID)) &&
                    cmd.providerId().equals(ProviderId.of(PROVIDER_UUID)) &&
                    cmd.type() == AppointmentType.GENERAL));
        }
    }

    @Test
    @DisplayName("confirm delegates to use case and returns 204")
    void confirm_validId_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var response = controller.confirm(APPT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(confirmUseCase).confirm(
                    new ConfirmAppointmentCommand(TENANT, AppointmentId.of(APPT_UUID)));
        }
    }

    @Test
    @DisplayName("cancel passes cancellation reason and returns 204")
    void cancel_validRequest_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var request = new CancelAppointmentRequest("PATIENT_REQUEST", "Patient requested cancellation");
            var response = controller.cancel(APPT_UUID, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(cancelUseCase).cancel(new CancelAppointmentCommand(
                    TENANT, AppointmentId.of(APPT_UUID),
                    new CancellationReason("PATIENT_REQUEST", "Patient requested cancellation")));
        }
    }

    @Test
    @DisplayName("complete delegates to use case and returns 204")
    void complete_validId_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var response = controller.complete(APPT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(completeUseCase).complete(
                    new CompleteAppointmentCommand(TENANT, AppointmentId.of(APPT_UUID)));
        }
    }

    @Test
    @DisplayName("markNoShow delegates to use case and returns 204")
    void markNoShow_validId_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var response = controller.markNoShow(APPT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(markNoShowUseCase).markNoShow(
                    new MarkNoShowCommand(TENANT, AppointmentId.of(APPT_UUID)));
        }
    }

    @Test
    @DisplayName("addPrescription returns 201 with prescriptionId")
    void addPrescription_validRequest_returnsCreated() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(addPrescriptionUseCase.addPrescription(any()))
                    .thenReturn(PrescriptionId.of(PRESC_UUID));

            var request = new AddPrescriptionRequest("12345-678-90", "BrandX", "GenericY",
                    "10mg", "Take once daily");

            var response = controller.addPrescription(APPT_UUID, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).containsEntry("prescriptionId", PRESC_UUID);
            verify(addPrescriptionUseCase).addPrescription(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.appointmentId().equals(AppointmentId.of(APPT_UUID)) &&
                    cmd.medication().ndcCode().equals("12345-678-90")));
        }
    }

    @Test
    @DisplayName("getAppointment maps SCHEDULED status and returns 200")
    void getAppointment_scheduledAppointment_mapsStatusCorrectly() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            Appointment appt = mockAppointment(new AppointmentStatus.Scheduled());
            when(queryUseCase.findById(TENANT, AppointmentId.of(APPT_UUID))).thenReturn(appt);

            var response = controller.getAppointment(APPT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().status()).isEqualTo("SCHEDULED");
            assertThat(response.getBody().cancellationCode()).isNull();
        }
    }

    @Test
    @DisplayName("getAppointment maps CANCELLED status with reason fields populated")
    void getAppointment_cancelledAppointment_mapsCancellationReason() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var reason = new CancellationReason("PATIENT_REQUEST", "Patient requested cancellation");
            Appointment appt = mockAppointment(new AppointmentStatus.Cancelled(reason));
            when(queryUseCase.findById(TENANT, AppointmentId.of(APPT_UUID))).thenReturn(appt);

            var response = controller.getAppointment(APPT_UUID);

            assertThat(response.getBody().status()).isEqualTo("CANCELLED");
            assertThat(response.getBody().cancellationCode()).isEqualTo("PATIENT_REQUEST");
            assertThat(response.getBody().cancellationDescription()).isEqualTo("Patient requested cancellation");
        }
    }

    @Test
    @DisplayName("queryAppointments with patientId routes to findByPatient")
    void queryAppointments_patientIdOnly_routesToFindByPatient() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(queryUseCase.findByPatient(any(), any())).thenReturn(List.of());

            var response = controller.queryAppointments(PATIENT_UUID, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(queryUseCase).findByPatient(TENANT, PatientId.of(PATIENT_UUID));
            verify(queryUseCase, never()).findByProvider(any(), any(), any());
        }
    }

    // TODO(human): implement queryAppointments_providerIdAndDate_routesToFindByProvider
    // When both providerId and date are present, the controller should call findByProvider
    // and never call findByPatient. Test both the routing and the 200 OK response.

    private Appointment mockAppointment(AppointmentStatus status) {
        return Appointment.reconstruct(
                AppointmentId.of(APPT_UUID), TENANT,
                PatientId.of(PATIENT_UUID), ProviderId.of(PROVIDER_UUID),
                new TimeSlot(LocalDate.of(2026, 5, 1), LocalTime.of(9, 0), LocalTime.of(9, 30)),
                AppointmentType.GENERAL, status, List.of(), 0L);
    }

}
