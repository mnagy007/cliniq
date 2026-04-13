package com.cliniq.application.notification.port.out;

import com.cliniq.domain.notification.OutboxEntry;

import java.util.List;

public interface OutboxRepository {
    void save(OutboxEntry entry);
    List<OutboxEntry> findUnprocessed(int limit);
}