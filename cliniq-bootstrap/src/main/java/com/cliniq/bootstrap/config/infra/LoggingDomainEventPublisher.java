package com.cliniq.bootstrap.config.infra;

import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.shared.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Simple logging implementation of DomainEventPublisher.
 * In a production environment, this would be replaced with an actual
 * message broker or event store implementation.
 */
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        for (DomainEvent event : events) {
            log.info("Domain event published: {} (eventId={})",
                    event.getClass().getSimpleName(),
                    event.eventId());
        }
    }
}
