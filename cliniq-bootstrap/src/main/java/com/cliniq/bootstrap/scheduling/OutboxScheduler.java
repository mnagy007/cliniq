package com.cliniq.bootstrap.scheduling;

import com.cliniq.application.notification.OutboxProcessorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxProcessorService outboxProcessorService;

    public OutboxScheduler(OutboxProcessorService outboxProcessorService) {
        this.outboxProcessorService = outboxProcessorService;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        outboxProcessorService.processNext(50);
    }
}