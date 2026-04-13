# Research: Cliniq — Phase 0

All unknowns from the Technical Context are resolved below.

---

## Spring Boot 3.x + Java 21 Setup

**Decision**: Use Spring Boot 3.3+ with Java 21 virtual threads (Project Loom) for I/O-bound work.  
**Rationale**: Spring Boot 3.2+ supports virtual threads via `spring.threads.virtual.enabled=true`. This simplifies the thread model without changing the programming model.  
**Alternatives considered**: Reactive (WebFlux) — rejected because the team is familiar with imperative style and Testcontainers/JPA work more naturally with blocking I/O.

---

## Multi-Tenancy Strategy

**Decision**: Row-level tenancy — single schema, `tenant_id UUID NOT NULL` on every table. Hibernate `@Filter` activated per-request in persistence adapters.  
**Rationale**: Simplest to operate (one schema, one DB), sufficient for a SaaS clinic platform. Schema-per-tenant adds migration complexity with no benefit at this scale.  
**Alternatives considered**: Schema-per-tenant — rejected due to Flyway complexity and connection pool overhead.

**Key implementation detail**: `TenantContext` (ThreadLocal) lives **only** in `cliniq-web`. Background jobs (`OutboxProcessorService`) pass `tenantId` explicitly — never read from ThreadLocal in async or scheduled contexts.

---

## Outbox Pattern (Transactional Messaging)

**Decision**: Persist `OutboxEntry` rows in the same transaction as the aggregate save. A `@Scheduled` poller (`OutboxProcessorService`) reads with `SELECT FOR UPDATE SKIP LOCKED` and dispatches via `DomainEventPublisher`.  
**Rationale**: Guarantees at-least-once delivery without a message broker. `SKIP LOCKED` allows safe multi-pod processing.  
**Alternatives considered**: Kafka/RabbitMQ — rejected as overkill for MVP; can be introduced later by replacing `DomainEventPublisher` adapter.

---

## DomainEvent Hierarchy (ADR-001)

**Decision**: `DomainEvent` in shared-kernel is a non-sealed interface. Each BC defines its own sealed sub-interface (e.g., `AppointmentEvent extends DomainEvent permits AppointmentScheduled, ...`).  
**Rationale**: Sealed at BC level enables exhaustive pattern matching within a BC. `OutboxProcessorService` matches on BC groupings, not individual event types — stays maintainable as events grow.

---

## Resilience4j for Twilio/SendGrid

**Decision**: Retry (3 attempts, exponential backoff) + Circuit Breaker per channel using Resilience4j `@Retry` + `@CircuitBreaker`.  
**Rationale**: Prevents thundering-herd on transient Twilio failures; circuit breaker avoids cascading failures in outbox poll cycles.  
**Alternatives considered**: Manual retry loop — rejected due to lack of backpressure and circuit breaker semantics.

---

## OpenFDA Medication Lookup

**Decision**: `Spring RestClient` with Caffeine L1 cache (1h TTL, 1000 entries per tenant). ACL mapper converts FDA JSON → `MedicationReference` value objects.  
**Rationale**: FDA data is read-only and rarely changes; caching dramatically reduces external API calls.  
**Alternatives considered**: No cache — rejected due to rate limiting risk on free FDA API tier.

---

## Google Calendar OAuth2

**Decision**: Per-tenant OAuth2 credentials stored encrypted (AES-256) in DB. Retrieved at dispatch time by `GoogleCalendarAdapter`.  
**Rationale**: Multi-tenant — each clinic (tenant) has its own Google Calendar. Storing per-tenant tokens in DB is standard SaaS practice.  
**Alternatives considered**: Single service-account — rejected because it can't represent per-clinic calendars.

---

## ArchUnit Dependency Enforcement

**Decision**: ArchUnit tests in `cliniq-bootstrap` verify module dependency rules at build time.  
**Rationale**: Maven module boundaries alone don't prevent transitive misuse. ArchUnit rules fail the build if any class in `domain` imports Spring/JPA, or if any module imports `bootstrap`.

---

## Testing Strategy

| Layer | Tool | Scope |
|-------|------|-------|
| Domain unit tests | JUnit 5 | Aggregate invariants, status transitions, event registration |
| Application unit tests | JUnit 5 + Mockito | Service logic with mocked ports |
| Persistence integration | Testcontainers (PostgreSQL) | Repository adapters, Flyway migrations |
| Adapter integration | WireMock | Twilio, OpenFDA, Google Calendar |
| System integration | `AppointmentSchedulingIT` | Full schedule→confirm→complete flow |
| Outbox | `OutboxProcessorIT` | Atomicity + processed_at set correctly |
| Multi-tenancy | Custom | Data for tenant A invisible to tenant B |

---

## Flyway Migration Order

```
V1__create_tenants.sql
V2__create_providers.sql
V3__create_patients.sql
V4__create_appointments.sql
V5__create_prescriptions.sql
V6__create_reminders.sql
V7__create_outbox.sql
V8__add_calendar_credentials.sql
```

All tables include: `tenant_id UUID NOT NULL REFERENCES tenants(id)`, composite index on `(tenant_id, id)`, `created_at`, `updated_at`, `version` (optimistic locking).

---

All NEEDS CLARIFICATION items resolved. Proceed to Phase 1.
