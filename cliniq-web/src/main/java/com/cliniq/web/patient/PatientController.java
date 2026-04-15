package com.cliniq.web.patient;

import com.cliniq.application.patient.command.RegisterPatientCommand;
import com.cliniq.application.patient.command.UpdateContactInfoCommand;
import com.cliniq.application.patient.command.UpdateNotificationPreferenceCommand;
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
import com.cliniq.web.patient.dto.PatientResponse;
import com.cliniq.web.patient.dto.RegisterPatientRequest;
import com.cliniq.web.patient.dto.UpdateContactInfoRequest;
import com.cliniq.web.patient.dto.UpdateNotificationPreferenceRequest;
import com.cliniq.web.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final RegisterPatientUseCase registerUseCase;
    private final QueryPatientUseCase queryPatientUseCase;
    private final UpdateContactInfoUseCase updateContactInfoUseCase;
    private final UpdateNotificationPreferenceUseCase updatePreferenceUseCase;

    public PatientController(RegisterPatientUseCase registerUseCase,
                             QueryPatientUseCase queryPatientUseCase,
                             UpdateContactInfoUseCase updateContactInfoUseCase,
                             UpdateNotificationPreferenceUseCase updatePreferenceUseCase) {
        this.registerUseCase = registerUseCase;
        this.queryPatientUseCase = queryPatientUseCase;
        this.updateContactInfoUseCase = updateContactInfoUseCase;
        this.updatePreferenceUseCase = updatePreferenceUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> register(@Valid @RequestBody RegisterPatientRequest request) {
        TenantId tenantId = TenantContext.require();

        PersonalInfo personalInfo = new PersonalInfo(
                request.givenName(),
                request.familyName(),
                request.dateOfBirth() != null ? request.dateOfBirth() : LocalDate.of(1900, 1, 1),
                Gender.valueOf(request.gender())
        );

        ContactInfo contactInfo = new ContactInfo(
                new PhoneNumber(request.phoneNumber()),
                request.emailAddress() != null ? new EmailAddress(request.emailAddress()) : null
        );

        MedicalRecordNumber mrn = new MedicalRecordNumber("MRN-" + UUID.randomUUID().toString().substring(0, 8));

        Channel channel = request.preferredChannel() != null
                ? resolveChannel(request.preferredChannel())
                : Channel.Sms.INSTANCE;
        boolean optedOut = request.optedOut() != null && request.optedOut();
        NotificationPreference preference = new NotificationPreference(channel, optedOut);

        RegisterPatientCommand command = new RegisterPatientCommand(
                tenantId, personalInfo, contactInfo, mrn, preference
        );

        PatientId patientId = registerUseCase.register(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(patientId.value())
                .toUri();

        return ResponseEntity.created(location)
                .body(Map.of("patientId", patientId.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        Patient patient = queryPatientUseCase.findById(tenantId, PatientId.of(id));
        return ResponseEntity.ok(toPatientResponse(patient));
    }

    @PatchMapping("/{id}/contact")
    public ResponseEntity<Void> updateContactInfo(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateContactInfoRequest request) {
        TenantId tenantId = TenantContext.require();
        Patient patient = queryPatientUseCase.findById(tenantId, PatientId.of(id));

        ContactInfo existingContact = patient.getContactInfo();
        ContactInfo contactInfo = new ContactInfo(
                request.phoneNumber() != null ? new PhoneNumber(request.phoneNumber()) : existingContact.phoneNumber(),
                request.emailAddress() != null ? new EmailAddress(request.emailAddress()) : existingContact.emailAddress()
        );

        UpdateContactInfoCommand command = new UpdateContactInfoCommand(
                tenantId, PatientId.of(id), contactInfo
        );

        updateContactInfoUseCase.updateContactInfo(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/notification-preference")
    public ResponseEntity<Void> updateNotificationPreference(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        TenantId tenantId = TenantContext.require();

        Channel channel = request.preferredChannel() != null
                ? resolveChannel(request.preferredChannel())
                : Channel.Sms.INSTANCE;
        boolean optedOut = request.optedOut() != null && request.optedOut();

        NotificationPreference preference = new NotificationPreference(channel, optedOut);

        UpdateNotificationPreferenceCommand command = new UpdateNotificationPreferenceCommand(
                tenantId, PatientId.of(id), preference
        );

        updatePreferenceUseCase.updatePreference(command);
        return ResponseEntity.noContent().build();
    }

    private static Channel resolveChannel(String channelName) {
        return switch (channelName.toLowerCase()) {
            case "email" -> Channel.Email.INSTANCE;
            case "sms" -> Channel.Sms.INSTANCE;
            default -> throw new IllegalArgumentException("Unknown channel: " + channelName);
        };
    }

    private static PatientResponse toPatientResponse(Patient patient) {
        PersonalInfo pi = patient.getPersonalInfo();
        ContactInfo ci = patient.getContactInfo();
        NotificationPreference np = patient.getNotificationPreference();

        return new PatientResponse(
                patient.getId().value(),
                patient.getTenantId().value(),
                pi.givenName(),
                pi.familyName(),
                pi.dateOfBirth(),
                pi.gender().name(),
                ci.phoneNumber() != null ? ci.phoneNumber().value() : null,
                ci.emailAddress() != null ? ci.emailAddress().value() : null,
                patient.getMedicalRecordNumber().value(),
                np.preferredChannel() instanceof Channel.Email ? "email" : "sms",
                np.optedOut()
        );
    }
}
