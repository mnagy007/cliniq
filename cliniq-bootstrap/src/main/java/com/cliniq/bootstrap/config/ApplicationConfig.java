package com.cliniq.bootstrap.config;

import com.cliniq.application.appointment.AppointmentService;
import com.cliniq.application.appointment.port.out.AppointmentRepository;
import com.cliniq.application.notification.port.out.OutboxRepository;
import com.cliniq.application.notification.OutboxProcessorService;
import com.cliniq.application.notification.ReminderService;
import com.cliniq.application.notification.port.out.NotificationDispatchPort;
import com.cliniq.application.notification.port.out.ReminderRepository;
import com.cliniq.application.patient.PatientService;
import com.cliniq.application.patient.port.out.PatientRepository;
import com.cliniq.application.provider.ProviderService;
import com.cliniq.application.provider.port.out.ProviderRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.bootstrap.config.infra.LoggingDomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new LoggingDomainEventPublisher();
    }

    @Bean
    public PatientService patientService(
            PatientRepository patientRepository,
            DomainEventPublisher domainEventPublisher) {
        return new PatientService(patientRepository, domainEventPublisher);
    }

    @Bean
    public ProviderService providerService(ProviderRepository providerRepository) {
        return new ProviderService(providerRepository);
    }

    @Bean
    public AppointmentService appointmentService(
            AppointmentRepository appointmentRepository,
            ProviderRepository providerRepository,
            OutboxRepository outboxRepository,
            DomainEventPublisher domainEventPublisher) {
        return new AppointmentService(
                appointmentRepository,
                providerRepository,
                outboxRepository,
                domainEventPublisher);
    }

    @Bean
    public ReminderService reminderService(
            ReminderRepository reminderRepository,
            OutboxRepository outboxRepository,
            DomainEventPublisher domainEventPublisher) {
        return new ReminderService(reminderRepository, outboxRepository, domainEventPublisher);
    }

    @Bean
    public OutboxProcessorService outboxProcessorService(
            OutboxRepository outboxRepository,
            DomainEventPublisher domainEventPublisher,
            ReminderRepository reminderRepository,
            ReminderService reminderService,
            NotificationDispatchPort notificationDispatchPort,
            PatientRepository patientRepository) {
        return new OutboxProcessorService(
                outboxRepository,
                domainEventPublisher,
                reminderRepository,
                reminderService,
                notificationDispatchPort,
                patientRepository);
    }
}
