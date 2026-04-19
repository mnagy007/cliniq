package com.cliniq.bootstrap.config;

import com.cliniq.application.shared.port.out.EncryptionPort;
import com.cliniq.persistence.appointment.adapter.JpaAppointmentAdapter;
import com.cliniq.persistence.appointment.adapter.JpaCalendarCredentialAdapter;
import com.cliniq.persistence.appointment.repository.JpaAppointmentRepository;
import com.cliniq.persistence.appointment.repository.JpaCalendarCredentialRepository;
import com.cliniq.persistence.appointment.repository.JpaPrescriptionRepository;
import com.cliniq.persistence.notification.adapter.JpaOutboxAdapter;
import com.cliniq.persistence.notification.adapter.JpaReminderAdapter;
import com.cliniq.persistence.notification.repository.JpaOutboxEntryRepository;
import com.cliniq.persistence.notification.repository.JpaReminderRepository;
import com.cliniq.persistence.patient.adapter.JpaPatientAdapter;
import com.cliniq.persistence.patient.repository.JpaPatientRepository;
import com.cliniq.persistence.provider.adapter.JpaProviderAdapter;
import com.cliniq.persistence.provider.repository.JpaProviderRepository;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.UUID;

@Configuration
@EnableJpaRepositories(basePackages = "com.cliniq.persistence")
@EntityScan(basePackages = "com.cliniq.persistence")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
public class PersistenceConfig {

    @Bean
    public JpaPatientAdapter jpaPatientAdapter(JpaPatientRepository jpaPatientRepository) {
        return new JpaPatientAdapter(jpaPatientRepository);
    }

    @Bean
    public JpaProviderAdapter jpaProviderAdapter(JpaProviderRepository jpaProviderRepository) {
        return new JpaProviderAdapter(jpaProviderRepository);
    }

    @Bean
    public JpaAppointmentAdapter jpaAppointmentAdapter(
            JpaAppointmentRepository jpaAppointmentRepository,
            JpaPrescriptionRepository jpaPrescriptionRepository) {
        return new JpaAppointmentAdapter(jpaAppointmentRepository, jpaPrescriptionRepository);
    }

    @Bean
    public JpaReminderAdapter jpaReminderAdapter(JpaReminderRepository jpaReminderRepository) {
        return new JpaReminderAdapter(jpaReminderRepository);
    }

    @Bean
    public JpaOutboxAdapter jpaOutboxAdapter(JpaOutboxEntryRepository jpaOutboxEntryRepository) {
        return new JpaOutboxAdapter(jpaOutboxEntryRepository);
    }

    @Bean
    public JpaCalendarCredentialAdapter jpaCalendarCredentialAdapter(
            JpaCalendarCredentialRepository repository,
            EncryptionPort encryptionPort) {
        return new JpaCalendarCredentialAdapter(repository, encryptionPort);
    }
}
