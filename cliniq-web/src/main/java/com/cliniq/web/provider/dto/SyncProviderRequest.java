package com.cliniq.web.provider.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SyncProviderRequest(
        @NotBlank String givenName,
        @NotBlank String familyName,
        @NotBlank String specialty,
        List<AvailabilitySlotDto> availabilitySlots
) {}