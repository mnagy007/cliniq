package com.cliniq.persistence.patient.mapper;

import com.cliniq.domain.notification.Channel;
import com.cliniq.domain.patient.ContactInfo;
import com.cliniq.domain.patient.EmailAddress;
import com.cliniq.domain.patient.Gender;
import com.cliniq.domain.patient.MedicalRecordNumber;
import com.cliniq.domain.patient.NotificationPreference;
import com.cliniq.domain.patient.Patient;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.patient.PersonalInfo;
import com.cliniq.domain.patient.PhoneNumber;
import com.cliniq.persistence.patient.entity.JpaPatient;
import com.cliniq.shared.domain.TenantId;

public final class JpaPatientMapper {

    private JpaPatientMapper() {
    }

    public static JpaPatient toJpa(Patient patient) {
        PatientId id = patient.getId();
        PersonalInfo info = patient.getPersonalInfo();
        ContactInfo contact = patient.getContactInfo();
        NotificationPreference pref = patient.getNotificationPreference();

        return new JpaPatient(
                id.value(),
                patient.getTenantId().value(),
                info.givenName(),
                info.familyName(),
                info.dateOfBirth(),
                info.gender().name(),
                contact.phoneNumber().value(),
                contact.emailAddress().value(),
                patient.getMedicalRecordNumber().value(),
                channelToString(pref.preferredChannel()),
                pref.optedOut());
    }

    public static Patient toDomain(JpaPatient jpa) {
        PatientId id = PatientId.of(jpa.getId());
        TenantId tenantId = TenantId.of(jpa.getTenantId());
        PersonalInfo personalInfo = new PersonalInfo(
                jpa.getGivenName(),
                jpa.getFamilyName(),
                jpa.getDateOfBirth(),
                Gender.valueOf(jpa.getGender()));
        ContactInfo contactInfo = new ContactInfo(
                new PhoneNumber(jpa.getPhoneNumber()),
                new EmailAddress(jpa.getEmailAddress()));
        MedicalRecordNumber mrn = new MedicalRecordNumber(jpa.getMedicalRecordNumber());
        NotificationPreference preference = new NotificationPreference(
                stringToChannel(jpa.getPreferredChannel()),
                jpa.isOptedOut());

        return Patient.reconstruct(id, tenantId, personalInfo, contactInfo, mrn, preference);
    }

    private static String channelToString(Channel channel) {
        if (channel instanceof Channel.Sms) {
            return "SMS";
        } else if (channel instanceof Channel.Email) {
            return "EMAIL";
        }
        return "SMS";
    }

    private static Channel stringToChannel(String value) {
        if ("EMAIL".equals(value)) {
            return Channel.Email.INSTANCE;
        }
        return Channel.Sms.INSTANCE;
    }
}
