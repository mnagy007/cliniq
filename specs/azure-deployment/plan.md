# Azure Deployment Plan — cliniq

## Context

`cliniq` is a Spring Boot 3 / Java 21 modular monolith (bootstrap + web + application + domain + adapters) backed by PostgreSQL with Flyway migrations, a configurable outbox poller (`OutboxScheduler`, default 5s via `cliniq.outbox.poll-delay-ms`), and outbound integrations with Twilio, SendGrid, Google Calendar, and openFDA. Today it only runs locally via `docker-compose` against Postgres; there is **no Dockerfile, no actuator exposure, and no cloud IaC**. The team wants to deploy it to Azure for a `dev` and `prod` environment so a React SPA (hosted separately) can consume the REST API.

**Constraints agreed with the user:**
1. Scheduler topology: **single replica** (Option A) — no ShedLock needed; Container App min=max=1.
2. Environments: **dev** and **prod**, same topology, different sizes and secrets.
3. No data-residency/compliance requirement — pick the nearest region (e.g., `westeurope` or `uaenorth`; confirm at execute-time).
4. IaC tool: **Bicep**.
5. Frontend: React SPA (separate repo/folder) hosted on **Azure Static Web Apps**; `cliniq-web` serves JSON only.

**Outcome:** two reproducible environments, each running a single-replica Container App backed by managed Postgres, with secrets in Key Vault, images in ACR, observability in Application Insights, and a GitHub Actions pipeline that builds, pushes, and rolls a new Container Apps revision on every merge to the matching branch.

---

## Target Architecture (per environment)

| Concern | Azure Service | Notes |
|---|---|---|
| Compute | Azure Container Apps (Consumption) | `min=1, max=1` replica (outbox safety) |
| DB | Azure Database for PostgreSQL Flexible Server | PG 16, private access via VNet |
| Registry | Azure Container Registry (shared across envs) | Basic SKU |
| Secrets | Azure Key Vault (one per env) | Container App managed identity reads secrets |
| Observability | Application Insights + Log Analytics | Java agent auto-attached |
| SPA hosting | Azure Static Web Apps | Separate resource; CORS allow-listed to Container App FQDN |
| Networking | VNet + Private Endpoint for PG | Container App joined via VNet integration |

---

## Deliverables

### A. Application changes (minimum set)

1. **`cliniq-bootstrap/src/main/docker/Dockerfile`** — multi-stage build.
    - Stage 1: `maven:3.9-eclipse-temurin-21` builds the reactor and produces the bootstrap jar.
    - Stage 2: `eclipse-temurin:21-jre` copies `cliniq-bootstrap/target/cliniq-bootstrap-*.jar` as `/app/app.jar`.
    - `EXPOSE 8080`, non-root user, `ENTRYPOINT ["java","-jar","/app/app.jar"]`.
2. **`cliniq-bootstrap/src/main/resources/application.yml`** — add actuator exposure:
    - `management.endpoints.web.exposure.include: health,info,prometheus`
    - `management.endpoint.health.probes.enabled: true`
    - Group `liveness` / `readiness` mappings for Container Apps probes.
3. **`cliniq-bootstrap/src/main/resources/application-azure.yml`** (new profile shared by dev/prod; env-specific values come from env vars/Key Vault refs).
    - Datasource URL/user/password via `SPRING_DATASOURCE_*` env.
    - Outbox tuning bound to env vars so each environment can override without rebuilds:
        - `cliniq.outbox.poll-delay-ms: ${CLINIQ_OUTBOX_POLL_DELAY_MS:5000}`
        - `cliniq.outbox.batch-size: ${CLINIQ_OUTBOX_BATCH_SIZE:50}`
    - `logging.level.root: ${LOG_LEVEL:INFO}`.
4. **`.dockerignore`** at repo root (exclude `target/`, `.git/`, `specs/`, `.claude/`).
5. **No code change** in `OutboxScheduler.java` — single-replica constraint makes it safe. Document the constraint in `ARCHITECTURE.md`.

### B. Infrastructure-as-Code (Bicep)

Create under `infra/bicep/`:

```
infra/bicep/
├── main.bicep                 # entrypoint; parameterized by env
├── modules/
│   ├── acr.bicep              # shared — deployed once
│   ├── log-analytics.bicep
│   ├── app-insights.bicep
│   ├── key-vault.bicep
│   ├── postgres.bicep         # Flexible Server + firewall/private endpoint
│   ├── container-apps-env.bicep
│   ├── container-app.bicep    # single-replica API
│   └── static-web-app.bicep   # React SPA
├── params/
│   ├── dev.parameters.json
│   └── prod.parameters.json
```

Key decisions embedded in Bicep:
- `containerApp.scale: { minReplicas: 1, maxReplicas: 1 }` — enforces outbox safety.
- Managed identity on the Container App with `Key Vault Secrets User` role.
- Secrets bound as `secretRef` env vars (`twilio-auth-token`, `sendgrid-api-key`, `google-oauth-client-secret`, `cliniq-encryption-key`, `db-password`).
- PG Flexible Server: `Standard_B1ms` (dev), `Standard_D2ds_v5` (prod), 32 GB / 128 GB disks, 7-day / 30-day PITR retention.
- **Outbox tuning as Bicep parameters** (per-env overrides in `params/<env>.parameters.json`), surfaced as plain env vars on the Container App:
    - `outboxPollDelayMs` → `CLINIQ_OUTBOX_POLL_DELAY_MS` (dev default `5000`, prod default `2000`).
    - `outboxBatchSize` → `CLINIQ_OUTBOX_BATCH_SIZE` (dev default `50`, prod default `100`).
    - `logLevel` → `LOG_LEVEL` (dev `DEBUG`, prod `INFO`).
    - Changing any of these requires only an `az deployment group create` rerun or a `az containerapp update --set-env-vars` — no image rebuild.

