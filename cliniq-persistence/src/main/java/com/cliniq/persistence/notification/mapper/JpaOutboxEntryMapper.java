package com.cliniq.persistence.notification.mapper;

import com.cliniq.domain.notification.OutboxEntry;
import com.cliniq.domain.notification.OutboxEntryId;
import com.cliniq.persistence.notification.entity.JpaOutboxEntry;
import com.cliniq.shared.domain.TenantId;

public final class JpaOutboxEntryMapper {

    private JpaOutboxEntryMapper() {
    }

    public static JpaOutboxEntry toJpa(OutboxEntry entry) {
        return new JpaOutboxEntry(
                entry.getId().value(),
                entry.getTenantId().value(),
                entry.getAggregateType(),
                entry.getAggregateId(),
                entry.getEventType(),
                entry.getPayload(),
                entry.getOccurredAt(),
                entry.getProcessedAt());
    }

    public static OutboxEntry toDomain(JpaOutboxEntry jpa) {
        return OutboxEntry.reconstruct(
                OutboxEntryId.of(jpa.getId()),
                TenantId.of(jpa.getTenantId()),
                jpa.getAggregateType(),
                jpa.getAggregateId(),
                jpa.getEventType(),
                jpa.getPayload(),
                jpa.getOccurredAt(),
                jpa.getProcessedAt());
    }
}
