---
description: "Task list for Azure deployment of cliniq (derived from specs/azure-deployment/plan.md)"
---

# Tasks: Azure Deployment — cliniq

**Input**: `specs/azure-deployment/plan.md` (spec.md absent — user stories derived from plan Deliverables A–E)

**Tests**: This feature is pure infrastructure/configuration (Dockerfile, Bicep, GitHub Actions, Spring profiles). Constitution Principle IV applies to domain/application/adapter code, not deployment artifacts. Verification is performed via live deployment smoke tests (see each story's **Independent Test**).

**Organization**: Tasks are grouped by user story so that containerization (US1) → dev infra (US2) → CI/CD (US3) → SPA wiring (US4) → prod cutover (US5) can each ship as an independent increment.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1–US5
- All file paths are absolute-from-repo-root (repo root = `/Users/mohamednagy/projects/cliniq`)

## Path Conventions

Repository structure already established:

- Spring Boot modules: `cliniq-bootstrap/`, `cliniq-web/`, `cliniq-application/`, `cliniq-domain/`, `cliniq-persistence/`, adapters
- New IaC root: `infra/bicep/`
- CI/CD: `.github/workflows/`
- Feature docs: `specs/azure-deployment/`

---

## Phase 1: Setup (Shared Scaffolding)

**Purpose**: Create directories and ignore files required by all downstream phases.

- [ ] T001 Create directory tree `infra/bicep/modules/` and `infra/bicep/params/`
- [ ] T002 [P] Add `.dockerignore` at repo root excluding `target/`, `.git/`, `specs/`, `.claude/`, `**/node_modules/`, `**/*.log`
- [ ] T003 [P] Verify `cliniq-bootstrap/src/main/docker/` exists (create if missing) as home for the Dockerfile

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Spring-side configuration changes every story depends on. No infra change yet.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T004 Update `cliniq-bootstrap/src/main/resources/application.yml` — add `management.endpoints.web.exposure.include: health,info,prometheus`, `management.endpoint.health.probes.enabled: true`, and liveness/readiness group mappings
- [ ] T005 Create `cliniq-bootstrap/src/main/resources/application-azure.yml` with:
  - `spring.datasource.url/username/password` bound to `SPRING_DATASOURCE_*` env vars
  - `cliniq.outbox.poll-delay-ms: ${CLINIQ_OUTBOX_POLL_DELAY_MS:5000}`
  - `cliniq.outbox.batch-size: ${CLINIQ_OUTBOX_BATCH_SIZE:50}`
  - `logging.level.root: ${LOG_LEVEL:INFO}`
- [ ] T006 [P] Add "Single-replica deployment constraint" section in `ARCHITECTURE.md` explaining `OutboxScheduler` safety requirement (`min=max=1`) until ShedLock is introduced
- [ ] T007 Build reactor locally to confirm profile loads: `mvn -pl cliniq-bootstrap -am -DskipITs -Dspring-boot.run.profiles=azure verify`

**Checkpoint**: Spring Boot app exposes actuator probes and recognizes `azure` profile. Ready to containerize.

---

## Phase 3: User Story 1 — Containerize cliniq backend (Priority: P1) 🎯 MVP

**Goal**: Package the Spring Boot bootstrap jar as an OCI image that runs locally and reports healthy actuator probes.

**Independent Test**: Build the image, run it with `SPRING_PROFILES_ACTIVE=dev,azure` against a local Postgres, and receive `{"status":"UP"}` from `GET /actuator/health/liveness`.

### Implementation for User Story 1

- [ ] T008 [US1] Write `cliniq-bootstrap/src/main/docker/Dockerfile` — multi-stage: stage 1 uses `maven:3.9-eclipse-temurin-21` to `mvn -pl cliniq-bootstrap -am -DskipTests package`; stage 2 uses `eclipse-temurin:21-jre`, non-root user, copies `cliniq-bootstrap/target/cliniq-bootstrap-*.jar` to `/app/app.jar`, `EXPOSE 8080`, `ENTRYPOINT ["java","-jar","/app/app.jar"]`
- [ ] T009 [US1] Build image locally: `docker build -f cliniq-bootstrap/src/main/docker/Dockerfile -t cliniq:local .` from repo root
- [ ] T010 [US1] Start local Postgres via `docker-compose up -d postgres` then run container: `docker run --rm -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev,azure --env SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/cliniq --env SPRING_DATASOURCE_USERNAME=cliniq --env SPRING_DATASOURCE_PASSWORD=cliniq cliniq:local`
- [ ] T011 [US1] Verify `curl -s localhost:8080/actuator/health/liveness` returns `{"status":"UP"}` and `/actuator/health/readiness` likewise

**Checkpoint**: Image builds, runs, connects to Postgres, passes liveness/readiness. MVP container is shippable.

---

## Phase 4: User Story 2 — Provision Azure dev environment via Bicep (Priority: P1)

**Goal**: Stand up ACR, Log Analytics, Application Insights, Key Vault, PostgreSQL Flexible Server, Container Apps Environment, and a single-replica Container App in `rg-cliniq-dev`.

**Independent Test**: `az deployment group create` succeeds; `curl https://<ca-cliniq-dev-fqdn>/actuator/health` returns UP once the image from US1 is pushed and set.

### Implementation for User Story 2

- [ ] T012 [P] [US2] Write `infra/bicep/modules/acr.bicep` — Basic SKU, admin disabled, managed identity pull role for downstream consumers
- [ ] T013 [P] [US2] Write `infra/bicep/modules/log-analytics.bicep` — workspace scoped to the env resource group
- [ ] T014 [P] [US2] Write `infra/bicep/modules/app-insights.bicep` — workspace-based, linked to Log Analytics from T013
- [ ] T015 [P] [US2] Write `infra/bicep/modules/key-vault.bicep` — RBAC mode, soft-delete enabled, 7-day retention, outputs the vault URI
- [ ] T016 [P] [US2] Write `infra/bicep/modules/postgres.bicep` — Flexible Server (`Standard_B1ms` dev / param-driven SKU), PG 16, 32 GB (dev) disk, private access via VNet delegated subnet, Flyway-compatible admin user
- [ ] T017 [P] [US2] Write `infra/bicep/modules/container-apps-env.bicep` — VNet-integrated env wired to Log Analytics (T013) and App Insights (T014)
- [ ] T018 [US2] Write `infra/bicep/modules/container-app.bicep` — `minReplicas: 1`, `maxReplicas: 1`, system-assigned managed identity, ACR pull role, Key Vault `Secrets User` role, `secretRef` bindings for `db-password`/`twilio-auth-token`/`sendgrid-api-key`/`google-calendar-client-secret`/`cliniq-encryption-key`/`twilio-account-sid`, plain env vars for `CLINIQ_OUTBOX_POLL_DELAY_MS`/`CLINIQ_OUTBOX_BATCH_SIZE`/`LOG_LEVEL`, `TWILIO_PHONE_NUMBER`, `SENDGRID_FROM_EMAIL`, `GOOGLE_CALENDAR_BASE_URL`, `OPENFDA_BASE_URL`, liveness/readiness HTTP probes on `/actuator/health/liveness` and `/actuator/health/readiness`
- [ ] T019 [US2] Write `infra/bicep/main.bicep` — subscription- or RG-scoped entrypoint that wires modules T012–T018 together and accepts `env` (`dev`/`prod`), `location`, `outboxPollDelayMs`, `outboxBatchSize`, `logLevel`, PG SKU and storage parameters
- [ ] T020 [P] [US2] Write `infra/bicep/params/dev.parameters.json` — `env=dev`, `outboxPollDelayMs=5000`, `outboxBatchSize=50`, `logLevel=DEBUG`, PG `Standard_B1ms`/32 GB/7-day PITR
- [ ] T021 [US2] Create resource group and deploy: `az group create -n rg-cliniq-dev -l <region>`; `az deployment group create -g rg-cliniq-dev -f infra/bicep/main.bicep -p @infra/bicep/params/dev.parameters.json`
- [ ] T022 [US2] Populate dev Key Vault secrets (`db-password`, `twilio-auth-token`, `twilio-account-sid`, `sendgrid-api-key`, `google-calendar-client-secret`, `cliniq-encryption-key`) via `az keyvault secret set`
- [ ] T023 [US2] Push the US1 image to ACR and update the Container App: `az acr login -n <acr>`; `docker tag cliniq:local <acr>.azurecr.io/cliniq:bootstrap`; `docker push`; `az containerapp update -g rg-cliniq-dev -n ca-cliniq-dev --image <acr>.azurecr.io/cliniq:bootstrap`
- [ ] T024 [US2] Smoke test: `curl https://<ca-cliniq-dev-fqdn>/actuator/health` → UP; insert a row into `outbox_entries` via `psql` against the dev PG and confirm it flips to `SENT` within ~5s in Application Insights logs; confirm `flyway_schema_history` is populated

**Checkpoint**: Dev environment is live, health-checked, and processing outbox entries. Can be demoed end-to-end.

---

## Phase 5: User Story 3 — CI/CD pipeline (Priority: P2)

**Goal**: Automate image build/push/revision-update on branch pushes and gate infra changes behind a manual workflow.

**Independent Test**: Pushing a code-only commit to `dev` triggers `deploy.yml`, rolls a new Container Apps revision tagged with the commit SHA, and the new revision becomes primary with health probes passing.

### Implementation for User Story 3

- [ ] T025 [US3] Register a GitHub OIDC federated credential on an Azure AD app with `Contributor` on `rg-cliniq-dev` (and later `rg-cliniq-prod`) and `AcrPush` on the shared ACR — store `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` as GitHub repo variables
- [ ] T026 [P] [US3] Create `.github/workflows/deploy.yml` — triggered on push to `dev` (→ dev env) and `main` (→ prod env) with steps: `actions/checkout@v4`, `actions/setup-java@v4` (Temurin 21 + Maven cache), `mvn -pl cliniq-bootstrap -am -DskipITs verify`, `azure/login@v2` (OIDC), `az acr login`, `docker build -f cliniq-bootstrap/src/main/docker/Dockerfile -t <acr>.azurecr.io/cliniq:${{ github.sha }} .`, `docker push`, `az containerapp update --name ca-cliniq-<env> --resource-group rg-cliniq-<env> --image <acr>.azurecr.io/cliniq:${{ github.sha }}`
- [ ] T027 [P] [US3] Create `.github/workflows/infra.yml` — `workflow_dispatch` only, inputs: `env` (dev/prod), runs `az deployment group create` against the matching params file
- [ ] T028 [US3] Push a trivial commit to `dev` and observe the workflow succeed; confirm new revision name in `az containerapp revision list -g rg-cliniq-dev -n ca-cliniq-dev`
- [ ] T029 [US3] Confirm new revision traffic is 100% and previous revision is retained for rollback

**Checkpoint**: Any merge to `dev` produces a healthy revision without manual intervention.

---

## Phase 6: User Story 4 — React SPA hosting and CORS (Priority: P2)

**Goal**: Provision Azure Static Web Apps for the React frontend and allow it to call `cliniq-web` endpoints.

**Independent Test**: The SPA loaded from the Static Web App FQDN can call `GET /api/providers` on the Container App without CORS errors in the browser console.

### Implementation for User Story 4

- [ ] T030 [P] [US4] Write `infra/bicep/modules/static-web-app.bicep` — Free tier for dev, Standard for prod, outputs default host and API token
- [ ] T031 [US4] Wire `static-web-app.bicep` into `infra/bicep/main.bicep` and redeploy dev
- [ ] T032 [US4] Locate existing CORS configuration in `cliniq-web` (search for `WebMvcConfigurer` / `CorsConfigurationSource` under `cliniq-web/src/main/java`); add the Static Web App origin as an allowed origin driven by `cliniq.cors.allowed-origins` env var
- [ ] T033 [US4] Add `CLINIQ_CORS_ALLOWED_ORIGINS` env var to the Container App (via `container-app.bicep` or `az containerapp update --set-env-vars`) with the SPA FQDN
- [ ] T034 [US4] Trigger the SPA's own Static Web Apps GitHub Actions workflow and validate a real SPA → API call from the browser

**Checkpoint**: SPA can authenticate against and call the dev backend.

---

## Phase 7: User Story 5 — Prod environment promotion (Priority: P3)

**Goal**: Mirror dev into a prod environment with larger SKUs, production secrets, optional custom domain, and a rehearsed rollback path.

**Independent Test**: `curl https://<prod-fqdn>/actuator/health` returns UP; availability test in Application Insights shows green for 30 minutes; rolling back via `az containerapp ingress traffic set` restores prior revision within one minute.

### Implementation for User Story 5

- [ ] T035 [US5] Create `infra/bicep/params/prod.parameters.json` — `env=prod`, `outboxPollDelayMs=2000`, `outboxBatchSize=100`, `logLevel=INFO`, PG `Standard_D2ds_v5`/128 GB/30-day PITR
- [ ] T036 [US5] `az group create -n rg-cliniq-prod -l <region>`; `az deployment group create -g rg-cliniq-prod -f infra/bicep/main.bicep -p @infra/bicep/params/prod.parameters.json`
- [ ] T037 [US5] Populate prod Key Vault secrets (same names as dev, production values) via `az keyvault secret set`
- [ ] T038 [US5] Configure custom domain and managed certificate on the prod Container App via `az containerapp hostname add` + `az containerapp ssl upload`
- [ ] T039 [US5] Enable Application Insights availability test against `https://<prod-fqdn>/actuator/health` with 5-minute cadence from 3 regions
- [ ] T040 [US5] Document rollback runbook in `specs/azure-deployment/runbook.md` covering `az containerapp revision list` and `az containerapp ingress traffic set --revision-weight <old>=100 <new>=0`
- [ ] T041 [US5] Rehearse rollback on prod against a no-op revision to validate the runbook

**Checkpoint**: Prod is live, monitored, and rollback-capable.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [ ] T042 [P] Add "Deployment" section to repository `README.md` linking `specs/azure-deployment/plan.md`, `specs/azure-deployment/tasks.md`, and the runbook
- [ ] T043 [P] Document per-env outbox tuning knobs (`CLINIQ_OUTBOX_POLL_DELAY_MS`, `CLINIQ_OUTBOX_BATCH_SIZE`, `LOG_LEVEL`) in `ARCHITECTURE.md`
- [ ] T044 Run the full Verification section of `specs/azure-deployment/plan.md` against dev and prod; record results in `specs/azure-deployment/verification-log.md`
- [ ] T045 Review Open Items section of `plan.md` and close each one (region confirmed, OIDC IDs recorded, domain finalized, CORS file path captured)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies
- **Foundational (Phase 2)**: depends on Phase 1; blocks all user stories
- **US1 (Phase 3)**: depends on Phase 2; blocks US2's image push (T023)
- **US2 (Phase 4)**: depends on Phase 2; T023 additionally depends on US1
- **US3 (Phase 5)**: depends on US2 (needs resource names and ACR in place)
- **US4 (Phase 6)**: depends on US2 (reuses `main.bicep` wiring and Container App)
- **US5 (Phase 7)**: depends on US2 + US3 (reuses modules and pipeline)
- **Polish (Phase 8)**: depends on all stories the team chooses to ship

### Within Each User Story

- Bicep modules (T012–T017) run in parallel; `container-app.bicep` (T018) consumes their outputs
- `main.bicep` (T019) must exist before the deployment command (T021)
- Smoke tests (T011, T024, T028, T034, T039, T041) are always last in their phase

### Parallel Opportunities

- **Setup**: T002 and T003 in parallel
- **Foundational**: T006 can run in parallel with T004/T005 editing different files
- **US2**: T012–T017 and T020 all touch different files and run in parallel
- **US3**: T026 and T027 are independent workflow files
- **Polish**: T042 and T043 are independent

---

## Parallel Example: User Story 2

```bash
# Launch Bicep module authoring in parallel:
Task: "Write infra/bicep/modules/acr.bicep"             # T012
Task: "Write infra/bicep/modules/log-analytics.bicep"   # T013
Task: "Write infra/bicep/modules/app-insights.bicep"    # T014
Task: "Write infra/bicep/modules/key-vault.bicep"       # T015
Task: "Write infra/bicep/modules/postgres.bicep"        # T016
Task: "Write infra/bicep/modules/container-apps-env.bicep"  # T017
Task: "Write infra/bicep/params/dev.parameters.json"    # T020
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational)
2. Complete Phase 3 (US1) — containerize and validate locally
3. **STOP and VALIDATE**: the app runs in a container with healthy probes against local Postgres
4. Decision point: demo the image, then proceed to US2

### Incremental Delivery

1. Setup + Foundational → profile ready
2. US1 → local container MVP
3. US2 → dev env live (demo-able)
4. US3 → push-to-deploy automation
5. US4 → SPA ↔ API wired
6. US5 → prod cutover and rollback rehearsal

### Parallel Team Strategy

Once Phase 2 is done:

- Dev A: US1 (Dockerfile + local validation)
- Dev B: US2 Bicep authoring (can start T012–T017 without the image)
- Dev C: US3 workflow files (mocked resource names until US2 lands)
- Integrate at T023 when the image meets the infra.

---

## Notes

- Every task produces a committed artifact; commit after each task or logical group.
- The single-replica constraint is a project-wide invariant until ShedLock is introduced — any task that changes `container-app.bicep` must preserve `minReplicas == maxReplicas == 1`.
- Outbox tuning knobs are env-var-only: never bake dev/prod values into `application-azure.yml`.
- Secrets live in Key Vault only; no `*.parameters.json` file should ever contain secret values — only references by secret name.
- Open items from `plan.md` (region, OIDC IDs, domain, CORS file path) must be closed before T021, T025, T038, T032 respectively.
