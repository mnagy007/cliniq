package com.cliniq.web.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record AppointmentResponse(
        UUID appointmentId,
        UUID tenantId,
        UUID patientId,
        UUID providerId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String type,
        String status,
        String cancellationCode,
        String cancellationDescription,
        List<PrescriptionResponse> prescriptions
) {}