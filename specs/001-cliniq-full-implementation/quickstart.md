# Quickstart: Cliniq Development

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker + Docker Compose (for PostgreSQL)

## Running Locally

```bash
# 1. Start PostgreSQL
docker compose -f cliniq-bootstrap/src/main/docker/docker-compose.yml up -d

# 2. Build all modules (skips tests for speed)
./mvnw clean install -DskipTests

# 3. Run the application
./mvnw spring-boot:run -pl cliniq-bootstrap

# 4. API available at
curl http://localhost:8080/api/v1/patients
```

## Running Tests

```bash
# All tests (requires Docker for Testcontainers)
./mvnw verify

# Single module
./mvnw test -pl cliniq-domain

# Integration tests only
./mvnw verify -pl cliniq-persistence -Pintegration
```

## Module Development Order

Follow this order strictly — each module depends on the previous:

| Step | Module | Key Task |
|------|--------|----------|
| 1 | `cliniq-shared-kernel` | TenantId, AggregateRoot, DomainEvent, DomainException, Preconditions ✓ (started) |
| 2 | `cliniq-domain` | Patient BC — aggregate, value objects, events, exceptions |
| 3 | `cliniq-domain` | Appointment BC — Appointment, Provider, Prescription |
| 4 | `cliniq-domain` | Notification BC — Reminder, OutboxEntry, Channel |
| 5 | `cliniq-application` | All port interfaces + command/query records |
| 6 | `cliniq-application` | PatientService, AppointmentService, ReminderService, OutboxProcessorService |
| 7 | `cliniq-persistence` | Flyway migrations, JPA entities, repository adapters |
| 8 | `cliniq-web` | REST controllers, DTOs, exception handler, OpenAPI |
| 9 | `cliniq-notification-adapter` | Twilio adapter + Resilience4j |
| 10 | `cliniq-external-adapter` | OpenFDA adapter (cached), Google Calendar adapter |
| 11 | `cliniq-bootstrap` | Spring Boot wiring, Docker Compose, ArchUnit tests |

## Environment Variables

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cliniq
SPRING_DATASOURCE_USERNAME=cliniq
SPRING_DATASOURCE_PASSWORD=cliniq
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_FROM_NUMBER=+1...
SENDGRID_API_KEY=...
GOOGLE_OAUTH2_CLIENT_ID=...
GOOGLE_OAUTH2_CLIENT_SECRET=...
CALENDAR_CREDENTIALS_ENCRYPTION_KEY=...  # 32-byte AES-256 key, base64
```

## Key Architectural Notes

- `TenantContext` (ThreadLocal) lives **only** in `cliniq-web`. Never use it in domain or scheduled tasks.
- `OutboxProcessorService` passes `tenantId` explicitly — extracted from `OutboxEntry`, not ThreadLocal.
- Domain classes must be pure Java 21 — zero Spring/JPA imports. ArchUnit enforces this.
- All repository port methods take `TenantId` as an explicit parameter.
- JPA entities use `Jpa` prefix (e.g., `JpaAppointment`) and never leak outside `cliniq-persistence`.
