# Tasks: Cliniq — Patient Appointment & Reminder System

**Input**: Design documents from `/specs/001-cliniq-full-implementation/`  
**Prerequisites**: plan.md ✅ | research.md ✅ | data-model.md ✅ | contracts/rest-api.md ✅ | quickstart.md ✅

**Implementation Note for AI agents**: Every task below is self-contained. Each task specifies:
- The exact file path to create or modify
- The exact class/interface name
- The exact fields, method signatures, and rules to implement
- Any invariants or validation logic required
- Dependencies on other tasks

**Constitution compliance**: Tests MUST be written before implementation per Principle IV. Domain classes MUST contain zero Spring/JPA imports per Principle I.

---

## Phase 1: Setup (Maven Module Scaffolding)

**Purpose**: Create all Maven module directories and POM files so the multi-module build compiles end-to-end.

- [ ] T001 Verify `cliniq-shared-kernel` module compiles — run `./mvnw test -pl cliniq-shared-kernel` and confirm 0 errors. The following files already exist: `TenantId.java`, `AggregateRoot.java`, `DomainEvent.java`, `DomainException.java`, `ValueObject.java`, `Preconditions.java`. Fix any compilation errors found.

- [ ] T002 Create Maven module directory and `pom.xml` for `cliniq-domain`. The POM must: (a) declare parent as `com.cliniq:cliniq-parent`, (b) have artifactId `cliniq-domain`, (c) depend on `com.cliniq:cliniq-shared-kernel`. No Spring or JPA dependencies. Java 21 source. File: `cliniq-domain/pom.xml`.

- [ ] T003 [P] Create Maven module directory and `pom.xml` for `cliniq-application`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-application`, (c) depend on `cliniq-domain` and `cliniq-shared-kernel`. No Spring or JPA dependencies. File: `cliniq-application/pom.xml`.

- [ ] T004 [P] Create Maven module directory and `pom.xml` for `cliniq-persistence`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-persistence`, (c) depend on `cliniq-application`, `cliniq-domain`, `cliniq-shared-kernel`, Spring Data JPA, Flyway, PostgreSQL JDBC driver. File: `cliniq-persistence/pom.xml`.

- [ ] T005 [P] Create Maven module directory and `pom.xml` for `cliniq-web`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-web`, (c) depend on `cliniq-application`, `cliniq-domain`, `cliniq-shared-kernel`, Spring Web, Spring Security, springdoc-openapi. File: `cliniq-web/pom.xml`.

- [ ] T006 [P] Create Maven module directory and `pom.xml` for `cliniq-notification-adapter`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-notification-adapter`, (c) depend on `cliniq-application`, `cliniq-domain`, Twilio Java SDK, Resilience4j Spring Boot starter. File: `cliniq-notification-adapter/pom.xml`.

- [ ] T007 [P] Create Maven module directory and `pom.xml` for `cliniq-external-adapter`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-external-adapter`, (c) depend on `cliniq-application`, `cliniq-domain`, Spring Web (RestClient), Caffeine cache, Google Calendar API client library. File: `cliniq-external-adapter/pom.xml`.

- [ ] T008 Create Maven module directory and `pom.xml` for `cliniq-bootstrap`. The POM must: (a) parent `com.cliniq:cliniq-parent`, (b) artifactId `cliniq-bootstrap`, (c) depend on ALL other modules: `cliniq-application`, `cliniq-domain`, `cliniq-shared-kernel`, `cliniq-persistence`, `cliniq-web`, `cliniq-notification-adapter`, `cliniq-external-adapter`, Spring Boot starter, ArchUnit. This is the only module that may depend on all others. File: `cliniq-bootstrap/pom.xml`.

- [ ] T009 Update the parent `pom.xml` at repository root to declare all 8 modules in `<modules>` section: `cliniq-shared-kernel`, `cliniq-domain`, `cliniq-application`, `cliniq-persistence`, `cliniq-web`, `cliniq-notification-adapter`, `cliniq-external-adapter`, `cliniq-bootstrap`. Also add `<dependencyManagement>` entries for all internal artifacts at version `0.1.0-SNAPSHOT`. File: `pom.xml`.

- [ ] T010 Verify full multi-module build compiles: run `./mvnw clean compile -DskipTests`. Fix any POM resolution errors. All 8 modules must reach the `compile` phase without error.

---

## Phase 2: Foundational — Domain Layer (Blocking)

**Purpose**: Implement all pure Java 21 domain classes across all four Bounded Contexts. MUST be complete before application ports or adapters are written. Zero Spring/JPA imports allowed in any file in this phase.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Patient Bounded Context

- [ ] T011 [P] Create `PatientId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/PatientId.java`. Implement as a Java record: `public record PatientId(UUID value) implements ValueObject`. Add a static factory: `public static PatientId generate() { return new PatientId(UUID.randomUUID()); }`. Add a static factory: `public static PatientId of(UUID value) { return new PatientId(Preconditions.requireNonNull(value, "PatientId value")); }`. Import only `java.util.UUID`, `com.cliniq.shared.domain.ValueObject`, `com.cliniq.shared.validation.Preconditions`.

- [ ] T012 [P] Create `Gender` enum. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/Gender.java`. Values: `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`.

- [ ] T013 [P] Create `PhoneNumber` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/PhoneNumber.java`. Implement as a record: `public record PhoneNumber(String value) implements ValueObject`. In the compact constructor, validate using `Preconditions.requireNotBlank(value, "phone number")` and then check it matches the E.164 regex `^\+[1-9]\d{1,14}$` — if it does not match, throw `new DomainException("Phone number must be in E.164 format: " + value)`.

- [ ] T014 [P] Create `EmailAddress` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/EmailAddress.java`. Implement as a record: `public record EmailAddress(String value) implements ValueObject`. In compact constructor, validate with `Preconditions.requireNotBlank(value, "email")` and check value contains exactly one `@` and at least one `.` after the `@`. If invalid, throw `new DomainException("Invalid email address: " + value)`.

- [ ] T015 [P] Create `MedicalRecordNumber` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/MedicalRecordNumber.java`. Implement as a record: `public record MedicalRecordNumber(String value) implements ValueObject`. Compact constructor: `Preconditions.requireNotBlank(value, "medical record number")`.

- [ ] T016 [P] Create `PersonalInfo` value object (immutable record). File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/PersonalInfo.java`. Implement as: `public record PersonalInfo(String givenName, String familyName, LocalDate dateOfBirth, Gender gender) implements ValueObject`. Compact constructor: validate all fields non-null using `Preconditions.requireNonNull`. Import `java.time.LocalDate`.

- [ ] T017 [P] Create `NotificationPreference` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/NotificationPreference.java`. Implement as: `public record NotificationPreference(Channel preferredChannel, boolean optedOut) implements ValueObject`. Import `Channel` from `com.cliniq.domain.notification.Channel`. Add a static factory: `public static NotificationPreference defaultPreference() { return new NotificationPreference(Channel.Sms.INSTANCE, false); }` (adjust when Channel is defined in T039).

- [ ] T018 [P] Create `ContactInfo` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/ContactInfo.java`. Implement as: `public record ContactInfo(PhoneNumber phoneNumber, EmailAddress emailAddress) implements ValueObject`. Compact constructor validates both fields non-null.

- [ ] T019 Create `PatientEvent` sealed interface. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/event/PatientEvent.java`. Content: `public sealed interface PatientEvent extends DomainEvent permits PatientRegistered, ContactInfoUpdated, NotificationPreferenceChanged {}`. This is the authoritative registry of all Patient BC events.

- [ ] T020 Create `PatientRegistered` domain event record. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/event/PatientRegistered.java`. Implement: `public record PatientRegistered(UUID eventId, Instant occurredAt, TenantId tenantId, PatientId patientId, PersonalInfo personalInfo, ContactInfo contactInfo) implements PatientEvent`. Add static factory: `public static PatientRegistered of(TenantId tenantId, PatientId patientId, PersonalInfo info, ContactInfo contact) { return new PatientRegistered(UUID.randomUUID(), Instant.now(), tenantId, patientId, info, contact); }`.

- [ ] T021 [P] Create `ContactInfoUpdated` domain event. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/event/ContactInfoUpdated.java`. Same structure as `PatientRegistered`: record implementing `PatientEvent`, fields `eventId`, `occurredAt`, `tenantId`, `patientId`, `newContactInfo (ContactInfo)`. Static factory `of(TenantId, PatientId, ContactInfo)`.

- [ ] T022 [P] Create `NotificationPreferenceChanged` domain event. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/event/NotificationPreferenceChanged.java`. Record implementing `PatientEvent`, fields: `eventId`, `occurredAt`, `tenantId`, `patientId`, `newPreference (NotificationPreference)`.

