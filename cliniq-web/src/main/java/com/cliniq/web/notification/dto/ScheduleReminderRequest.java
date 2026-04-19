package com.cliniq.web.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScheduleReminderRequest(
        @NotNull UUID appointmentId,
        @NotNull UUID patientId,
        @NotNull String channel
) {}