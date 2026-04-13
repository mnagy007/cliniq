# Data Model: Cliniq

## Bounded Contexts & Aggregates

### Appointment BC (`cliniq-domain/appointment`)

#### `Appointment` (Aggregate Root)
| Field | Type | Notes |
|-------|------|-------|
| id | `AppointmentId` (UUID) | |
| tenantId | `TenantId` | |
| patientId | `PatientId` | |
| providerId | `ProviderId` | |
| timeSlot | `TimeSlot` | record: date, startTime, endTime |
| type | `AppointmentType` | enum |
| status | `AppointmentStatus` | sealed: Scheduled→Confirmed→Completed/NoShow; Cancelled from Scheduled/Confirmed |
| cancellationReason | `CancellationReason?` | nullable; set on cancel |
| prescriptions | `List<Prescription>` | child entities |
| version | `long` | optimistic locking |

**State Transitions:**
```
Scheduled → Confirmed → Completed
                      → NoShow
Scheduled → Cancelled
Confirmed → Cancelled
```

**Invariants:**
- `schedule()`: TimeSlot must be within Provider availability; fires `AppointmentScheduled`
- `addPrescription()`: only when status ∈ {Confirmed, Completed}

#### `Prescription` (Entity, child of Appointment)
| Field | Type | Notes |
|-------|------|-------|
| id | `PrescriptionId` (UUID) | |
| medication | `MedicationReference` | value object |
| dosage | `String` | |
| instructions | `String` | |

#### `TimeSlot` (Value Object)
| Field | Type |
|-------|------|
| date | `LocalDate` |
| startTime | `LocalTime` |
| endTime | `LocalTime` |

#### `MedicationReference` (Value Object)
| Field | Type |
|-------|------|
| ndcCode | `String` |
| brandName | `String` |
| genericName | `String` |

#### Events
- `AppointmentScheduled` — on `schedule()`
- `AppointmentConfirmed` — on `confirm()`
- `AppointmentCancelled` — on `cancel(reason)`
- `AppointmentCompleted` — on `complete()`
- `AppointmentMarkedNoShow` — on `markNoShow()`

All extend `AppointmentEvent extends DomainEvent` (sealed interface permitting only the above five).

---

### Provider BC (`cliniq-domain/provider`)

#### `Provider` (Aggregate Root — reference data, externally owned)
| Field | Type | Notes |
|-------|------|-------|
| id | `ProviderId` (UUID) | |
| tenantId | `TenantId` | |
| name | `ProviderName` | record: givenName, familyName |
| specialty | `Specialty` | enum |
| availabilitySlots | `List<AvailabilitySlot>` | |

#### `AvailabilitySlot` (Value Object)
| Field | Type |
|-------|------|
| dayOfWeek | `DayOfWeek` |
| startTime | `LocalTime` |
| endTime | `LocalTime` |

---

### Patient BC (`cliniq-domain/patient`)

#### `Patient` (Aggregate Root)
| Field | Type | Notes |
|-------|------|-------|
| id | `PatientId` (UUID) | |
| tenantId | `TenantId` | |
| personalInfo | `PersonalInfo` | record: givenName, familyName, dob, gender |
| contactInfo | `ContactInfo` | record: PhoneNumber, EmailAddress |
| medicalRecordNumber | `MedicalRecordNumber` | value object |
| notificationPreference | `NotificationPreference` | record: preferredChannel, optedOut |

#### Events
- `PatientRegistered`
- `ContactInfoUpdated`
- `NotificationPreferenceChanged`

All extend `PatientEvent extends DomainEvent` (sealed).

---

### Notification BC (`cliniq-domain/notification`)

#### `Reminder` (Aggregate Root)
| Field | Type | Notes |
|-------|------|-------|
| id | `ReminderId` (UUID) | |
| tenantId | `TenantId` | |
| appointmentId | `AppointmentId` | |
| patientId | `PatientId` | |
| channel | `Channel` | sealed: Sms | Email |
| status | `ReminderStatus` | sealed: Pending→Dispatched→Delivered/Failed |
| failureReason | `FailureReason?` | nullable |

**State Transitions:**
```
Pending → Dispatched → Delivered
                     → Failed
```

#### `OutboxEntry` (Aggregate Root)
| Field | Type | Notes |
|-------|------|-------|
| id | `OutboxEntryId` (UUID) | |
| tenantId | `TenantId` | |
| aggregateType | `String` | e.g., "Appointment" |
| aggregateId | `String` | |
| eventType | `String` | FQCN of event |
| payload | `String` | JSON |
| occurredAt | `Instant` | |
| processedAt | `Instant?` | null until processed |

---

## PostgreSQL Schema (aligned with Flyway order)

### `tenants`
```sql
id UUID PRIMARY KEY,
name VARCHAR NOT NULL,
created_at TIMESTAMPTZ NOT NULL
```

### `providers`
```sql
id UUID,
tenant_id UUID NOT NULL REFERENCES tenants(id),
given_name VARCHAR NOT NULL,
family_name VARCHAR NOT NULL,
specialty VARCHAR NOT NULL,
created_at, updated_at, version
PRIMARY KEY (id),
INDEX (tenant_id, id)
```

### `patients`
```sql
id UUID,
tenant_id UUID NOT NULL REFERENCES tenants(id),
given_name, family_name, dob DATE, gender VARCHAR,
phone_number VARCHAR, email_address VARCHAR,
medical_record_number VARCHAR,
preferred_channel VARCHAR, opted_out BOOLEAN,
created_at, updated_at, version
INDEX (tenant_id, id)
```

### `appointments`
```sql
id UUID,
tenant_id UUID NOT NULL REFERENCES tenants(id),
patient_id UUID NOT NULL,
provider_id UUID NOT NULL,
slot_date DATE, slot_start TIME, slot_end TIME,
type VARCHAR, status VARCHAR,
cancellation_code VARCHAR, cancellation_description VARCHAR,
created_at, updated_at, version
INDEX (tenant_id, id), INDEX (tenant_id, patient_id), INDEX (tenant_id, provider_id)
```

### `prescriptions`
```sql
id UUID,
appointment_id UUID NOT NULL REFERENCES appointments(id),
tenant_id UUID NOT NULL REFERENCES tenants(id),
ndc_code VARCHAR, brand_name VARCHAR, generic_name VARCHAR,
dosage VARCHAR, instructions VARCHAR
```

### `reminders`
```sql
id UUID,
tenant_id UUID NOT NULL REFERENCES tenants(id),
appointment_id UUID NOT NULL,
patient_id UUID NOT NULL,
channel VARCHAR, status VARCHAR,
failure_code VARCHAR, failure_message VARCHAR,
created_at, updated_at, version
INDEX (tenant_id, id)
```

### `outbox_entries`
```sql
id UUID,
tenant_id UUID NOT NULL REFERENCES tenants(id),
aggregate_type VARCHAR, aggregate_id VARCHAR,
event_type VARCHAR,
payload JSONB,
occurred_at TIMESTAMPTZ NOT NULL,
processed_at TIMESTAMPTZ NULL
INDEX (tenant_id, processed_at) WHERE processed_at IS NULL
```

### `calendar_credentials`
```sql
tenant_id UUID PRIMARY KEY REFERENCES tenants(id),
encrypted_tokens TEXT NOT NULL,
created_at, updated_at
```