- [ ] T023 Create `PatientDomainException` base exception. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/exception/PatientDomainException.java`. Extends `DomainException`. Constructor: `public PatientDomainException(String message) { super(message); }`.

- [ ] T024 [P] Create `PatientNotFoundException` exception. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/exception/PatientNotFoundException.java`. Extends `PatientDomainException`. Constructor: `public PatientNotFoundException(PatientId id) { super("Patient not found: " + id.value()); }`.

- [ ] T025 Create `Patient` aggregate root. File: `cliniq-domain/src/main/java/com/cliniq/domain/patient/Patient.java`. Rules:
  - Extends `AggregateRoot`.
  - Fields: `PatientId id`, `TenantId tenantId`, `PersonalInfo personalInfo`, `ContactInfo contactInfo`, `MedicalRecordNumber medicalRecordNumber`, `NotificationPreference notificationPreference`.
  - Private constructor. All fields final (or effectively final — set once).
  - Static factory: `public static Patient register(TenantId tenantId, PersonalInfo personalInfo, ContactInfo contactInfo, MedicalRecordNumber mrn, NotificationPreference preference)` — validates all args non-null using `Preconditions`, sets `id = PatientId.generate()`, registers `PatientRegistered` event via `registerEvent(...)`.
  - Method: `public void updateContactInfo(ContactInfo newContactInfo)` — validates non-null, replaces field, registers `ContactInfoUpdated` event.
  - Method: `public void updateNotificationPreference(NotificationPreference preference)` — validates non-null, replaces field, registers `NotificationPreferenceChanged` event.
  - All getters. No setters.
  - ZERO Spring/JPA imports.

- [ ] T026 Write unit tests for `Patient` aggregate. File: `cliniq-domain/src/test/java/com/cliniq/domain/patient/PatientTest.java`. Tests MUST cover:
  - `register()` creates patient with correct fields and registers `PatientRegistered` event.
  - `register()` throws `DomainException` when any required field is null.
  - `updateContactInfo()` replaces contact and registers `ContactInfoUpdated` event.
  - `updateNotificationPreference()` replaces preference and registers `NotificationPreferenceChanged` event.
  - `PhoneNumber` rejects non-E.164 strings.
  - `EmailAddress` rejects strings without `@`.
  Use JUnit 5 (`@Test`, `assertThrows`, `assertEquals`). No Spring context.

### Provider Bounded Context

- [ ] T027 [P] Create `ProviderId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/ProviderId.java`. Same pattern as `PatientId`: record with `UUID value`, static `generate()` and `of(UUID)` factories, implements `ValueObject`.

- [ ] T028 [P] Create `ProviderName` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/ProviderName.java`. Record: `public record ProviderName(String givenName, String familyName) implements ValueObject`. Compact constructor: `Preconditions.requireNotBlank` on both fields.

- [ ] T029 [P] Create `Specialty` enum. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/Specialty.java`. Values: `GENERAL_PRACTICE`, `CARDIOLOGY`, `DERMATOLOGY`, `NEUROLOGY`, `ORTHOPEDICS`, `PEDIATRICS`, `PSYCHIATRY`, `ONCOLOGY`, `OTHER`.

- [ ] T030 [P] Create `AvailabilitySlot` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/AvailabilitySlot.java`. Record: `public record AvailabilitySlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) implements ValueObject`. Compact constructor validates: all non-null; `endTime` must be after `startTime` — if not, throw `new DomainException("AvailabilitySlot end must be after start")`.

- [ ] T031 [P] Create `ProviderNotFoundException` exception. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/exception/ProviderNotFoundException.java`. Extends `DomainException`. Constructor: `public ProviderNotFoundException(ProviderId id) { super("Provider not found: " + id.value()); }`.

- [ ] T032 Create `Provider` aggregate root. File: `cliniq-domain/src/main/java/com/cliniq/domain/provider/Provider.java`. Rules:
  - Extends `AggregateRoot`.
  - Fields: `ProviderId id`, `TenantId tenantId`, `ProviderName name`, `Specialty specialty`, `List<AvailabilitySlot> availabilitySlots` (unmodifiable list).
  - Static factory: `public static Provider create(TenantId tenantId, ProviderName name, Specialty specialty, List<AvailabilitySlot> slots)` — validates non-null, stores defensive copy of slots as unmodifiable list.
  - Method: `public boolean isAvailable(TimeSlot timeSlot)` — returns `true` if any `AvailabilitySlot` covers the given `TimeSlot`'s day-of-week and the timeSlot's start/end falls within the slot's start/end. Import `TimeSlot` from appointment BC (use `com.cliniq.domain.appointment.TimeSlot`).
  - Method: `public void syncFrom(ProviderName name, Specialty specialty, List<AvailabilitySlot> slots)` — updates the provider's fields (used for EHR sync).
  - Provider is reference data — no domain events required.
  - No Spring/JPA imports.

- [ ] T033 Write unit tests for `Provider`. File: `cliniq-domain/src/test/java/com/cliniq/domain/provider/ProviderTest.java`. Tests:
  - `create()` stores fields correctly.
  - `isAvailable()` returns true for a TimeSlot that falls within an AvailabilitySlot.
  - `isAvailable()` returns false for a TimeSlot outside all slots.
  - `syncFrom()` updates name, specialty, and slots.
  - `AvailabilitySlot` rejects endTime ≤ startTime.

### Appointment Bounded Context

- [ ] T034 [P] Create `AppointmentId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/AppointmentId.java`. Same pattern: record, `UUID value`, `generate()`, `of(UUID)`, implements `ValueObject`.

- [ ] T035 [P] Create `PrescriptionId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/PrescriptionId.java`. Same pattern.

- [ ] T036 [P] Create `AppointmentType` enum. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/AppointmentType.java`. Values: `GENERAL`, `FOLLOW_UP`, `SPECIALIST`, `EMERGENCY`.

- [ ] T037 [P] Create `TimeSlot` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/TimeSlot.java`. Record: `public record TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) implements ValueObject`. Compact constructor: validate all non-null, `endTime` must be after `startTime` (throw `DomainException` if not), `date` must not be in the past (throw `DomainException("Appointment date cannot be in the past")` if `date.isBefore(LocalDate.now())`).

- [ ] T038 [P] Create `CancellationReason` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/CancellationReason.java`. Record: `public record CancellationReason(String code, String description) implements ValueObject`. Both fields validated as non-blank.

- [ ] T039 [P] Create `MedicationReference` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/MedicationReference.java`. Record: `public record MedicationReference(String ndcCode, String brandName, String genericName) implements ValueObject`. Compact constructor: validate `ndcCode` non-blank (it is the primary identifier).

- [ ] T040 [P] Create `AppointmentStatus` sealed interface (NOT an enum — it is a sealed type hierarchy). File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/AppointmentStatus.java`. Content:
  ```java
  public sealed interface AppointmentStatus
      permits AppointmentStatus.Scheduled, AppointmentStatus.Confirmed,
              AppointmentStatus.Cancelled, AppointmentStatus.Completed,
              AppointmentStatus.NoShow {
    record Scheduled() implements AppointmentStatus {}
    record Confirmed() implements AppointmentStatus {}
    record Cancelled(CancellationReason reason) implements AppointmentStatus {}
    record Completed() implements AppointmentStatus {}
    record NoShow() implements AppointmentStatus {}
  }
  ```
  All nested records are `public`. `Cancelled` carries the `CancellationReason`.

- [ ] T041 [P] Create `Prescription` entity (child of Appointment aggregate). File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/Prescription.java`. It is NOT an aggregate root. Fields: `PrescriptionId id`, `MedicationReference medication`, `String dosage`, `String instructions`. Package-private constructor (created only by `Appointment`). Static factory `create(MedicationReference, String dosage, String instructions)` that validates non-null/non-blank and generates id.

