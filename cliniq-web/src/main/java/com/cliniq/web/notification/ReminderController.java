package com.cliniq.web.notification;

import com.cliniq.application.notification.command.ScheduleReminderCommand;
import com.cliniq.application.notification.port.in.QueryReminderUseCase;
import com.cliniq.application.notification.port.in.ScheduleReminderUseCase;
import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.notification.ReminderId;
import com.cliniq.domain.notification.ReminderStatus;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.notification.dto.ReminderResponse;
import com.cliniq.web.notification.dto.ScheduleReminderRequest;
import com.cliniq.web.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ScheduleReminderUseCase scheduleReminderUseCase;
    private final QueryReminderUseCase queryReminderUseCase;

    public ReminderController(ScheduleReminderUseCase scheduleReminderUseCase,
                              QueryReminderUseCase queryReminderUseCase) {
        this.scheduleReminderUseCase = scheduleReminderUseCase;
        this.queryReminderUseCase = queryReminderUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> schedule(@Valid @RequestBody ScheduleReminderRequest request) {
        TenantId tenantId = TenantContext.require();
        Channel channel = "EMAIL".equals(request.channel()) ? Channel.Email.INSTANCE : Channel.Sms.INSTANCE;

        ScheduleReminderCommand command = new ScheduleReminderCommand(
                tenantId,
                AppointmentId.of(request.appointmentId()),
                PatientId.of(request.patientId()),
                channel);

        ReminderId id = scheduleReminderUseCase.schedule(command);
        return ResponseEntity.created(null)
                .body(Map.of("reminderId", id.value()));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> findByAppointment(@RequestParam UUID appointmentId) {
        TenantId tenantId = TenantContext.require();
        List<Reminder> reminders = queryReminderUseCase.findByAppointment(tenantId, AppointmentId.of(appointmentId));

        List<ReminderResponse> response = reminders.stream()
                .map(this::toReminderResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private ReminderResponse toReminderResponse(Reminder reminder) {
        String failureCode = null;
        String failureMessage = null;
        if (reminder.getStatus() instanceof ReminderStatus.Failed failed) {
            failureCode = failed.reason().code();
            failureMessage = failed.reason().message();
        }

        return new ReminderResponse(
                reminder.getId().value(),
                reminder.getAppointmentId().value(),
                reminder.getPatientId().value(),
                channelToString(reminder.getChannel()),
                statusToString(reminder.getStatus()),
                failureCode,
                failureMessage);
    }

    private static String channelToString(Channel channel) {
        if (channel instanceof Channel.Email) return "EMAIL";
        return "SMS";
    }

    private static String statusToString(ReminderStatus status) {
        if (status instanceof ReminderStatus.Dispatched) return "DISPATCHED";
        if (status instanceof ReminderStatus.Delivered) return "DELIVERED";
        if (status instanceof ReminderStatus.Failed) return "FAILED";
        return "PENDING";
    }
}