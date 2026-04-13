<!-- SYNC IMPACT REPORT
Version change: (none) → 1.0.0 (initial ratification)
Added sections:
  - I. Hexagonal Architecture & Dependency Rule
  - II. Domain-Driven Design
  - III. Code Quality & Simplicity
  - IV. Test-First (NON-NEGOTIABLE)
  - V. Testing Standards
  - VI. API & UX Consistency
  - VII. Multi-Tenancy Safety
  - Quality Gates
  - Development Workflow
  - Governance
Templates reviewed:
  - .specify/templates/plan-template.md ✅ Constitution Check section aligns
  - .specify/templates/spec-template.md ✅ User stories + acceptance scenarios align with TDD principle
  - .specify/templates/tasks-template.md ✅ Tests-first task ordering aligns with Principle IV
Deferred items: None
-->

# Cliniq Constitution

## Core Principles

### I. Hexagonal Architecture & Dependency Rule (NON-NEGOTIABLE)

The dependency arrow points inward — always. Every module boundary MUST enforce this:

- `cliniq-domain` and `cliniq-shared-kernel` MUST NOT import any Spring, JPA, Hibernate, or infrastructure class.
- `cliniq-application` MUST define all port interfaces (driving + driven). It MUST NOT contain adapter implementations.
- `cliniq-persistence`, `cliniq-web`, `cliniq-notification-adapter`, `cliniq-external-adapter` MUST depend on `cliniq-application` and `cliniq-domain` — never the reverse.
- `cliniq-bootstrap` is the only wiring point. No other module may depend on it.
- ArchUnit tests in `cliniq-bootstrap` MUST enforce all of the above at build time. A build without ArchUnit checks MUST NOT be merged.

**Rationale**: Inverting infrastructure dependencies is what makes the domain independently testable, portable, and resilient to technology change. A single accidental Spring import in the domain collapses the architecture's primary guarantee.

---

### II. Domain-Driven Design

Ubiquitous language and bounded context boundaries are structural constraints, not naming preferences:

- Each Bounded Context (Appointment, Patient, Notification, Provider) MUST own its aggregates, value objects, domain events, and exceptions. Cross-BC references MUST use IDs only — never object references.
- Aggregates MUST enforce all invariants internally. Application services MUST NOT duplicate invariant logic.
- Domain events MUST be registered inside aggregate methods (e.g., `registerEvent(new AppointmentScheduled(...))`), not emitted from application services.
- Sealed BC-level event interfaces (`AppointmentEvent`, `PatientEvent`, `NotificationEvent`) MUST remain the authoritative registry for their BC's events (per ADR-001). No event may be added to a BC without being listed in that sealed interface.
- Value objects MUST be immutable records. Entities MUST use optimistic locking (`version` field).

**Rationale**: DDD discipline ensures that business rules live where they belong — in the domain — not scattered across services, controllers, or database queries.

---

### III. Code Quality & Simplicity

Complexity MUST be justified; simplicity is the default:

- Functions MUST be ≤ 50 lines. Files MUST be ≤ 800 lines. Exceptions require explicit justification in PR description.
- Deep nesting (> 3 levels) is prohibited. Use early returns and guard clauses.
- Magic numbers and strings MUST be named constants.
- Mutation is prohibited in domain and shared-kernel classes. All domain state changes MUST produce new instances or be encapsulated within aggregate methods.
- YAGNI: No speculative abstractions. A second use case justifies extraction; one use case does not.
- Dead code MUST be deleted, not commented out.

**Rationale**: Complexity is the primary enemy of long-term maintainability. Every line of unnecessary code is a future maintenance burden.

---

### IV. Test-First (NON-NEGOTIABLE)

Tests are written before implementation. This is not optional:

- RED: Write a failing test for the behavior.
- GREEN: Write the minimum implementation to pass.
- REFACTOR: Clean up without breaking the test.
- No implementation code may be committed without a corresponding test that was written first. PR reviewers MUST verify test commit precedes implementation commit (or is in same commit with test failing snapshot in description).

**Rationale**: Test-first is the only reliable way to ensure tests verify behavior rather than implementation. It also forces clear interface design before code is written.

---

### V. Testing Standards

Coverage is a floor, not a goal:

