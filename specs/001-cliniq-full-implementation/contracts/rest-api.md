# REST API Contracts

Base URL: `/api/v1`  
Auth: Bearer JWT (contains `tenantId` claim, resolved by `TenantContext` filter)  
Content-Type: `application/json`

---

## Appointments

### POST `/appointments`
Schedule a new appointment.

**Request**
```json
{
  "patientId": "uuid",
  "providerId": "uuid",
  "timeSlot": {
    "date": "2026-05-10",
    "startTime": "09:00",
    "endTime": "09:30"
  },
  "type": "GENERAL | FOLLOW_UP | SPECIALIST"
}
```
**Response** `201 Created`
```json
{ "appointmentId": "uuid" }
```
**Errors**: `409 Conflict` (slot unavailable), `422 Unprocessable Entity` (validation)

---

### PATCH `/appointments/{id}/confirm`
Confirm a scheduled appointment.  
**Response** `204 No Content`  
**Errors**: `409` (invalid status transition)

---

### PATCH `/appointments/{id}/cancel`
Cancel an appointment.

**Request**
```json
{ "code": "PATIENT_REQUEST", "description": "Patient called to cancel" }
```
**Response** `204 No Content`

---

### PATCH `/appointments/{id}/complete`
Mark appointment as completed.  
**Response** `204 No Content`

---

### PATCH `/appointments/{id}/no-show`
Mark appointment as no-show.  
**Response** `204 No Content`

---

### POST `/appointments/{id}/prescriptions`
Add a prescription (only when Confirmed or Completed).

**Request**
```json
{
  "ndcCode": "0069-0105-03",
  "brandName": "Lipitor",
  "genericName": "atorvastatin",
  "dosage": "10mg once daily",
  "instructions": "Take with water"
}
```
**Response** `201 Created`
```json
{ "prescriptionId": "uuid" }
```

---

### GET `/appointments/{id}`
Retrieve appointment details.  
**Response** `200 OK` — full appointment with prescriptions.

---

### GET `/appointments?patientId=&providerId=&date=`
Query appointments for the current tenant.

---

## Patients

### POST `/patients`
Register a new patient.

**Request**
```json
{
  "givenName": "Jane",
  "familyName": "Doe",
  "dateOfBirth": "1990-03-15",
  "gender": "FEMALE",
  "phoneNumber": "+15550001234",
  "emailAddress": "jane@example.com",
  "notificationPreference": {
    "preferredChannel": "SMS | EMAIL",
    "optedOut": false
  }
}
```
**Response** `201 Created`
```json
{ "patientId": "uuid" }
```

---

### PATCH `/patients/{id}/contact`
Update contact information.

---

### PATCH `/patients/{id}/notification-preference`
Update notification preference.

---

### GET `/patients/{id}`

---

## Providers

### POST `/providers/sync`
Sync provider from EHR/HR system (external trigger).

**Request**
```json
{
  "externalId": "provider-ext-001",
  "givenName": "Dr. Alice",
  "familyName": "Smith",
  "specialty": "CARDIOLOGY",
  "availabilitySlots": [
    { "dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "17:00" }
  ]
}
```
**Response** `200 OK`

---

### GET `/providers/{id}`

---

## Reminders

### POST `/reminders`
Schedule a reminder manually (normally auto-scheduled via `AppointmentScheduled` event).

---

### GET `/reminders?appointmentId=`

---

## Medication Lookup

### GET `/medications?q={query}`
Search medications via OpenFDA (proxied + cached).

**Response** `200 OK`
```json
{
  "results": [
    { "ndcCode": "...", "brandName": "...", "genericName": "..." }
  ]
}
```

---

## Error Envelope

All errors return:
```json
{
  "status": 422,
  "error": "VALIDATION_ERROR",
  "message": "Human-readable description",
  "timestamp": "2026-04-11T10:00:00Z"
}
```

Standard codes: `VALIDATION_ERROR`, `NOT_FOUND`, `SLOT_UNAVAILABLE`, `INVALID_STATUS_TRANSITION`, `TENANT_RESOLUTION_FAILED`, `INTERNAL_ERROR`
