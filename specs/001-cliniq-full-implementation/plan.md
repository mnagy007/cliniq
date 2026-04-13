# Implementation Plan: Cliniq — Patient Appointment & Reminder System

**Branch**: `001-cliniq-full-implementation` | **Date**: 2026-04-11 | **Spec**: ARCHITECTURE.md  
**Input**: Architectural specification from `/ARCHITECTURE.md`

## Summary

Greenfield multi-tenant clinic scheduling backend. Hexagonal (Ports & Adapters) architecture with DDD. Supports appointment booking, provider availability, patient management, medication references (OpenFDA), SMS/email reminders (Twilio), and Google Calendar sync. Implementation proceeds one Maven module at a time, shared-kernel → domain → application → adapters → bootstrap.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, Flyway, Resilience4j, Twilio SDK, Google Calendar API client, Caffeine, ArchUnit  
**Storage**: PostgreSQL (row-level multi-tenancy — single schema, `tenant_id` on every table)  
**Testing**: JUnit 5, Testcontainers, WireMock, ArchUnit  
**Target Platform**: Linux server (Docker Compose for local dev)  
**Project Type**: web-service (REST API, Maven multi-module)  
**Performance Goals**: N/A for MVP; outbox poller every 5s  
**Constraints**: `SELECT FOR UPDATE SKIP LOCKED` for safe concurrent outbox processing; AES-256 for stored OAuth2 tokens  
**Scale/Scope**: Multi-tenant SaaS; designed for horizontal scaling at bootstrap layer

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| Module dependency rules enforced | PASS | ArchUnit tests planned in bootstrap |
| No domain class imports framework code | PASS | Pure Java 21 domain module |
| TenantId explicit on all repository methods | PASS | Architecture mandates this |
| DomainEvent hierarchy strategy (ADR-001) | PASS | Non-sealed base, BC-level sealed sub-interfaces |
| TDD mandatory | PASS | Per project rules |
| 80%+ test coverage | REQUIRED | Unit + integration + E2E required |

No violations. Proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/001-cliniq-full-implementation/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
cliniq/                              ← parent pom (dependency management only)
├── cliniq-shared-kernel/            ← TenantId, AggregateRoot, DomainEvent, DomainException, Preconditions
├── cliniq-domain/                   ← Pure Java 21 domain: appointment, patient, notification, provider BCs
├── cliniq-application/              ← Use case interfaces (driving ports), driven port interfaces, command records, services
├── cliniq-persistence/              ← JPA entities (Jpa* prefix), mappers, Flyway migrations, repository adapters
├── cliniq-web/                      ← REST controllers, DTOs, GlobalExceptionHandler, OpenAPI config, TenantContext
├── cliniq-notification-adapter/     ← TwilioNotificationAdapter + Resilience4j retry/circuit-breaker
├── cliniq-external-adapter/         ← OpenFdaMedicationAdapter (Caffeine cache) + GoogleCalendarAdapter (OAuth2)
└── cliniq-bootstrap/                ← Spring Boot main, Docker Compose, ArchUnit wiring tests
```

**Structure Decision**: Maven multi-module as defined in ARCHITECTURE.md. Each module has a clear dependency direction enforced by ArchUnit. No module may depend on `cliniq-bootstrap`.
