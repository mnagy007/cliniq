package com.cliniq.web.provider.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        UUID tenantId,
        String givenName,
        String familyName,
        String specialty,
        List<AvailabilitySlotDto> availabilitySlots
) {}