- [ ] T042 Create `AppointmentEvent` sealed interface. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/event/AppointmentEvent.java`. Content: `public sealed interface AppointmentEvent extends DomainEvent permits AppointmentScheduled, AppointmentConfirmed, AppointmentCancelled, AppointmentCompleted, AppointmentMarkedNoShow {}`.

- [ ] T043 [P] Create all five Appointment domain event records in `cliniq-domain/src/main/java/com/cliniq/domain/appointment/event/`:
  - `AppointmentScheduled.java`: record with `eventId(UUID)`, `occurredAt(Instant)`, `tenantId(TenantId)`, `appointmentId(AppointmentId)`, `patientId(PatientId)`, `providerId(ProviderId)`, `timeSlot(TimeSlot)`, `type(AppointmentType)`. Implements `AppointmentEvent`. Static factory `of(...)`.
  - `AppointmentConfirmed.java`: fields `eventId`, `occurredAt`, `tenantId`, `appointmentId`.
  - `AppointmentCancelled.java`: fields `eventId`, `occurredAt`, `tenantId`, `appointmentId`, `reason(CancellationReason)`.
  - `AppointmentCompleted.java`: fields `eventId`, `occurredAt`, `tenantId`, `appointmentId`.
  - `AppointmentMarkedNoShow.java`: fields `eventId`, `occurredAt`, `tenantId`, `appointmentId`.
  All are records implementing `AppointmentEvent`.

- [ ] T044 [P] Create Appointment BC exceptions. Files in `cliniq-domain/src/main/java/com/cliniq/domain/appointment/exception/`:
  - `AppointmentDomainException.java`: extends `DomainException`, constructor `(String message)`.
  - `AppointmentNotFoundException.java`: extends `AppointmentDomainException`, constructor `(AppointmentId id)` with message `"Appointment not found: " + id.value()`.
  - `SlotUnavailableException.java`: extends `AppointmentDomainException`, constructor `(TimeSlot slot)` with message `"Time slot is not available: " + slot`.
  - `InvalidStatusTransitionException.java`: extends `AppointmentDomainException`, constructor `(AppointmentStatus current, String attemptedAction)` with message `"Cannot " + attemptedAction + " when status is " + current.getClass().getSimpleName()`.

- [ ] T045 Create `Appointment` aggregate root. File: `cliniq-domain/src/main/java/com/cliniq/domain/appointment/Appointment.java`. This is the most complex class. Rules:
  - Extends `AggregateRoot`.
  - Fields: `AppointmentId id`, `TenantId tenantId`, `PatientId patientId`, `ProviderId providerId`, `TimeSlot timeSlot`, `AppointmentType type`, `AppointmentStatus status`, `List<Prescription> prescriptions`, `long version`.
  - Private constructor.
  - Static factory: `public static Appointment schedule(TenantId tenantId, PatientId patientId, ProviderId providerId, TimeSlot timeSlot, AppointmentType type, Provider provider)`. Logic: (1) validate all args non-null, (2) call `provider.isAvailable(timeSlot)` — throw `SlotUnavailableException(timeSlot)` if false, (3) set `status = new AppointmentStatus.Scheduled()`, (4) `prescriptions = new ArrayList<>()`, (5) register `AppointmentScheduled` event.
  - `public void confirm()`: only allowed when `status instanceof AppointmentStatus.Scheduled` — else throw `InvalidStatusTransitionException(status, "confirm")`. Set `status = new AppointmentStatus.Confirmed()`. Register `AppointmentConfirmed` event.
  - `public void cancel(CancellationReason reason)`: allowed when `Scheduled` or `Confirmed` — else throw `InvalidStatusTransitionException`. Set `status = new AppointmentStatus.Cancelled(reason)`. Register `AppointmentCancelled` event.
  - `public void complete()`: only from `Confirmed` — else throw. Set `status = new AppointmentStatus.Completed()`. Register `AppointmentCompleted` event.
  - `public void markNoShow()`: only from `Confirmed` — else throw. Set `status = new AppointmentStatus.NoShow()`. Register `AppointmentMarkedNoShow` event.
  - `public PrescriptionId addPrescription(MedicationReference medication, String dosage, String instructions)`: only when `status instanceof AppointmentStatus.Confirmed || status instanceof AppointmentStatus.Completed` — else throw `AppointmentDomainException("Cannot add prescription to appointment in status: " + status.getClass().getSimpleName())`. Create `Prescription`, add to list, return its id.
  - All getters. Unmodifiable view of prescriptions list. No Spring/JPA imports.

- [ ] T046 Write unit tests for `Appointment`. File: `cliniq-domain/src/test/java/com/cliniq/domain/appointment/AppointmentTest.java`. Tests MUST cover:
  - `schedule()` creates appointment with `Scheduled` status and registers `AppointmentScheduled` event.
  - `schedule()` throws `SlotUnavailableException` when provider is not available for the time slot.
  - `confirm()` from `Scheduled` → `Confirmed` + event registered.
  - `confirm()` from `Confirmed` throws `InvalidStatusTransitionException`.
  - `cancel()` from `Scheduled` → `Cancelled` + event.
  - `cancel()` from `Confirmed` → `Cancelled` + event.
  - `cancel()` from `Completed` throws `InvalidStatusTransitionException`.
  - `complete()` from `Confirmed` → `Completed` + event.
  - `complete()` from `Scheduled` throws.
  - `markNoShow()` from `Confirmed` → `NoShow` + event.
  - `addPrescription()` from `Confirmed` succeeds.
  - `addPrescription()` from `Scheduled` throws.
  - `addPrescription()` from `Completed` succeeds.

### Notification Bounded Context

- [ ] T047 [P] Create `ReminderId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/ReminderId.java`. Same UUID value-object pattern.

- [ ] T048 [P] Create `OutboxEntryId` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/OutboxEntryId.java`. Same UUID value-object pattern.

- [ ] T049 [P] Create `Channel` sealed interface. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/Channel.java`. Content:
  ```java
  public sealed interface Channel permits Channel.Sms, Channel.Email {
    record Sms() implements Channel {
      public static final Sms INSTANCE = new Sms();
    }
    record Email() implements Channel {
      public static final Email INSTANCE = new Email();
    }
  }
  ```

- [ ] T050 [P] Create `ReminderStatus` sealed interface. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/ReminderStatus.java`. Content (same pattern as AppointmentStatus):
  ```java
  public sealed interface ReminderStatus permits ReminderStatus.Pending, ReminderStatus.Dispatched, ReminderStatus.Delivered, ReminderStatus.Failed {
    record Pending() implements ReminderStatus {}
    record Dispatched() implements ReminderStatus {}
    record Delivered() implements ReminderStatus {}
    record Failed(FailureReason reason) implements ReminderStatus {}
  }
  ```

- [ ] T051 [P] Create `FailureReason` value object. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/FailureReason.java`. Record: `public record FailureReason(String code, String message) implements ValueObject`. Both validated non-blank.

- [ ] T052 Create `NotificationEvent` sealed interface. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/event/NotificationEvent.java`. Content: `public sealed interface NotificationEvent extends DomainEvent permits ReminderScheduled, ReminderDispatched, ReminderFailed {}`.

- [ ] T053 [P] Create the three Notification event records in `cliniq-domain/src/main/java/com/cliniq/domain/notification/event/`:
  - `ReminderScheduled.java`: record, fields `eventId`, `occurredAt`, `tenantId`, `reminderId(ReminderId)`, `appointmentId(AppointmentId)`, `patientId(PatientId)`, `channel(Channel)`. Implements `NotificationEvent`.
  - `ReminderDispatched.java`: fields `eventId`, `occurredAt`, `tenantId`, `reminderId`.
  - `ReminderFailed.java`: fields `eventId`, `occurredAt`, `tenantId`, `reminderId`, `reason(FailureReason)`.

- [ ] T054 [P] Create `NotificationDomainException`. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/exception/NotificationDomainException.java`. Extends `DomainException`. Constructor `(String message)`.

- [ ] T055 Create `Reminder` aggregate root. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/Reminder.java`. Rules:
  - Extends `AggregateRoot`.
  - Fields: `ReminderId id`, `TenantId tenantId`, `AppointmentId appointmentId`, `PatientId patientId`, `Channel channel`, `ReminderStatus status`.
  - Static factory: `public static Reminder schedule(TenantId tenantId, AppointmentId appointmentId, PatientId patientId, Channel channel)`. Sets `status = new ReminderStatus.Pending()`. Registers `ReminderScheduled` event.
  - `public void dispatch()`: only from `Pending` — else throw `NotificationDomainException("Cannot dispatch reminder not in Pending status")`. Set `status = new ReminderStatus.Dispatched()`. Register `ReminderDispatched` event.
  - `public void markDelivered()`: only from `Dispatched` — else throw. Set `status = new ReminderStatus.Delivered()`. No event (delivery confirmation is terminal, no downstream listeners).
  - `public void markFailed(FailureReason reason)`: only from `Dispatched` — else throw. Set `status = new ReminderStatus.Failed(reason)`. Register `ReminderFailed` event.
  - No Spring/JPA imports.

- [ ] T056 Create `OutboxEntry` aggregate root. File: `cliniq-domain/src/main/java/com/cliniq/domain/notification/OutboxEntry.java`. Rules:
  - Extends `AggregateRoot`.
  - Fields: `OutboxEntryId id`, `TenantId tenantId`, `String aggregateType`, `String aggregateId`, `String eventType`, `String payload` (JSON string), `Instant occurredAt`, `Instant processedAt` (nullable).
  - Static factory: `public static OutboxEntry create(TenantId tenantId, String aggregateType, String aggregateId, String eventType, String payload)`. Sets `id = OutboxEntryId.generate()`, `occurredAt = Instant.now()`, `processedAt = null`.
  - `public void markProcessed()`: sets `processedAt = Instant.now()`. Throws `NotificationDomainException("OutboxEntry already processed")` if already processed.
  - `public boolean isProcessed()`: returns `processedAt != null`.
  - No Spring/JPA imports.

