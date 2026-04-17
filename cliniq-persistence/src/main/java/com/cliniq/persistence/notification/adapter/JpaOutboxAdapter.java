package com.cliniq.persistence.notification.adapter;

import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.domain.notification.OutboxEntry;
import com.cliniq.persistence.notification.entity.JpaOutboxEntry;
import com.cliniq.persistence.notification.mapper.JpaOutboxEntryMapper;
import com.cliniq.persistence.notification.repository.JpaOutboxEntryRepository;

import java.util.List;

public class JpaOutboxAdapter implements OutboxRepository {

    private final JpaOutboxEntryRepository jpaOutboxEntryRepository;

    public JpaOutboxAdapter(JpaOutboxEntryRepository jpaOutboxEntryRepository) {
        this.jpaOutboxEntryRepository = jpaOutboxEntryRepository;
    }

    @Override
    public void save(OutboxEntry entry) {
        JpaOutboxEntry jpa = JpaOutboxEntryMapper.toJpa(entry);
        jpaOutboxEntryRepository.save(jpa);
    }

    @Override
    public List<OutboxEntry> findUnprocessed(int limit) {
        return jpaOutboxEntryRepository.findUnprocessed(limit)
                .stream()
                .map(JpaOutboxEntryMapper::toDomain)
                .toList();
    }
}
