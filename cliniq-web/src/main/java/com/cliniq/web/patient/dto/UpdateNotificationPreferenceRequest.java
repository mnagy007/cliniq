package com.cliniq.web.patient.dto;

public record UpdateNotificationPreferenceRequest(
    String preferredChannel,
    Boolean optedOut
) {}
