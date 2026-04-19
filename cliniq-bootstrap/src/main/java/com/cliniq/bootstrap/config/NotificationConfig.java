package com.cliniq.bootstrap.config;

import com.cliniq.notification.NotificationResilienceConfig;
import com.cliniq.notification.TwilioNotificationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(NotificationResilienceConfig.class)
public class NotificationConfig {

    /**
     * TwilioNotificationAdapter is already declared as @Component in the
     * notification-adapter module with @Value injection. Spring's component
     * scan will pick it up automatically since @SpringBootApplication
     * scans the com.cliniq base package.
     * 
     * This configuration class imports NotificationResilienceConfig to
     * ensure Resilience4j retry and circuit breaker are configured.
     */
}