### C. Secrets mapping (Key Vault → env → Spring property)

| Spring property | Env var | Key Vault secret |
|---|---|---|
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | `db-password` |
| `twilio.auth-token` | `TWILIO_AUTH_TOKEN` | `twilio-auth-token` |
| `twilio.account-sid` | `TWILIO_ACCOUNT_SID` | `twilio-account-sid` |
| `sendgrid.api-key` (add to config) | `SENDGRID_API_KEY` | `sendgrid-api-key` |
| `google.calendar.client-secret` | `GOOGLE_CALENDAR_CLIENT_SECRET` | `google-calendar-client-secret` |
| `cliniq.encryption.key` | `CLINIQ_ENCRYPTION_KEY` | `cliniq-encryption-key` |

Non-secret config (`twilio.phone-number`, `sendgrid.from-email`, `google.calendar.base-url`, `openfda.base-url`) stays in `application-azure.yml` or plain env vars.

### D. CI/CD (GitHub Actions)

Create `.github/workflows/deploy.yml`:

- Triggers: push to `dev` → deploy to dev; push to `main` → deploy to prod.
- Steps:
    1. `actions/checkout@v4`
    2. `actions/setup-java@v4` (Temurin 21) + Maven cache
    3. `mvn -pl cliniq-bootstrap -am -DskipITs verify`
    4. `azure/login@v2` with OIDC federated credential (no secrets).
    5. `az acr login --name crcliniqshared`
    6. `docker build -f cliniq-bootstrap/src/main/docker/Dockerfile -t crcliniqshared.azurecr.io/cliniq:${{ github.sha }} .`
    7. `docker push ...`
    8. `az containerapp update --name ca-cliniq-<env> --resource-group rg-cliniq-<env> --image crcliniqshared.azurecr.io/cliniq:${{ github.sha }}`

Separate manual-dispatch workflow for `az deployment group create` (Bicep) — infra changes are rare and gated.

### E. React SPA (separate)

- `static-web-app.bicep` provisions the resource; the SPA's own GitHub Actions workflow (generated by Static Web Apps) handles build/publish.
- Add SPA origin to Spring Boot CORS config (existing `WebConfig` if present — verify at execute-time).

---

## Critical Files to Create / Modify

**New:**
- `cliniq-bootstrap/src/main/docker/Dockerfile`
- `cliniq-bootstrap/src/main/resources/application-azure.yml`
- `.dockerignore`
- `infra/bicep/**` (full tree above)
- `.github/workflows/deploy.yml`
- `.github/workflows/infra.yml`

**Modified:**
- `cliniq-bootstrap/src/main/resources/application.yml` — actuator block + azure profile include.
- `ARCHITECTURE.md` — document single-replica constraint for outbox.
- `cliniq-web` CORS configuration (file to be identified during execution) — add SPA origin.

**Reused (no change):**
- `com.cliniq.bootstrap.CliniqApplication` — entrypoint.
- `com.cliniq.bootstrap.scheduling.OutboxScheduler` — works as-is under min=max=1.
- `spring-boot-maven-plugin` in `cliniq-bootstrap/pom.xml` — produces the deployable jar.
- Existing Flyway migrations under `cliniq-persistence` — run at app startup.

---

## Verification

**Local (before pushing):**
1. `docker build -f cliniq-bootstrap/src/main/docker/Dockerfile -t cliniq:local .`
2. `docker run --rm -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev,azure cliniq:local` against a local Postgres.
3. `curl localhost:8080/actuator/health/liveness` → `{"status":"UP"}`.

**Dev environment smoke test:**
1. `az deployment group create -g rg-cliniq-dev -f infra/bicep/main.bicep -p @infra/bicep/params/dev.parameters.json`.
2. Push a commit to `dev` branch; observe GitHub Actions run green.
3. `curl https://<ca-cliniq-dev-fqdn>/actuator/health` → UP.
4. Hit a representative endpoint (e.g., `GET /api/providers`) → 200 with JSON.
5. Insert a test row into `outbox_entries` via `psql` → within ~5s it should transition to `SENT` in Application Insights logs.
6. Confirm Flyway history table populated: `SELECT * FROM flyway_schema_history;`.

**Prod cutover:**
- Repeat against `rg-cliniq-prod` after dev soak.
- Validate CORS from the Static Web App SPA.
- Enable Application Insights availability test against `/actuator/health`.

**Rollback:** `az containerapp revision list` → `az containerapp ingress traffic set` to previous revision. Zero rebuild required.

---

## Open Items to Resolve at Execute-Time

1. **Azure region** — confirm `westeurope` vs `uaenorth` based on latency/user base.
2. **Azure subscription & tenant IDs** — needed to wire OIDC federated credential for GitHub Actions.
3. **Domain name** — custom domain on Container App (optional for dev, recommended for prod).
4. **CORS source file** — locate existing `WebMvcConfigurer`/`CorsConfigurationSource` in `cliniq-web` to add SPA origin.