- [ ] T057 Write unit tests for `Reminder` and `OutboxEntry`. File: `cliniq-domain/src/test/java/com/cliniq/domain/notification/NotificationTest.java`. Tests:
  - `Reminder.schedule()` sets Pending status and registers `ReminderScheduled` event.
  - `Reminder.dispatch()` from Pending → Dispatched + event.
  - `Reminder.dispatch()` from Delivered throws.
  - `Reminder.markFailed(reason)` from Dispatched → Failed + event.
  - `Reminder.markDelivered()` from Dispatched → Delivered.
  - `OutboxEntry.create()` has null processedAt.
  - `OutboxEntry.markProcessed()` sets processedAt and `isProcessed()` returns true.
  - `OutboxEntry.markProcessed()` twice throws.

- [ ] T058 Run `./mvnw test -pl cliniq-domain` — ALL domain tests must pass before proceeding.

---

## Phase 3: Application Layer — Ports & Services (Blocking)

**Purpose**: Define all port interfaces (driving + driven) and command records. Then implement application services. MUST be complete before persistence or web adapters.

### Driving Ports (Use Case Interfaces)

- [ ] T059 [P] Create driving port interfaces for Patient BC. Files in `cliniq-application/src/main/java/com/cliniq/application/patient/port/in/`:
  - `RegisterPatientUseCase.java`: interface with one method `PatientId register(RegisterPatientCommand command)`.
  - `UpdateContactInfoUseCase.java`: interface with `void updateContactInfo(UpdateContactInfoCommand command)`.
  - `UpdateNotificationPreferenceUseCase.java`: interface with `void updatePreference(UpdateNotificationPreferenceCommand command)`.
  - `QueryPatientUseCase.java`: interface with `Patient findById(TenantId tenantId, PatientId id)`.

- [ ] T060 [P] Create command records for Patient BC. Files in `cliniq-application/src/main/java/com/cliniq/application/patient/command/`:
  - `RegisterPatientCommand.java`: record with fields `TenantId tenantId`, `PersonalInfo personalInfo`, `ContactInfo contactInfo`, `MedicalRecordNumber medicalRecordNumber`, `NotificationPreference notificationPreference`.
  - `UpdateContactInfoCommand.java`: record with `TenantId tenantId`, `PatientId patientId`, `ContactInfo newContactInfo`.
  - `UpdateNotificationPreferenceCommand.java`: record with `TenantId tenantId`, `PatientId patientId`, `NotificationPreference newPreference`.

- [ ] T061 [P] Create driving port interfaces for Appointment BC. Files in `cliniq-application/src/main/java/com/cliniq/application/appointment/port/in/`:
  - `ScheduleAppointmentUseCase.java`: `AppointmentId schedule(ScheduleAppointmentCommand command)`.
  - `ConfirmAppointmentUseCase.java`: `void confirm(ConfirmAppointmentCommand command)`.
  - `CancelAppointmentUseCase.java`: `void cancel(CancelAppointmentCommand command)`.
  - `CompleteAppointmentUseCase.java`: `void complete(CompleteAppointmentCommand command)`.
  - `MarkNoShowUseCase.java`: `void markNoShow(MarkNoShowCommand command)`.
  - `AddPrescriptionUseCase.java`: `PrescriptionId addPrescription(AddPrescriptionCommand command)`.
  - `QueryAppointmentUseCase.java`: `Appointment findById(TenantId tenantId, AppointmentId id)`, `List<Appointment> findByPatient(TenantId tenantId, PatientId patientId)`, `List<Appointment> findByProvider(TenantId tenantId, ProviderId providerId, LocalDate date)`.

- [ ] T062 [P] Create command records for Appointment BC. Files in `cliniq-application/src/main/java/com/cliniq/application/appointment/command/`:
  - `ScheduleAppointmentCommand.java`: `TenantId tenantId`, `PatientId patientId`, `ProviderId providerId`, `TimeSlot timeSlot`, `AppointmentType type`.
  - `ConfirmAppointmentCommand.java`: `TenantId tenantId`, `AppointmentId appointmentId`.
  - `CancelAppointmentCommand.java`: `TenantId tenantId`, `AppointmentId appointmentId`, `CancellationReason reason`.
  - `CompleteAppointmentCommand.java`: `TenantId tenantId`, `AppointmentId appointmentId`.
  - `MarkNoShowCommand.java`: `TenantId tenantId`, `AppointmentId appointmentId`.
  - `AddPrescriptionCommand.java`: `TenantId tenantId`, `AppointmentId appointmentId`, `MedicationReference medication`, `String dosage`, `String instructions`.

- [ ] T063 [P] Create driving port interfaces for Provider BC. Files in `cliniq-application/src/main/java/com/cliniq/application/provider/port/in/`:
  - `SyncProviderUseCase.java`: `void sync(SyncProviderCommand command)`.
  - `QueryProviderUseCase.java`: `Provider findById(TenantId tenantId, ProviderId id)`.

- [ ] T064 [P] Create `SyncProviderCommand.java`. File: `cliniq-application/src/main/java/com/cliniq/application/provider/command/SyncProviderCommand.java`. Record: `TenantId tenantId`, `ProviderId providerId`, `ProviderName name`, `Specialty specialty`, `List<AvailabilitySlot> availabilitySlots`.

- [ ] T065 [P] Create driving port interfaces for Notification BC. Files in `cliniq-application/src/main/java/com/cliniq/application/notification/port/in/`:
  - `ScheduleReminderUseCase.java`: `ReminderId schedule(ScheduleReminderCommand command)`.
  - `QueryReminderUseCase.java`: `List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId)`.

- [ ] T066 [P] Create `ScheduleReminderCommand.java`. File: `cliniq-application/src/main/java/com/cliniq/application/notification/command/ScheduleReminderCommand.java`. Record: `TenantId tenantId`, `AppointmentId appointmentId`, `PatientId patientId`, `Channel channel`.

### Driven Ports (Infrastructure Interfaces)

- [ ] T067 [P] Create driven port interfaces for repositories. Files in `cliniq-application/src/main/java/com/cliniq/application/`:
  - `patient/port/out/PatientRepository.java`: `void save(Patient patient)`, `Optional<Patient> findById(TenantId tenantId, PatientId id)`.
  - `provider/port/out/ProviderRepository.java`: `void save(Provider provider)`, `Optional<Provider> findById(TenantId tenantId, ProviderId id)`.
  - `appointment/port/out/AppointmentRepository.java`: `void save(Appointment appointment)`, `Optional<Appointment> findById(TenantId tenantId, AppointmentId id)`, `List<Appointment> findByPatient(TenantId tenantId, PatientId patientId)`, `List<Appointment> findByProviderAndDate(TenantId tenantId, ProviderId providerId, LocalDate date)`.
  - `notification/port/out/ReminderRepository.java`: `void save(Reminder reminder)`, `List<Reminder> findByAppointment(TenantId tenantId, AppointmentId appointmentId)`.
  - `notification/port/out/OutboxRepository.java`: `void save(OutboxEntry entry)`, `List<OutboxEntry> findUnprocessed(int limit)` (used by poller — `SELECT FOR UPDATE SKIP LOCKED`).

- [ ] T068 [P] Create remaining driven port interfaces. Files:
  - `cliniq-application/src/main/java/com/cliniq/application/notification/port/out/NotificationDispatchPort.java`: `DispatchResult dispatch(Reminder reminder, ContactInfo contactInfo)`. Also create `DispatchResult.java` record: `boolean success`, `String externalId` (nullable), `String errorMessage` (nullable).
  - `cliniq-application/src/main/java/com/cliniq/application/medication/port/out/MedicationLookupPort.java`: `List<MedicationReference> search(String query, TenantId tenantId)`.
  - `cliniq-application/src/main/java/com/cliniq/application/appointment/port/out/CalendarSyncPort.java`: `void syncAppointment(Appointment appointment)`, `void deleteEvent(AppointmentId appointmentId, TenantId tenantId)`.
  - `cliniq-application/src/main/java/com/cliniq/application/shared/port/out/DomainEventPublisher.java`: `void publish(List<DomainEvent> events)`.

### Application Services

- [ ] T069 Create `PatientService`. File: `cliniq-application/src/main/java/com/cliniq/application/patient/PatientService.java`. Rules:
  - Implements `RegisterPatientUseCase`, `UpdateContactInfoUseCase`, `UpdateNotificationPreferenceUseCase`, `QueryPatientUseCase`.
  - Constructor injection of `PatientRepository patientRepository` and `DomainEventPublisher eventPublisher`. No `@Autowired` — use plain constructor.
  - `register()`: call `Patient.register(...)`, save via `patientRepository.save(patient)`, publish events via `eventPublisher.publish(patient.pullEvents())`.
  - `updateContactInfo()`: load patient via `patientRepository.findById(command.tenantId(), command.patientId()).orElseThrow(() -> new PatientNotFoundException(...))`, call `patient.updateContactInfo(...)`, save, publish events.
  - `updatePreference()`: same pattern — load, mutate, save, publish.
  - `findById()`: delegate to `patientRepository.findById(...)`.orElseThrow.
  - This class must have ZERO Spring annotations. It is wired by the bootstrap module.

