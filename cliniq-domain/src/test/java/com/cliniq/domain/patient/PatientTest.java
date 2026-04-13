package com.cliniq.domain.patient;

import com.cliniq.domain.patient.event.ContactInfoUpdated;
import com.cliniq.domain.patient.event.NotificationPreferenceChanged;
import com.cliniq.domain.patient.event.PatientRegistered;
import com.cliniq.domain.patient.exception.PatientDomainException;
import com.cliniq.domain.notification.Channel;
import com.cliniq.shared.domain.DomainEvent;
import com.cliniq.shared.domain.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientTest {

    private static final TenantId TENANT_ID = TenantId.of(UUID.randomUUID());
    private static final PatientId PATIENT_ID = PatientId.generate();

    private PersonalInfo defaultPersonalInfo() {
        return new PersonalInfo("John", "Doe", LocalDate.of(1990, 1, 1), Gender.MALE);
    }

    private ContactInfo defaultContactInfo() {
        return new ContactInfo(new PhoneNumber("+1234567890"), new EmailAddress("john@example.com"));
    }

    private MedicalRecordNumber defaultMrn() {
        return new MedicalRecordNumber("MRN-001");
    }

    private NotificationPreference defaultPreference() {
        return NotificationPreference.defaultPreference();
    }

    @Nested
    @DisplayName("Patient.register()")
    class RegisterTests {

        @Test
        @DisplayName("should create patient with correct fields and register PatientRegistered event")
        void shouldCreatePatientAndRegisterEvent() {
            var personalInfo = defaultPersonalInfo();
            var contactInfo = defaultContactInfo();
            var mrn = defaultMrn();
            var preference = defaultPreference();

            Patient patient = Patient.register(TENANT_ID, personalInfo, contactInfo, mrn, preference);

            var events = patient.pullDomainEvents();
            assertThat(events).hasSize(1);
            var event = (PatientRegistered) events.getFirst();
            assertThat(event.patientId()).isEqualTo(patient.getId());
            assertThat(event.tenantId()).isEqualTo(TENANT_ID);
            assertThat(event.personalInfo()).isEqualTo(personalInfo);
            assertThat(event.contactInfo()).isEqualTo(contactInfo);

            assertThat(patient.getPersonalInfo()).isEqualTo(personalInfo);
            assertThat(patient.getContactInfo()).isEqualTo(contactInfo);
            assertThat(patient.getMedicalRecordNumber()).isEqualTo(mrn);
            assertThat(patient.getNotificationPreference()).isEqualTo(preference);
            assertThat(patient.getTenantId()).isEqualTo(TENANT_ID);
        }

        @Test
        @DisplayName("should throw when tenantId is null")
        void shouldThrowWhenTenantIdIsNull() {
            assertThatThrownBy(() -> Patient.register(null, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId");
        }

        @Test
        @DisplayName("should throw when personalInfo is null")
        void shouldThrowWhenPersonalInfoIsNull() {
            assertThatThrownBy(() -> Patient.register(TENANT_ID, null, defaultContactInfo(),
                    defaultMrn(), defaultPreference()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("personalInfo");
        }

        @Test
        @DisplayName("should throw when contactInfo is null")
        void shouldThrowWhenContactInfoIsNull() {
            assertThatThrownBy(() -> Patient.register(TENANT_ID, defaultPersonalInfo(), null,
                    defaultMrn(), defaultPreference()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("contactInfo");
        }

        @Test
        @DisplayName("should throw when medicalRecordNumber is null")
        void shouldThrowWhenMrnIsNull() {
            assertThatThrownBy(() -> Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    null, defaultPreference()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("medicalRecordNumber");
        }

        @Test
        @DisplayName("should throw when preference is null")
        void shouldThrowWhenPreferenceIsNull() {
            assertThatThrownBy(() -> Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("preference");
        }
    }

    @Nested
    @DisplayName("Patient.updateContactInfo()")
    class UpdateContactInfoTests {

        @Test
        @DisplayName("should replace contact and register ContactInfoUpdated event")
        void shouldReplaceContactAndRegisterEvent() {
            Patient patient = Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference());
            patient.pullDomainEvents();

            var newContact = new ContactInfo(new PhoneNumber("+1987654321"), new EmailAddress("new@example.com"));
            patient.updateContactInfo(newContact);

            assertThat(patient.getContactInfo()).isEqualTo(newContact);

            var events = patient.pullDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ContactInfoUpdated.class);
            var event = (ContactInfoUpdated) events.getFirst();
            assertThat(event.newContactInfo()).isEqualTo(newContact);
        }

        @Test
        @DisplayName("should throw when new contact info is null")
        void shouldThrowWhenNewContactInfoIsNull() {
            Patient patient = Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference());

            assertThatThrownBy(() -> patient.updateContactInfo(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("newContactInfo");
        }
    }

    @Nested
    @DisplayName("Patient.updateNotificationPreference()")
    class UpdatePreferenceTests {

        @Test
        @DisplayName("should replace preference and register NotificationPreferenceChanged event")
        void shouldReplacePreferenceAndRegisterEvent() {
            Patient patient = Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference());
            patient.pullDomainEvents();

            var newPref = new NotificationPreference(Channel.Email.INSTANCE, true);
            patient.updateNotificationPreference(newPref);

            assertThat(patient.getNotificationPreference()).isEqualTo(newPref);

            var events = patient.pullDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(NotificationPreferenceChanged.class);
        }

        @Test
        @DisplayName("should throw when preference is null")
        void shouldThrowWhenPreferenceIsNull() {
            Patient patient = Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference());

            assertThatThrownBy(() -> patient.updateNotificationPreference(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("preference");
        }
    }

    @Nested
    @DisplayName("PhoneNumber validation")
    class PhoneNumberTests {

        @Test
        @DisplayName("should reject non-E.164 strings")
        void shouldRejectNonE164() {
            assertThatThrownBy(() -> new PhoneNumber("12345"))
                    .isInstanceOf(PatientDomainException.class)
                    .hasMessageContaining("E.164");
        }

        @Test
        @DisplayName("should accept valid E.164 number")
        void shouldAcceptValidE164() {
            var phone = new PhoneNumber("+1234567890");
            assertThat(phone.value()).isEqualTo("+1234567890");
        }

        @Test
        @DisplayName("should reject blank phone number")
        void shouldRejectBlank() {
            assertThatThrownBy(() -> new PhoneNumber(" "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("EmailAddress validation")
    class EmailAddressTests {

        @Test
        @DisplayName("should reject strings without @")
        void shouldRejectWithoutAt() {
            assertThatThrownBy(() -> new EmailAddress("no-at-sign.com"))
                    .isInstanceOf(PatientDomainException.class)
                    .hasMessageContaining("Invalid email");
        }

        @Test
        @DisplayName("should accept valid email")
        void shouldAcceptValidEmail() {
            var email = new EmailAddress("user@example.com");
            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should reject blank email")
        void shouldRejectBlank() {
            assertThatThrownBy(() -> new EmailAddress(" "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("pullDomainEvents() drains events")
    class EventDrainTests {

        @Test
        @DisplayName("second call returns empty list")
        void secondCallReturnsEmpty() {
            Patient patient = Patient.register(TENANT_ID, defaultPersonalInfo(), defaultContactInfo(),
                    defaultMrn(), defaultPreference());
            var first = patient.pullDomainEvents();
            assertThat(first).isNotEmpty();

            var second = patient.pullDomainEvents();
            assertThat(second).isEmpty();
        }
    }
}