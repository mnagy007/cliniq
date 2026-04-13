package com.cliniq.application.patient;

import com.cliniq.application.patient.command.RegisterPatientCommand;
import com.cliniq.application.patient.command.UpdateContactInfoCommand;
import com.cliniq.application.patient.command.UpdateNotificationPreferenceCommand;
import com.cliniq.application.patient.port.in.QueryPatientUseCase;
import com.cliniq.application.patient.port.in.RegisterPatientUseCase;
import com.cliniq.application.patient.port.in.UpdateContactInfoUseCase;
import com.cliniq.application.patient.port.in.UpdateNotificationPreferenceUseCase;
import com.cliniq.application.patient.port.out.PatientRepository;
import com.cliniq.application.shared.port.out.DomainEventPublisher;
import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.patient.exception.PatientNotFoundException;
import com.cliniq.shared.domain.TenantId;

public class PatientService implements
        RegisterPatientUseCase,
        UpdateContactInfoUseCase,
        UpdateNotificationPreferenceUseCase,
        QueryPatientUseCase {

    private final PatientRepository patientRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PatientService(PatientRepository patientRepository, DomainEventPublisher domainEventPublisher) {
        this.patientRepository = patientRepository;
        this.domainEventPublisher = domainEventPublisher;
    }


    @Override
    public Patient findById(TenantId tenantId, PatientId id) {
        return patientRepository.findById(tenantId, id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Override
    public PatientId register(RegisterPatientCommand command) {
        Patient patient = Patient.register(command.tenantId(), command.personalInfo(), command.contactInfo(),
                command.medicalRecordNumber(), command.notificationPreference());
        patientRepository.save(patient);
        domainEventPublisher.publish(patient.pullDomainEvents());
        return patient.getId();
    }

    @Override
    public void updatePreference(UpdateNotificationPreferenceCommand command) {
        patientRepository.findById(command.tenantId(), command.patientId())
                .ifPresentOrElse(patient -> {
                    patient.updateNotificationPreference(command.newPreference());
                    patientRepository.save(patient);
                    domainEventPublisher.publish(patient.pullDomainEvents());
                }, () -> {
                    throw new PatientNotFoundException(command.patientId());
                });
    }

    @Override
    public void updateContactInfo(UpdateContactInfoCommand command) {
        patientRepository.findById(command.tenantId(), command.patientId())
                .ifPresentOrElse(patient -> {
                    patient.updateContactInfo(command.newContactInfo());
                    patientRepository.save(patient);
                    domainEventPublisher.publish(patient.pullDomainEvents());
                }, () -> {
                    throw new PatientNotFoundException(command.patientId());
                });
    }
}
