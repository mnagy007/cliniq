package com.cliniq.web.notification;

import com.cliniq.application.notification.port.in.QueryReminderUseCase;
import com.cliniq.application.notification.port.in.ScheduleReminderUseCase;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.domain.notification.FailureReason;
import com.cliniq.domain.notification.ReminderStatus;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.notification.dto.ScheduleReminderRequest;
import com.cliniq.web.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    private static final UUID TENANT_UUID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID APPT_UUID     = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PATIENT_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID REMINDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final TenantId TENANT    = TenantId.of(TENANT_UUID);

    @Mock private ScheduleReminderUseCase scheduleReminderUseCase;
    @Mock private QueryReminderUseCase    queryReminderUseCase;

    private ReminderController controller;

    @BeforeEach
    void setUp() {
        controller = new ReminderController(scheduleReminderUseCase, queryReminderUseCase);
    }

    @Test
    @DisplayName("schedule with EMAIL channel maps to Channel.Email and returns 201")
    void schedule_emailChannel_returnsCreated() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(scheduleReminderUseCase.schedule(any())).thenReturn(ReminderId.of(REMINDER_UUID));

            var request = new ScheduleReminderRequest(APPT_UUID, PATIENT_UUID, "EMAIL");
            var response = controller.schedule(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).containsEntry("reminderId", REMINDER_UUID);
            verify(scheduleReminderUseCase).schedule(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.appointmentId().equals(AppointmentId.of(APPT_UUID)) &&
                    cmd.patientId().equals(PatientId.of(PATIENT_UUID)) &&
                    cmd.channel() instanceof Channel.Email));
        }
    }

    @Test
    @DisplayName("schedule with SMS channel maps to Channel.Sms and returns 201")
    void schedule_smsChannel_returnsCreated() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(scheduleReminderUseCase.schedule(any())).thenReturn(ReminderId.of(REMINDER_UUID));

            var request = new ScheduleReminderRequest(APPT_UUID, PATIENT_UUID, "SMS");
            var response = controller.schedule(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(scheduleReminderUseCase).schedule(argThat(cmd ->
                    cmd.channel() instanceof Channel.Sms));
        }
    }

    @Test
    @DisplayName("findByAppointment returns 200 with mapped reminder list")
    void findByAppointment_returnsOk() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            Reminder reminder = Reminder.reconstruct(
                    ReminderId.of(REMINDER_UUID), TENANT,
                    AppointmentId.of(APPT_UUID), PatientId.of(PATIENT_UUID),
                    Channel.Email.INSTANCE, new ReminderStatus.Pending());
            when(queryReminderUseCase.findByAppointment(TENANT, AppointmentId.of(APPT_UUID)))
                    .thenReturn(List.of(reminder));

            var response = controller.findByAppointment(APPT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).reminderId()).isEqualTo(REMINDER_UUID);
            assertThat(response.getBody().get(0).channel()).isEqualTo("EMAIL");
            assertThat(response.getBody().get(0).status()).isEqualTo("PENDING");
        }
    }

    @Test
    @DisplayName("findByAppointment maps FAILED status with failure reason fields")
    void findByAppointment_failedReminder_mapsFailureReason() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var failureReason = new FailureReason("TIMEOUT", "Delivery timed out");
            Reminder reminder = Reminder.reconstruct(
                    ReminderId.of(REMINDER_UUID), TENANT,
                    AppointmentId.of(APPT_UUID), PatientId.of(PATIENT_UUID),
                    Channel.Sms.INSTANCE, new ReminderStatus.Failed(failureReason));
            when(queryReminderUseCase.findByAppointment(any(), any())).thenReturn(List.of(reminder));

            var response = controller.findByAppointment(APPT_UUID);

            assertThat(response.getBody().get(0).status()).isEqualTo("FAILED");
            assertThat(response.getBody().get(0).failureCode()).isEqualTo("TIMEOUT");
            assertThat(response.getBody().get(0).failureMessage()).isEqualTo("Delivery timed out");
        }
    }
}
