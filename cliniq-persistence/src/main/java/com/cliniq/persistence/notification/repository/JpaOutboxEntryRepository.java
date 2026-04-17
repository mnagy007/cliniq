package com.cliniq.persistence.notification.repository;

import com.cliniq.persistence.notification.entity.JpaOutboxEntry;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaOutboxEntryRepository extends org.springframework.data.jpa.repository.JpaRepository<JpaOutboxEntry, UUID> {

    @Query(value = "SELECT * FROM outbox_entries WHERE processed_at IS NULL ORDER BY occurred_at ASC " +
            "LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<JpaOutboxEntry> findUnprocessed(int limit);
}
