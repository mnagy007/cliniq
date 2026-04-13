package com.cliniq.domain.patient;

import com.cliniq.domain.patient.event.ContactInfoUpdated;
import com.cliniq.domain.patient.event.NotificationPreferenceChanged;
import com.cliniq.domain.patient.event.PatientRegistered;
import com.cliniq.shared.domain.AggregateRoot;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.shared.validation.Preconditions;

public class Patient extends AggregateRoot<PatientId> {

    private final PatientId id;
    private final TenantId tenantId;
    private PersonalInfo personalInfo;
    private ContactInfo contactInfo;
    private final MedicalRecordNumber medicalRecordNumber;
    private NotificationPreference notificationPreference;

    private Patient(PatientId id, TenantId tenantId, PersonalInfo personalInfo,
                    ContactInfo contactInfo, MedicalRecordNumber medicalRecordNumber,
                    NotificationPreference notificationPreference) {
        this.id = id;
        this.tenantId = tenantId;
        this.personalInfo = personalInfo;
        this.contactInfo = contactInfo;
        this.medicalRecordNumber = medicalRecordNumber;
        this.notificationPreference = notificationPreference;
    }

    public static Patient register(TenantId tenantId, PersonalInfo personalInfo,
                                    ContactInfo contactInfo, MedicalRecordNumber medicalRecordNumber,
                                    NotificationPreference preference) {
        Preconditions.requireNonNull(tenantId, "tenantId");
        Preconditions.requireNonNull(personalInfo, "personalInfo");
        Preconditions.requireNonNull(contactInfo, "contactInfo");
        Preconditions.requireNonNull(medicalRecordNumber, "medicalRecordNumber");
        Preconditions.requireNonNull(preference, "preference");

        Patient patient = new Patient(
                PatientId.generate(), tenantId, personalInfo, contactInfo, medicalRecordNumber, preference);
        patient.registerEvent(PatientRegistered.of(tenantId, patient.id, personalInfo, contactInfo));
        return patient;
    }

    public void updateContactInfo(ContactInfo newContactInfo) {
        Preconditions.requireNonNull(newContactInfo, "newContactInfo");
        this.contactInfo = newContactInfo;
        registerEvent(ContactInfoUpdated.of(tenantId, id, newContactInfo));
    }

    public void updateNotificationPreference(NotificationPreference preference) {
        Preconditions.requireNonNull(preference, "preference");
        this.notificationPreference = preference;
        registerEvent(NotificationPreferenceChanged.of(tenantId, id, preference));
    }

    @Override
    public PatientId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public MedicalRecordNumber getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public NotificationPreference getNotificationPreference() {
        return notificationPreference;
    }
}