- [ ] T070 Create `ProviderService`. File: `cliniq-application/src/main/java/com/cliniq/application/provider/ProviderService.java`. Implements `SyncProviderUseCase`, `QueryProviderUseCase`. Constructor injects `ProviderRepository`. `sync()`: try to find existing provider by id — if found call `provider.syncFrom(...)` then save; if not found call `Provider.create(...)` then save. `findById()`: orElseThrow `ProviderNotFoundException`.

- [ ] T071 Create `AppointmentService`. File: `cliniq-application/src/main/java/com/cliniq/application/appointment/AppointmentService.java`. Implements all Appointment use case interfaces. Constructor injects `AppointmentRepository`, `ProviderRepository`, `OutboxRepository`, `DomainEventPublisher`. Rules:
  - `schedule()`: (1) load Provider via ProviderRepository — throw `ProviderNotFoundException` if absent, (2) call `Appointment.schedule(...)`, (3) in ONE logical operation: `appointmentRepository.save(appointment)` AND `outboxRepository.save(OutboxEntry.create(...))` for the `AppointmentScheduled` event — NOTE: persistence adapter must do this in one transaction, (4) publish events.
  - `confirm()`, `cancel()`, `complete()`, `markNoShow()`: load appointment, call aggregate method, save, publish events.
  - `addPrescription()`: load appointment, call `addPrescription(...)`, save, return `PrescriptionId`.
  - Query methods: delegate to repository.

- [ ] T072 Create `ReminderService`. File: `cliniq-application/src/main/java/com/cliniq/application/notification/ReminderService.java`. Implements `ScheduleReminderUseCase`, `QueryReminderUseCase`. Constructor injects `ReminderRepository`, `OutboxRepository`, `DomainEventPublisher`. `schedule()`: call `Reminder.schedule(...)`, save reminder AND outbox entry in same transaction (note: persistence layer must handle this), publish events.

- [ ] T073 Create `OutboxProcessorService`. File: `cliniq-application/src/main/java/com/cliniq/application/notification/OutboxProcessorService.java`. This class has NO Spring `@Scheduled` annotation — scheduling is wired in bootstrap. Constructor injects `OutboxRepository`, `DomainEventPublisher`, `ReminderService`, `NotificationDispatchPort`, `PatientRepository`. Method: `public void processNext(int batchSize)`. Logic:
  1. Call `outboxRepository.findUnprocessed(batchSize)`.
  2. For each `OutboxEntry`: deserialize `eventType` to determine which BC the event belongs to.
  3. If `eventType` contains `AppointmentScheduled`: extract `patientId` from payload, load patient to get `ContactInfo`, call `reminderService.schedule(...)` using patient's `preferredChannel`.
  4. If `eventType` contains `ReminderScheduled`: extract `reminderId`, load `Reminder`, load patient `ContactInfo`, call `notificationDispatchPort.dispatch(reminder, contactInfo)`. If dispatch succeeds, call `reminder.markDelivered()` and save. If fails, call `reminder.markFailed(FailureReason)` and save.
  5. Call `entry.markProcessed()` and save outbox entry.
  6. All steps for each entry must be wrapped in try-catch — if any step fails, log the error and continue to next entry (do not stop processing).

---

## Phase 4: User Story 1 — Patient Registration & Management

**Goal**: A clinic can register patients, retrieve them, and update their contact info and notification preferences via the REST API.

**Independent Test**: `POST /patients` returns 201 with a `patientId`. `GET /patients/{id}` returns the patient. `PATCH /patients/{id}/contact` updates contact info.

### Tests for US1 (MANDATORY — constitution Principle IV)

> **Write these tests FIRST. Verify they FAIL before writing implementation.**

- [ ] T074 [P] [US1] Write `PatientRepositoryIT` integration test. File: `cliniq-persistence/src/test/java/com/cliniq/persistence/patient/PatientRepositoryIT.java`. Uses Testcontainers (`@Testcontainers`, `@Container` with `PostgreSQLContainer`). Tests: save and findById round-trip for `Patient`, multi-tenancy isolation (patient saved under tenant A not found under tenant B). Run with `./mvnw test -pl cliniq-persistence -Dtest=PatientRepositoryIT`.

- [ ] T075 [P] [US1] Write `PatientControllerIT` integration test. File: `cliniq-web/src/test/java/com/cliniq/web/patient/PatientControllerIT.java`. Uses `@SpringBootTest` + `MockMvc`. Tests: `POST /api/v1/patients` returns 201 with location header. `GET /api/v1/patients/{id}` returns 200 with correct body. `POST /api/v1/patients` with missing required fields returns 422.

### Implementation for US1

- [ ] T076 [P] [US1] Create Flyway migrations V1 and V3. Files:
  - `cliniq-persistence/src/main/resources/db/migration/V1__create_tenants.sql`: Creates `tenants` table: `id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()`.
  - `cliniq-persistence/src/main/resources/db/migration/V3__create_patients.sql`: Creates `patients` table with all columns from data-model.md: `id UUID NOT NULL, tenant_id UUID NOT NULL REFERENCES tenants(id), given_name VARCHAR(255) NOT NULL, family_name VARCHAR(255) NOT NULL, dob DATE, gender VARCHAR(50), phone_number VARCHAR(20), email_address VARCHAR(255), medical_record_number VARCHAR(100), preferred_channel VARCHAR(10), opted_out BOOLEAN NOT NULL DEFAULT false, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), version BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id)`. Add index: `CREATE INDEX idx_patients_tenant ON patients(tenant_id, id)`.

- [ ] T077 [P] [US1] Create `JpaPatient` JPA entity. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/patient/JpaPatient.java`. Rules: annotate with `@Entity @Table(name="patients")`. Add `@Filter(name="tenantFilter", condition="tenant_id = :tenantId")` at class level. Fields map 1:1 to the `patients` table columns. Use `@Version` on `version` field. Use `@Id` on `id`. All fields are flat Java types (String, UUID, LocalDate, Boolean) — no embedded domain objects. This class MUST NOT import anything from `com.cliniq.domain`.

- [ ] T078 [P] [US1] Create `JpaPatientMapper`. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/patient/JpaPatientMapper.java`. Static methods: `public static JpaPatient toJpa(Patient patient)` and `public static Patient toDomain(JpaPatient jpa)`. The `toDomain` method reconstructs the domain object including its field values. Use a package-private static factory on `Patient` if needed, or reflection — discuss approach and choose the simpler one (recommend adding a package-private `Patient reconstruct(...)` static method in the domain class for persistence use).

- [ ] T079 [US1] Create `JpaPatientRepository` Spring Data repository. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/patient/JpaPatientRepository.java`. Interface: `public interface JpaPatientRepository extends JpaRepository<JpaPatient, UUID>`. Add method: `Optional<JpaPatient> findByIdAndTenantId(UUID id, UUID tenantId)`.

- [ ] T080 [US1] Create `JpaPatientAdapter`. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/patient/JpaPatientAdapter.java`. Implements `PatientRepository` from the application layer. Constructor injects `JpaPatientRepository`. `save(Patient)`: map to JPA entity, call `jpaRepo.save(...)`. `findById(TenantId, PatientId)`: call `jpaRepo.findByIdAndTenantId(patientId.value(), tenantId.value())`, map result to domain using mapper.

- [ ] T081 [P] [US1] Create Patient web DTOs. Files in `cliniq-web/src/main/java/com/cliniq/web/patient/dto/`:
  - `RegisterPatientRequest.java`: record with `String givenName`, `String familyName`, `LocalDate dateOfBirth`, `String gender`, `String phoneNumber`, `String emailAddress`, `String preferredChannel`, `Boolean optedOut`. All non-null validated with Jakarta `@NotBlank` / `@NotNull`.
  - `UpdateContactInfoRequest.java`: record with `String phoneNumber`, `String emailAddress`.
  - `UpdateNotificationPreferenceRequest.java`: record with `String preferredChannel`, `Boolean optedOut`.
  - `PatientResponse.java`: record with all patient fields for GET response.

