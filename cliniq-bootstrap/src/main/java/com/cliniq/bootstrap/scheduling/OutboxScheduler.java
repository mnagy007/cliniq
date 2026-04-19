package com.cliniq.bootstrap.scheduling;

import com.cliniq.application.notification.OutboxProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxProcessorService outboxProcessorService;
    private final int batchSize;

    public OutboxScheduler(OutboxProcessorService outboxProcessorService,
                           @Value("${cliniq.outbox.batch-size:50}") int batchSize) {
        this.outboxProcessorService = outboxProcessorService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${cliniq.outbox.poll-delay-ms:5000}")
    public void processOutbox() {
        outboxProcessorService.processNext(batchSize);
    }
}