- Minimum 80% line coverage across all modules. Domain modules MUST target 95%+.
- Domain unit tests MUST cover all aggregate invariants, all state transitions (valid and invalid paths), and all domain event registrations.
- Persistence adapters MUST use Testcontainers (real PostgreSQL). No in-memory database substitutes.
- External adapter tests MUST use WireMock. No live external calls in CI.
- Multi-tenancy MUST be verified by a dedicated test: data seeded under tenant A MUST NOT appear in queries for tenant B.
- `OutboxProcessorIT` MUST verify atomic processing: `processed_at` is set if and only if event dispatch succeeded.
- `AppointmentSchedulingIT` MUST cover the full flow: schedule → confirm → complete via application service against a real DB.

**Rationale**: Integration tests catch what unit tests cannot — real SQL behavior, Flyway migration correctness, and multi-tenant data isolation.

---

### VI. API & UX Consistency

Every HTTP surface MUST follow a uniform contract:

- All responses MUST use the standard error envelope: `{ status, error, message, timestamp }`.
- HTTP status codes MUST be semantically correct: `201` for creation, `204` for state mutations with no body, `409` for business rule conflicts, `422` for validation errors, `404` for not-found.
- All mutation endpoints MUST be idempotent where possible, or explicitly documented as non-idempotent.
- OpenAPI documentation MUST be generated from code (not hand-written) and MUST be accurate. Stale OpenAPI specs MUST NOT be merged.
- Command records (e.g., `ScheduleAppointmentCommand`) MUST carry all inputs. Driving ports MUST never accept raw primitives.
- REST path naming: plural nouns (`/appointments`, `/patients`), sub-resources for nested operations (`/appointments/{id}/prescriptions`).

**Rationale**: API consistency reduces client integration friction and prevents silent bugs caused by inconsistent error handling.

---

### VII. Multi-Tenancy Safety

Tenant data isolation is a security invariant:

- `TenantContext` (ThreadLocal) MUST reside exclusively in `cliniq-web`. Domain, application, and persistence layers MUST receive `TenantId` as an explicit parameter.
- Background jobs (`OutboxProcessorService`) MUST extract `tenantId` from the `OutboxEntry` record — never from `TenantContext`.
- Hibernate `@Filter(name = "tenantFilter")` MUST be activated on every JPA entity. ArchUnit MUST verify no entity is missing the filter annotation.
- All queries in repository adapters MUST include `tenant_id` in the `WHERE` clause. No query may return data for multiple tenants unless explicitly designed for cross-tenant admin use (which MUST require a separate role and explicit opt-in).
- The multi-tenancy isolation test (Principle V) MUST run on every CI build.

**Rationale**: Tenant data leakage is a critical security failure. Defense in depth — explicit parameters + Hibernate filters + ArchUnit checks — ensures no single point of failure.

---

## Quality Gates

These gates MUST pass before any PR is merged:

| Gate | How Enforced |
|------|-------------|
| ArchUnit dependency rules | `./mvnw verify` (build fails on violation) |
| Domain zero-framework imports | ArchUnit |
| 80%+ line coverage (95%+ domain) | JaCoCo threshold in Maven build |
| No raw SQL without tenant_id | Code review + ArchUnit custom rule |
| OpenAPI spec up to date | CI diff check on generated spec |
| Multi-tenancy isolation test passes | CI (`./mvnw verify`) |
| All tests green on real PostgreSQL | Testcontainers in CI |

---

## Development Workflow

1. **Spec first**: Feature must have a spec and plan before implementation begins.
2. **Test first**: Write failing tests before any implementation (Principle IV).
3. **Module order**: Follow the Implementation Order in `ARCHITECTURE.md` (shared-kernel → domain → application → adapters → bootstrap).
4. **One module at a time**: Do not skip ahead. Domain must be stable before application ports are defined.
5. **Collaborative implementation**: Design decisions (data structures, invariants, error strategies) MUST be discussed with the team/user before code is written. No unilateral bulk code generation.
6. **Commit granularity**: One logical unit per commit. Test commit before implementation commit (or combined with failing snapshot documented).
7. **PR review**: Every PR MUST be reviewed against this constitution before merge.

---

## Governance

- This constitution supersedes all other development practices and preferences.
- Amendments require: (a) written rationale, (b) version bump per semantic rules, (c) propagation to all dependent templates.
- MAJOR bump: principle removal, redefinition, or backward-incompatible governance change.
- MINOR bump: new principle or materially expanded guidance.
- PATCH bump: clarification, wording, non-semantic refinements.
- All PRs and code reviews MUST explicitly verify compliance with this constitution.
- Complexity violations (e.g., oversized files, missing ArchUnit rules) MUST be justified in the PR description or rejected.
- Runtime guidance for Claude Code: `CLAUDE.md` at repository root.

**Version**: 1.0.0 | **Ratified**: 2026-04-11 | **Last Amended**: 2026-04-11
