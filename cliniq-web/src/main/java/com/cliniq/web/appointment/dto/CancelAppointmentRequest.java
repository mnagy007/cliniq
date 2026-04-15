package com.cliniq.web.appointment.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequest(
        @NotBlank String code,
        @NotBlank String description
) {}