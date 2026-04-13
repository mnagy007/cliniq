package com.cliniq.application.shared.port.out;

import com.cliniq.shared.domain.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<DomainEvent> events);
}