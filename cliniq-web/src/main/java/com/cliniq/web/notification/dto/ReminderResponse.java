package com.cliniq.web.notification.dto;

import java.util.UUID;

public record ReminderResponse(
        UUID reminderId,
        UUID appointmentId,
        UUID patientId,
        String channel,
        String status,
        String failureCode,
        String failureMessage
) {}