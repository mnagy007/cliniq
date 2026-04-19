package com.cliniq.web.provider.dto;

import java.time.LocalTime;

public record AvailabilitySlotDto(
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}