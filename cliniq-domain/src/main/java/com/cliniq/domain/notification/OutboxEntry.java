package com.cliniq.domain.notification;

import com.cliniq.domain.notification.exception.NotificationDomainException;
import com.cliniq.shared.domain.AggregateRoot;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.shared.validation.Preconditions;

import java.time.Instant;

public class OutboxEntry extends AggregateRoot<OutboxEntryId> {

    private final OutboxEntryId id;
    private final TenantId tenantId;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final String payload;
    private final Instant occurredAt;
    private Instant processedAt;

    private OutboxEntry(OutboxEntryId id, TenantId tenantId, String aggregateType,
                        String aggregateId, String eventType, String payload,
                        Instant occurredAt, Instant processedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
    }

    public static OutboxEntry create(TenantId tenantId, String aggregateType,
                                      String aggregateId, String eventType, String payload) {
        Preconditions.requireNonNull(tenantId, "tenantId");
        Preconditions.requireNotBlank(aggregateType, "aggregateType");
        Preconditions.requireNotBlank(aggregateId, "aggregateId");
        Preconditions.requireNotBlank(eventType, "eventType");
        Preconditions.requireNotBlank(payload, "payload");

        return new OutboxEntry(OutboxEntryId.generate(), tenantId, aggregateType,
                aggregateId, eventType, payload, Instant.now(), null);
    }

    public void markProcessed() {
        if (processedAt != null) {
            throw new NotificationDomainException("OUTBOX_ALREADY_PROCESSED",
                    "OutboxEntry already processed");
        }
        this.processedAt = Instant.now();
    }

    public boolean isProcessed() {
        return processedAt != null;
    }

    @Override
    public OutboxEntryId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}