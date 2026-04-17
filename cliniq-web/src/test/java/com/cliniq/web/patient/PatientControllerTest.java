package com.cliniq.web.patient;

import com.cliniq.application.patient.port.in.QueryPatientUseCase;
import com.cliniq.application.patient.port.in.RegisterPatientUseCase;
import com.cliniq.application.patient.port.in.UpdateContactInfoUseCase;
import com.cliniq.application.patient.port.in.UpdateNotificationPreferenceUseCase;
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
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.patient.dto.RegisterPatientRequest;
import com.cliniq.web.patient.dto.UpdateContactInfoRequest;
import com.cliniq.web.patient.dto.UpdateNotificationPreferenceRequest;
import com.cliniq.web.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    private static final UUID TENANT_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PATIENT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final TenantId TENANT   = TenantId.of(TENANT_UUID);

    @Mock private RegisterPatientUseCase              registerUseCase;
    @Mock private QueryPatientUseCase                 queryPatientUseCase;
    @Mock private UpdateContactInfoUseCase            updateContactInfoUseCase;
    @Mock private UpdateNotificationPreferenceUseCase updatePreferenceUseCase;

    private PatientController controller;

    @BeforeEach
    void setUp() {
        controller = new PatientController(
                registerUseCase, queryPatientUseCase,
                updateContactInfoUseCase, updatePreferenceUseCase);
        // PatientController.register() calls ServletUriComponentsBuilder.fromCurrentRequest()
        // which requires an active servlet request in the thread-local context
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("register returns 201 Created with patientId in body")
    void register_validRequest_returnsCreated() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(registerUseCase.register(any())).thenReturn(PatientId.of(PATIENT_UUID));

            var request = new RegisterPatientRequest(
                    "Ali", "Hassan", LocalDate.of(1990, 3, 15),
                    "MALE", "+201012345678", "ali@example.com", "email", false);

            var response = controller.register(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).containsEntry("patientId", PATIENT_UUID);
            verify(registerUseCase).register(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.personalInfo().givenName().equals("Ali") &&
                    cmd.personalInfo().gender() == Gender.MALE));
        }
    }

    @Test
    @DisplayName("register defaults channel to SMS when preferredChannel is null")
    void register_nullChannel_defaultsToSms() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);
            when(registerUseCase.register(any())).thenReturn(PatientId.of(PATIENT_UUID));

            var request = new RegisterPatientRequest(
                    "Ali", "Hassan", null, "MALE", "+201012345678", "ali@example.com", null, null);

            controller.register(request);

            verify(registerUseCase).register(argThat(cmd ->
                    cmd.notificationPreference().preferredChannel() instanceof Channel.Sms &&
                    !cmd.notificationPreference().optedOut()));
        }
    }

    @Test
    @DisplayName("getPatient returns 200 with mapped patient response")
    void getPatient_existingPatient_returnsOk() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            Patient patient = mockPatient();
            when(queryPatientUseCase.findById(TENANT, PatientId.of(PATIENT_UUID))).thenReturn(patient);

            var response = controller.getPatient(PATIENT_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().id()).isEqualTo(PATIENT_UUID);
            assertThat(response.getBody().givenName()).isEqualTo("Ali");
            assertThat(response.getBody().gender()).isEqualTo("MALE");
            assertThat(response.getBody().preferredChannel()).isEqualTo("email");
        }
    }

    @Test
    @DisplayName("updateContactInfo returns 204 No Content")
    void updateContactInfo_validRequest_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            Patient patient = mockPatient();
            when(queryPatientUseCase.findById(any(), any())).thenReturn(patient);

            var request = new UpdateContactInfoRequest("+201099999999", null);
            var response = controller.updateContactInfo(PATIENT_UUID, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(updateContactInfoUseCase).updateContactInfo(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.patientId().equals(PatientId.of(PATIENT_UUID)) &&
                    cmd.newContactInfo().phoneNumber().value().equals("+201099999999")));
        }
    }

    @Test
    @DisplayName("updateNotificationPreference with email channel returns 204")
    void updateNotificationPreference_emailChannel_returnsNoContent() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var request = new UpdateNotificationPreferenceRequest("email", true);
            var response = controller.updateNotificationPreference(PATIENT_UUID, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(updatePreferenceUseCase).updatePreference(argThat(cmd ->
                    cmd.newPreference().preferredChannel() instanceof Channel.Email &&
                    cmd.newPreference().optedOut()));
        }
    }

    private Patient mockPatient() {
        return Patient.reconstruct(
                PatientId.of(PATIENT_UUID), TENANT,
                new PersonalInfo("Ali", "Hassan", LocalDate.of(1990, 3, 15), Gender.MALE),
                new ContactInfo(new PhoneNumber("+201012345678"), new EmailAddress("ali@example.com")),
                new MedicalRecordNumber("MRN-ABC123"),
                new NotificationPreference(Channel.Email.INSTANCE, false));
    }
}