- [ ] T082 [US1] Create `PatientController`. File: `cliniq-web/src/main/java/com/cliniq/web/patient/PatientController.java`. Annotate with `@RestController @RequestMapping("/api/v1/patients")`. Constructor injects `RegisterPatientUseCase`, `UpdateContactInfoUseCase`, `UpdateNotificationPreferenceUseCase`, `QueryPatientUseCase`. Endpoints:
  - `POST /`: calls `registerUseCase.register(...)` using `TenantContext.require()` for tenantId. Returns `ResponseEntity.created(URI.create("/api/v1/patients/" + id.value())).body(Map.of("patientId", id.value()))`.
  - `GET /{id}`: loads patient, maps to `PatientResponse`, returns 200.
  - `PATCH /{id}/contact`: calls `updateContactInfoUseCase`.
  - `PATCH /{id}/notification-preference`: calls `updatePreferenceUseCase`.

**Checkpoint**: Run `./mvnw test -pl cliniq-domain,cliniq-application,cliniq-persistence,cliniq-web`. Patient registration flow MUST be independently testable.

---

## Phase 5: User Story 2 — Provider Sync & Availability

**Goal**: An EHR system can sync provider data (name, specialty, availability slots) via the REST API. The Appointment scheduling feature depends on this data.

**Independent Test**: `POST /providers/sync` with a provider payload returns 200. `GET /providers/{id}` returns the synced provider.

### Tests for US2 (MANDATORY)

- [ ] T083 [P] [US2] Write `ProviderRepositoryIT`. File: `cliniq-persistence/src/test/java/com/cliniq/persistence/provider/ProviderRepositoryIT.java`. Tests: save and findById round-trip; availability slots persisted correctly; tenant isolation.

- [ ] T084 [P] [US2] Write `ProviderControllerIT`. File: `cliniq-web/src/test/java/com/cliniq/web/provider/ProviderControllerIT.java`. Tests: `POST /api/v1/providers/sync` returns 200. `GET /api/v1/providers/{id}` returns provider with slots.

### Implementation for US2

- [ ] T085 [P] [US2] Create Flyway migration V2. File: `cliniq-persistence/src/main/resources/db/migration/V2__create_providers.sql`. Creates `providers` table: `id UUID NOT NULL, tenant_id UUID NOT NULL REFERENCES tenants(id), given_name VARCHAR(255) NOT NULL, family_name VARCHAR(255) NOT NULL, specialty VARCHAR(100) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), version BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (id)`. Also creates `provider_availability_slots` table: `id UUID NOT NULL DEFAULT gen_random_uuid(), provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE CASCADE, day_of_week VARCHAR(10) NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, PRIMARY KEY (id)`. Index: `CREATE INDEX idx_providers_tenant ON providers(tenant_id, id)`.

- [ ] T086 [P] [US2] Create `JpaProvider` and `JpaAvailabilitySlot` JPA entities. Files in `cliniq-persistence/src/main/java/com/cliniq/persistence/provider/`. `JpaProvider`: `@Entity @Table(name="providers")`, `@OneToMany(cascade=ALL, orphanRemoval=true, fetch=EAGER)` for slots. `JpaAvailabilitySlot`: `@Entity @Table(name="provider_availability_slots")`. Both with `@Filter` for tenant isolation on `JpaProvider`.

- [ ] T087 [P] [US2] Create `JpaProviderMapper` (toJpa, toDomain) and `JpaProviderRepository` (`findByIdAndTenantId`). Same pattern as Patient.

- [ ] T088 [US2] Create `JpaProviderAdapter` implementing `ProviderRepository`. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/provider/JpaProviderAdapter.java`.

- [ ] T089 [P] [US2] Create Provider web DTOs and `ProviderController`. DTOs in `cliniq-web/src/main/java/com/cliniq/web/provider/dto/`: `SyncProviderRequest.java` (externalId, givenName, familyName, specialty, list of slot DTOs), `AvailabilitySlotDto.java`, `ProviderResponse.java`. Controller at `cliniq-web/src/main/java/com/cliniq/web/provider/ProviderController.java`: `POST /api/v1/providers/sync` → calls `syncProviderUseCase`; `GET /api/v1/providers/{id}` → calls `queryProviderUseCase`.

**Checkpoint**: Provider sync flow independently testable.

---

## Phase 6: User Story 3 — Appointment Scheduling & Full Lifecycle

**Goal**: A clinic can schedule, confirm, cancel, complete, or mark no-show for appointments. Prescriptions can be added post-confirmation. Appointments are linked to patients and providers.

**Independent Test**: Full lifecycle — `POST /appointments` → `PATCH confirm` → `POST prescriptions` → `PATCH complete` all return correct status codes and the appointment state is reflected in `GET /appointments/{id}`.

### Tests for US3 (MANDATORY)

- [ ] T090 [P] [US3] Write `AppointmentRepositoryIT`. File: `cliniq-persistence/src/test/java/com/cliniq/persistence/appointment/AppointmentRepositoryIT.java`. Tests: save and findById; findByPatient returns correct records; findByProviderAndDate filters correctly; tenant isolation (appointment under tenant A not found under tenant B); prescriptions persisted correctly.

- [ ] T091 [P] [US3] Write `AppointmentSchedulingIT`. File: `cliniq-persistence/src/test/java/com/cliniq/persistence/appointment/AppointmentSchedulingIT.java`. Full integration test using application services against real PostgreSQL (Testcontainers). Scenario: (1) sync provider, (2) register patient, (3) schedule appointment → verify `Scheduled` status, (4) confirm → verify `Confirmed`, (5) add prescription → verify stored, (6) complete → verify `Completed`.

- [ ] T092 [P] [US3] Write `AppointmentControllerIT`. File: `cliniq-web/src/test/java/com/cliniq/web/appointment/AppointmentControllerIT.java`. Tests all PATCH endpoints return 204. Tests `POST /appointments` returns 201. Tests `409` on invalid state transition. Tests `409` on slot unavailable.

### Implementation for US3

- [ ] T093 [P] [US3] Create Flyway migrations V4 and V5. Files:
  - `V4__create_appointments.sql`: appointments table with all columns from data-model.md (slot_date DATE, slot_start TIME, slot_end TIME, status VARCHAR, cancellation_code, cancellation_description). Indexes on (tenant_id, id), (tenant_id, patient_id), (tenant_id, provider_id, slot_date).
  - `V5__create_prescriptions.sql`: prescriptions table linked to appointments.

- [ ] T094 [P] [US3] Create `JpaAppointment`, `JpaPrescription` JPA entities, `JpaAppointmentMapper`, `JpaAppointmentRepository`. Same patterns. `AppointmentStatus` is stored as a VARCHAR (status name) + nullable `cancellation_code` and `cancellation_description`. Mapper reconstructs the sealed `AppointmentStatus` from stored strings.

- [ ] T095 [US3] Create `JpaAppointmentAdapter` implementing `AppointmentRepository`. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/appointment/JpaAppointmentAdapter.java`. `findByProviderAndDate` queries by `provider_id`, `tenant_id`, and `slot_date`.

- [ ] T096 [P] [US3] Create Appointment web DTOs. Files in `cliniq-web/src/main/java/com/cliniq/web/appointment/dto/`: `ScheduleAppointmentRequest.java`, `CancelAppointmentRequest.java`, `AddPrescriptionRequest.java`, `AppointmentResponse.java` (includes list of `PrescriptionResponse`).

- [ ] T097 [US3] Create `AppointmentController`. File: `cliniq-web/src/main/java/com/cliniq/web/appointment/AppointmentController.java`. All endpoints from `contracts/rest-api.md`: POST schedule, PATCH confirm/cancel/complete/no-show, POST prescriptions, GET by id, GET query. Use `TenantContext.require()` for all tenant resolution.

**Checkpoint**: Run `AppointmentSchedulingIT`. Full schedule → confirm → complete flow MUST pass.

---

## Phase 7: User Story 4 — Outbox Processing & Automated Reminders

**Goal**: When an appointment is scheduled, a reminder is automatically created and dispatched via the configured notification channel. The outbox pattern guarantees at-least-once delivery.

**Independent Test**: Schedule an appointment, wait one poll cycle (or trigger `OutboxProcessorService.processNext()` directly in test), verify a `Reminder` record exists with `Dispatched` or `Delivered` status.

### Tests for US4 (MANDATORY)

- [ ] T098 [P] [US4] Write `OutboxProcessorIT`. File: `cliniq-persistence/src/test/java/com/cliniq/persistence/outbox/OutboxProcessorIT.java`. Uses Testcontainers. Scenario: (1) save OutboxEntry with event type `AppointmentScheduled`, (2) call `outboxProcessorService.processNext(10)`, (3) verify `processedAt` is set on the OutboxEntry, (4) verify a `Reminder` was created. Uses a mock `NotificationDispatchPort`.

### Implementation for US4

- [ ] T099 [P] [US4] Create Flyway migrations V6 and V7. Files:
  - `V6__create_reminders.sql`: reminders table with all columns from data-model.md.
  - `V7__create_outbox.sql`: outbox_entries table. Add partial index: `CREATE INDEX idx_outbox_unprocessed ON outbox_entries (occurred_at) WHERE processed_at IS NULL`.

