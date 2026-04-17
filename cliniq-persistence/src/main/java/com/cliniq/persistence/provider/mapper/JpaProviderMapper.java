package com.cliniq.persistence.provider.mapper;

import com.cliniq.domain.provider.AvailabilitySlot;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.ProviderName;
import com.cliniq.domain.provider.Specialty;
import com.cliniq.persistence.provider.entity.JpaAvailabilitySlot;
import com.cliniq.persistence.provider.entity.JpaProvider;
import com.cliniq.shared.domain.TenantId;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JpaProviderMapper {

    private JpaProviderMapper() {
    }

    public static JpaProvider toJpa(Provider provider) {
        List<JpaAvailabilitySlot> jpaSlots = new ArrayList<>();
        for (AvailabilitySlot slot : provider.getAvailabilitySlots()) {
            jpaSlots.add(new JpaAvailabilitySlot(
                    UUID.randomUUID(),
                    slot.dayOfWeek().name(),
                    slot.startTime(),
                    slot.endTime()));
        }

        return new JpaProvider(
                provider.getId().value(),
                provider.getTenantId().value(),
                provider.getName().givenName(),
                provider.getName().familyName(),
                provider.getSpecialty().name(),
                jpaSlots);
    }

    public static Provider toDomain(JpaProvider jpa) {
        ProviderId id = ProviderId.of(jpa.getId());
        TenantId tenantId = TenantId.of(jpa.getTenantId());
        ProviderName name = new ProviderName(jpa.getGivenName(), jpa.getFamilyName());
        Specialty specialty = Specialty.valueOf(jpa.getSpecialty());

        List<AvailabilitySlot> slots = new ArrayList<>();
        if (jpa.getSlots() != null) {
            for (JpaAvailabilitySlot jpaSlot : jpa.getSlots()) {
                slots.add(new AvailabilitySlot(
                        DayOfWeek.valueOf(jpaSlot.getDayOfWeek()),
                        jpaSlot.getStartTime(),
                        jpaSlot.getEndTime()));
            }
        }

        return Provider.reconstruct(id, tenantId, name, specialty, slots);
    }
}
