package com.cliniq.application.provider.command;

import com.cliniq.domain.provider.AvailabilitySlot;
import com.cliniq.domain.provider.ProviderName;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.Specialty;
import com.cliniq.shared.domain.TenantId;

import java.util.List;

public record SyncProviderCommand(
        TenantId tenantId,
        ProviderId providerId,
        ProviderName name,
        Specialty specialty,
        List<AvailabilitySlot> availabilitySlots
) {}