- [ ] T100 [P] [US4] Create `JpaReminder` JPA entity, `JpaReminderMapper`, `JpaReminderRepository`, `JpaReminderAdapter` implementing `ReminderRepository`. Same patterns. `ReminderStatus` stored as VARCHAR (status name + nullable failure fields).

- [ ] T101 [P] [US4] Create `JpaOutboxEntry` JPA entity, `JpaOutboxEntryRepository`, `JpaOutboxAdapter` implementing `OutboxRepository`. `findUnprocessed(int limit)` must use: `@Query("SELECT e FROM JpaOutboxEntry e WHERE e.processedAt IS NULL ORDER BY e.occurredAt ASC LIMIT :limit FOR UPDATE SKIP LOCKED")`. This ensures concurrent-safe polling.

- [ ] T102 [US4] Wire `OutboxProcessorService` with `@Scheduled` in the bootstrap module. File: `cliniq-bootstrap/src/main/java/com/cliniq/bootstrap/scheduling/OutboxScheduler.java`. This class has `@Component` and uses `@Scheduled(fixedDelay = 5000)` to call `outboxProcessorService.processNext(50)` every 5 seconds.

- [ ] T103 [P] [US4] Create Reminder web endpoints in `ReminderController`. File: `cliniq-web/src/main/java/com/cliniq/web/notification/ReminderController.java`. `POST /api/v1/reminders` → `scheduleReminderUseCase.schedule(...)`. `GET /api/v1/reminders?appointmentId=...` → `queryReminderUseCase.findByAppointment(...)`.

**Checkpoint**: Run `OutboxProcessorIT`. Outbox atomicity MUST be verified.

---

## Phase 8: User Story 5 — SMS & Email Notifications via Twilio

**Goal**: When a reminder is in `Pending` state and the outbox processor dispatches it, the notification is sent via Twilio (SMS or SendGrid email) with Resilience4j retry and circuit breaker.

**Independent Test**: Trigger `dispatch()` with a WireMock Twilio stub — verify the HTTP call is made and `Reminder` transitions to `Dispatched`. Trigger with a failing WireMock stub and verify retry and then `Failed` status after 3 attempts.

### Tests for US5 (MANDATORY)

- [ ] T104 [P] [US5] Write `TwilioAdapterIT`. File: `cliniq-notification-adapter/src/test/java/com/cliniq/notification/TwilioAdapterIT.java`. Uses WireMock to stub Twilio API. Tests: SMS dispatch success → `DispatchResult.success=true`. Email dispatch success. Twilio returns 500 → retry 3 times → `DispatchResult.success=false`. Circuit breaker opens after repeated failures.

### Implementation for US5

- [ ] T105 [US5] Create `TwilioNotificationAdapter`. File: `cliniq-notification-adapter/src/main/java/com/cliniq/notification/TwilioNotificationAdapter.java`. Implements `NotificationDispatchPort`. Constructor injects Twilio `Message` client and SendGrid client (or use Twilio's email via SendGrid). `dispatch(Reminder reminder, ContactInfo contactInfo)`: switch on `reminder.channel()` — if `Sms`, send SMS via Twilio API to `contactInfo.phoneNumber().value()`; if `Email`, send email via SendGrid to `contactInfo.emailAddress().value()`. Returns `DispatchResult`.

- [ ] T106 [US5] Add Resilience4j configuration. File: `cliniq-notification-adapter/src/main/java/com/cliniq/notification/NotificationResilienceConfig.java`. Configure a `RetryConfig` with 3 max attempts and exponential backoff (1s base, 2x multiplier). Configure a `CircuitBreakerConfig` with 50% failure threshold and 30s wait. Apply both to the `dispatch` call using decorators or `@Retry` / `@CircuitBreaker` if Spring Boot is wired. Since this module may not have Spring context directly, prefer programmatic Resilience4j decoration.

---

## Phase 9: User Story 6 — Medication Lookup via OpenFDA

**Goal**: A user can search for medications by name or NDC code. Results are fetched from the OpenFDA API and cached per-tenant for 1 hour.

**Independent Test**: `GET /api/v1/medications?q=aspirin` returns a list of `MedicationReference` objects with ndcCode, brandName, genericName. A second call within 1 hour returns cached results (verified via WireMock call count).

### Tests for US6 (MANDATORY)

- [ ] T107 [P] [US6] Write `OpenFdaAdapterIT`. File: `cliniq-external-adapter/src/test/java/com/cliniq/external/OpenFdaAdapterIT.java`. Uses WireMock. Tests: search returns mapped `MedicationReference` list; second call within TTL hits cache (WireMock verify called once); cache is per-tenant (different tenants get separate cache entries).

### Implementation for US6

- [ ] T108 [US6] Create `OpenFdaMedicationAdapter`. File: `cliniq-external-adapter/src/main/java/com/cliniq/external/OpenFdaMedicationAdapter.java`. Implements `MedicationLookupPort`. Constructor injects Spring `RestClient` (configured with 5s connection timeout, 10s read timeout) and `LoadingCache<String, List<MedicationReference>>` (Caffeine, 1h TTL, 1000 max entries). Cache key: `tenantId.value() + ":" + query`. `search(query, tenantId)`: return `cache.get(tenantId.value() + ":" + query, key -> fetchFromFda(query))`. `fetchFromFda(query)`: call FDA URL `https://api.fda.gov/drug/ndc.json?search=brand_name:${query}&limit=20`, parse JSON response's `results` array, map each to `MedicationReference`.

- [ ] T109 [US6] Create `MedicationController`. File: `cliniq-web/src/main/java/com/cliniq/web/medication/MedicationController.java`. `GET /api/v1/medications?q={query}` → calls `medicationLookupPort.search(query, TenantContext.require())` → returns list of `MedicationResponse`.

---

## Phase 10: User Story 7 — Google Calendar Sync

**Goal**: When an appointment is scheduled or cancelled, the clinic's Google Calendar is updated automatically via the `CalendarSyncPort`. OAuth2 credentials are stored per-tenant encrypted in the database.

**Independent Test**: `GoogleCalendarAdapter.syncAppointment(...)` with a WireMock Google Calendar stub creates the event. `deleteEvent(...)` deletes it. Credentials are loaded from DB and decrypted correctly.

### Tests for US7 (MANDATORY)

- [ ] T110 [P] [US7] Write `GoogleCalendarAdapterIT`. File: `cliniq-external-adapter/src/test/java/com/cliniq/external/GoogleCalendarAdapterIT.java`. Uses WireMock for Google Calendar API. Tests: sync creates event with correct payload; delete removes event; missing credentials throws `ExternalServiceException`.

### Implementation for US7

- [ ] T111 [US7] Create Flyway migration V8. File: `cliniq-persistence/src/main/resources/db/migration/V8__add_calendar_credentials.sql`. Creates `calendar_credentials` table: `tenant_id UUID PRIMARY KEY REFERENCES tenants(id), encrypted_tokens TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`.

- [ ] T112 [US7] Create `GoogleCalendarAdapter`. File: `cliniq-external-adapter/src/main/java/com/cliniq/external/GoogleCalendarAdapter.java`. Implements `CalendarSyncPort`. Constructor injects `RestClient`, `CalendarCredentialRepository` (a new driven port — define it in `cliniq-application/src/main/java/com/cliniq/application/appointment/port/out/CalendarCredentialRepository.java` with method `Optional<String> findDecryptedTokens(TenantId tenantId)`), and an `AesEncryptionService` (from bootstrap, injected via constructor). `syncAppointment(Appointment appointment)`: load tokens, call Google Calendar Events API to create/update event. `deleteEvent(AppointmentId, TenantId)`: load tokens, call delete endpoint.

---

## Phase 11: Bootstrap, Security & ArchUnit

**Purpose**: Wire all adapters into the Spring Boot application context, configure security, and enforce architectural rules.

- [ ] T113 Create Spring Boot main class. File: `cliniq-bootstrap/src/main/java/com/cliniq/bootstrap/CliniqApplication.java`. Annotate with `@SpringBootApplication(scanBasePackages = {"com.cliniq"})`. Standard `main` method calling `SpringApplication.run(...)`.

- [ ] T114 [P] Create `application.yml` configuration. File: `cliniq-bootstrap/src/main/resources/application.yml`. Include: datasource (env var `SPRING_DATASOURCE_URL`, username, password), Flyway enabled, JPA dialect PostgreSQL, virtual threads enabled (`spring.threads.virtual.enabled: true`), `@Scheduled` enabled, Resilience4j config, Twilio credentials (env vars), OpenFDA base URL, Google OAuth2 client id/secret, AES encryption key env var.

- [ ] T115 [P] Create `TenantContext` class. File: `cliniq-web/src/main/java/com/cliniq/web/security/TenantContext.java`. Content: `public final class TenantContext { private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>(); public static void set(TenantId id) { CURRENT.set(id); } public static TenantId require() { TenantId id = CURRENT.get(); if (id == null) throw new TenantResolutionException("No tenant in context"); return id; } public static void clear() { CURRENT.remove(); } }`. Also create `TenantResolutionException.java` extends `RuntimeException` in the same package.

- [ ] T116 [P] Create `TenantContextFilter`. File: `cliniq-web/src/main/java/com/cliniq/web/security/TenantContextFilter.java`. Implements `OncePerRequestFilter`. In `doFilterInternal`: extract `tenantId` claim from the `Authorization: Bearer <jwt>` header (decode JWT without verification for now — parse the base64 payload JSON, extract `tenantId` field), call `TenantContext.set(new TenantId(UUID.fromString(tenantId)))`, then proceed with filter chain. In `finally`: call `TenantContext.clear()`.

- [ ] T117 [P] Create `GlobalExceptionHandler`. File: `cliniq-web/src/main/java/com/cliniq/web/exception/GlobalExceptionHandler.java`. Annotate with `@RestControllerAdvice`. Handle: `AppointmentNotFoundException` → 404 with `NOT_FOUND` error code. `PatientNotFoundException` → 404. `ProviderNotFoundException` → 404. `SlotUnavailableException` → 409 with `SLOT_UNAVAILABLE`. `InvalidStatusTransitionException` → 409 with `INVALID_STATUS_TRANSITION`. `MethodArgumentNotValidException` → 422 with `VALIDATION_ERROR` listing field errors. `TenantResolutionException` → 401 with `TENANT_RESOLUTION_FAILED`. Any other `Exception` → 500 with `INTERNAL_ERROR`. All responses use the error envelope from `contracts/rest-api.md`: `{ status, error, message, timestamp }`.

- [ ] T118 Create Spring `@Configuration` classes in `cliniq-bootstrap`. Files:
  - `PersistenceConfig.java`: declare `JpaPatientAdapter`, `JpaProviderAdapter`, `JpaAppointmentAdapter`, `JpaReminderAdapter`, `JpaOutboxAdapter` as `@Bean`.
  - `ApplicationConfig.java`: declare `PatientService`, `ProviderService`, `AppointmentService`, `ReminderService`, `OutboxProcessorService` as `@Bean`, injecting the ports from `PersistenceConfig`.
  - `NotificationConfig.java`: declare `TwilioNotificationAdapter` as `@Bean`.
  - `ExternalConfig.java`: declare `OpenFdaMedicationAdapter` and `GoogleCalendarAdapter` as `@Bean`.
  - `WebConfig.java`: register `TenantContextFilter` as a filter bean, configure Spring Security to permit all (auth via tenant filter for now).

- [ ] T119 Create ArchUnit architectural rule tests. File: `cliniq-bootstrap/src/test/java/com/cliniq/bootstrap/ArchRulesTest.java`. Tests:
  1. Classes in `com.cliniq.domain..` must not import from `org.springframework..` or `jakarta.persistence..`.
  2. Classes in `com.cliniq.domain..` must not import from `com.cliniq.application..`.
  3. Classes in `com.cliniq.application..` must not import from `org.springframework..` or `jakarta.persistence..`.
  4. Classes in `com.cliniq.application..` must not import from `com.cliniq.persistence..` or `com.cliniq.web..` or `com.cliniq.notification..` or `com.cliniq.external..`.
  5. No class outside `com.cliniq.web..` may import `TenantContext`.
  6. Classes named `Jpa*` must reside in `com.cliniq.persistence..`.

- [ ] T120 Create Docker Compose file. File: `cliniq-bootstrap/src/main/docker/docker-compose.yml`. Services: `postgres` using `postgres:16` image, database `cliniq`, user `cliniq`, password `cliniq`, port 5432.

- [ ] T121 Run `./mvnw verify` — all modules compile, all tests pass, ArchUnit rules are satisfied. Fix any failures.

---

## Phase 12: Multi-Tenancy Verification & Polish

- [ ] T122 Write multi-tenancy isolation test. File: `cliniq-bootstrap/src/test/java/com/cliniq/bootstrap/MultiTenancyIsolationIT.java`. Scenario: seed two tenants A and B. Register patient under tenant A. Query patients under tenant B — assert empty result. Schedule appointment under tenant A. Query appointments under tenant B — assert empty result. This test MUST pass before the system is considered complete.

- [ ] T123 [P] Configure Hibernate `@FilterDef` for all JPA entities. File: `cliniq-persistence/src/main/java/com/cliniq/persistence/PersistenceFilterConfig.java`. Define `@FilterDef(name="tenantFilter", parameters=@ParamDef(name="tenantId", type=UUIDJavaType.class))` at package level (or on each entity class). In each `JpaXxxAdapter`, activate the filter before every query: `entityManager.unwrap(Session.class).enableFilter("tenantFilter").setParameter("tenantId", tenantId.value())`.

- [ ] T124 [P] Add OpenAPI configuration. File: `cliniq-web/src/main/java/com/cliniq/web/OpenApiConfig.java`. Annotate with `@Configuration`. Define `@Bean OpenAPI customOpenAPI()` with title "Cliniq API", version "0.1.0", description from `ARCHITECTURE.md` summary, and a `SecurityScheme` for Bearer JWT auth.

- [ ] T125 [P] Add integration test for `OutboxProcessorService` with real database and mock notification. File already planned in T098. Verify: after processing, `OutboxEntry.processedAt` is not null, `Reminder.status` is `Dispatched` or `Delivered`.

- [ ] T126 Run final verification: `./mvnw verify`. Output MUST show: all 8 modules compile, all unit tests pass, all integration tests pass with Testcontainers, ArchUnit rules satisfied, multi-tenancy isolation test passes.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** (Setup): No dependencies — start immediately
- **Phase 2** (Domain): Depends on Phase 1 completion — BLOCKS all user stories
- **Phase 3** (Application): Depends on Phase 2 — BLOCKS all persistence and web work
- **Phase 4** (Patient US1): Depends on Phase 3
- **Phase 5** (Provider US2): Depends on Phase 3 (parallel with Phase 4)
- **Phase 6** (Appointment US3): Depends on Phase 4 AND Phase 5 (needs Patient and Provider data)
- **Phase 7** (Outbox/Reminders US4): Depends on Phase 6
- **Phase 8** (Twilio US5): Depends on Phase 7 (needs Reminder domain and OutboxProcessor)
- **Phase 9** (OpenFDA US6): Depends on Phase 3 only — can start in parallel with Phase 4
- **Phase 10** (Google Calendar US7): Depends on Phase 6
- **Phase 11** (Bootstrap): Depends on all previous phases
- **Phase 12** (Polish): Depends on Phase 11

### Parallel Opportunities

After Phase 3 completes, the following can run in parallel:
- Phase 4 (Patient) + Phase 5 (Provider) + Phase 9 (OpenFDA)
- After Phase 4+5: Phase 6 (Appointment)
- After Phase 6: Phase 7 + Phase 10 in parallel
- After Phase 7: Phase 8

### Within Each Phase

- All `[P]` tasks have no dependencies on other tasks in the same phase
- Tests MUST be written and verified to FAIL before implementation tasks start
- Run `./mvnw test -pl <module>` after each phase to verify

---

## Implementation Strategy

### MVP (Phases 1–4)

Complete Phases 1 through 4 to get a working patient registration API with persistent storage.

1. Phase 1: Maven scaffolding
2. Phase 2: All domain classes (pure Java, no infra)
3. Phase 3: Port interfaces and PatientService
4. Phase 4: Patient persistence + web layer
5. **STOP and VALIDATE**: `POST /patients` and `GET /patients/{id}` work end-to-end

### Incremental Delivery

- MVP (Phases 1–4) → Patient API
- Add Phase 5 → Provider sync
- Add Phase 6 → Appointment scheduling (full lifecycle)
- Add Phase 7 → Automated reminders via outbox
- Add Phase 8 → Twilio notifications
- Add Phase 9 → Medication search
- Add Phase 10 → Google Calendar sync
- Phase 11 → Production-ready bootstrap
- Phase 12 → Verified, audited release

---

## Notes

- `[P]` = parallelizable (different files, no sibling task dependency)
- `[USn]` = belongs to user story n
- Tests MUST fail before implementation — commit them separately if possible
- Domain classes MUST have zero Spring/JPA imports (ArchUnit will catch violations)
- `TenantContext` is ONLY in `cliniq-web` — never pass it to domain or application services
- `OutboxEntry` and aggregate saves MUST happen in the same database transaction — the persistence adapter is responsible for transaction management
- All `JpaXxx` classes must NEVER be imported outside `cliniq-persistence`
