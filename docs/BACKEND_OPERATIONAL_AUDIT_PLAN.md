# Backend Operational Audit Plan

## Purpose

This document is the follow-up PR separation guide for backend operational stabilization after the dev/staging redeployment.

Use this document to decide:

- Which issues should be fixed immediately.
- Which issues require real data or schema analysis first.
- Which issues are risky and should wait until tests, rollback plans, and migration plans are ready.
- Which branch should own each follow-up task.

This document is intentionally documentation-only. It must not include real secret values, real private keys, passwords, access tokens, or production-only operational credentials.

## Overall Summary

The backend is now deployable to the new AWS dev/staging environment, but production readiness still depends on payment/ticket consistency, database schema governance, authorization regression tests, validation consistency, transaction boundaries, and observability.

The highest-risk area is payment and ticket state consistency. Application-level duplicate RealTicket prevention exists, but DB-level invariants, concurrency behavior, scheduler interaction, and compensation policies still need a deeper audit before production.

Recommended direction:

- Start with tests and audits before risky refactors.
- Add DB constraints only after checking existing data.
- Keep payment and ticket changes small, reversible, and covered by concurrency tests.
- Treat this document as the backlog map for follow-up PRs.

## Critical

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| RealTicket `kakaoTid` DB uniqueness | Application checks can reduce duplicate issuance, but without a DB unique index, concurrent approve retry risk remains. | `fix/enforce-real-ticket-kakaotid-uniqueness` | `RealTicket.java`, migration files, `RealTicketRepository.java`, KakaoPay tests | Duplicate data SQL, migration dry-run, concurrent approve retry test |
| Cancel/fail/scheduler stock restore race | Callback and scheduler paths can target the same pending TempTicket. Stock restore must be atomic and idempotent. | `fix/atomic-temp-ticket-expiration-stock-restore` | `KakaoPayBusinessService.java`, `TempTicketExpireScheduler.java`, `TempTicketRepository.java`, concurrency tests | Concurrent cancel/fail/scheduler test, final stock assertion |
| KakaoPay external calls inside transaction | Long DB transactions around external API calls can create partial failure and lock-duration issues. | `refactor/kakaopay-transaction-boundaries` | `KakaoPayBusinessService.java`, `KakaoPayService.java`, payment tests | Failure simulation, transaction boundary tests, DB state assertions |
| KakaoPay approve success but DB save failure | If external approval succeeds and local DB persistence fails, manual recovery or compensation policy is required. | `fix/payment-approve-compensation-policy` | `KakaoPayBusinessService.java`, payment recovery docs/tests | Mock DB failure after approve, verify defined recovery behavior |

## High

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| `ddl-auto=update` risk | Hibernate schema auto-update can hide schema drift and mutate production schema unexpectedly. | `chore/introduce-db-migration-baseline` | `application.yml`, `application-prod.yml`, Flyway or Liquibase migrations | Empty DB migration, staging snapshot migration |
| RDS schema and entity mismatch | Current live schema may not match entity annotations, nullable rules, indexes, or FK expectations. | `audit/rds-schema-entity-alignment` | Audit doc first, then entity/migration files | `SHOW CREATE TABLE`, schema-only dump, index checklist |
| Admin authorization consistency | Admin APIs should consistently enforce `ADMIN` role and avoid controller-level gaps. | `fix/admin-authorization-audit` | `admin/**Controller.java`, method security tests | audience/performer/admin request matrix |
| `@AuthenticationPrincipal` null safety | Controllers often assume authenticated `Member`; SecurityConfig and controller behavior must match. | `fix/auth-principal-null-safety-audit` | Controllers using `@AuthenticationPrincipal`, tests | Unauthenticated request matrix, 401/403 assertions |
| DTO validation gaps | `@RequestBody` without `@Valid` or DTO constraints can cause inconsistent 400/500 behavior. | `fix/request-dto-validation-baseline` | Request DTOs, controllers, exception tests | Invalid payload tests |
| Exception handling inconsistency | `GeneralException`, `IllegalArgumentException`, `IllegalStateException`, `ResponseStatusException`, raw `RuntimeException`, and `printStackTrace()` are mixed. | `fix/exception-handling-consistency` | Global exception handler, affected controllers/services | Representative error response tests |

## Medium

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| SecurityConfig `permitAll` inventory | Public endpoint list should be locked by tests to avoid accidental exposure. | `test/security-permitall-regression` | Security tests | Authenticated/unauthenticated endpoint matrix |
| `/auth/dev/*` profile guard regression | Dev auth must stay unavailable under `prod`. | `test/dev-auth-profile-guard` | Profile context tests | dev profile registers controller, prod profile does not |
| Refresh token lifecycle | Redis refresh token save, validate, logout, expiry, and reuse-after-logout behavior needs tests. | `test/refresh-token-lifecycle` | `TokenProvider.java`, `RefreshTokenService.java`, auth tests | login/refresh/logout/refresh-after-logout tests |
| Actuator health detail | Public health detail is useful in dev/staging, but should be restricted before production. | `chore/actuator-prod-hardening` | `application-prod.yml`, docs | Prod profile health response check |
| Smoke test automation | Manual deployment checks are easy to skip. | `chore/dev-smoke-test-script` | `scripts/smoke-test.sh`, deployment docs | Script exits non-zero on failed checks |
| Operational log runbook | App, Docker, Redis, and Nginx log commands should be documented in one place. | `docs/ops-runbook-logs` | Deployment docs/runbook | Manual command verification on EC2 |

## Low

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| Remove production `printStackTrace()` | Direct stack trace printing is noisy and less searchable than structured logs. | `chore/remove-printstacktrace-production` | Affected production classes | Compile, error path tests |
| API null/empty response policy | Inconsistent null and empty responses make FE handling harder. | `docs/api-response-policy` | API docs, DTO notes | Contract review |
| Service responsibility split | Large services are harder to test but refactoring them before behavior tests is risky. | `refactor/service-responsibility-split-plan` | Design doc first | No code verification until implementation PR |
| Immutable Docker tags | Mutable `latest` tags reduce rollback traceability. | `chore/docker-immutable-image-tags` | GitHub Actions, deploy scripts, docs | Deploy and rollback by commit SHA tag |

## Payment And Ticket Consistency

### Risks

- `RealTicket.kakaoTid` should be reviewed for a unique index.
- Existing duplicate RealTicket rows must be checked before adding a unique constraint.
- Approve retry must be idempotent for the same TempTicket/order.
- Cancel, fail, and scheduler paths must not restore stock more than once.
- TempTicket status transitions need a documented allowed transition matrix.
- Payment success followed by local DB failure needs compensation or manual recovery policy.

### Duplicate Data Check SQL

Run against a staging snapshot or read-only operational session. Do not commit query output if it contains user data.

```sql
SELECT kakao_tid, COUNT(*) AS cnt
FROM real_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;
```

```sql
SELECT kakao_tid, COUNT(*) AS cnt
FROM temp_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;
```

```sql
SELECT id, kakao_tid, reservation_status, created_at, updated_at
FROM temp_ticket
WHERE kakao_tid IS NOT NULL
ORDER BY kakao_tid, id;
```

### Required Tests

- First approve creates exactly one RealTicket.
- Repeated approve for the same TempTicket does not create a second RealTicket.
- Concurrent approve for the same TempTicket creates exactly one RealTicket.
- Cancel callback and fail callback racing against scheduler restore stock once.
- Approve after TempTicket expiration does not issue a RealTicket.
- KakaoPay approve succeeds but local DB persistence fails and follows a documented recovery policy.

### State Transition Draft

TempTicket:

```text
PENDING -> RESERVED
PENDING -> EXPIRED
RESERVED -> terminal for TempTicket
EXPIRED -> terminal
```

RealTicket:

```text
RESERVED -> CANCELLED
RESERVED -> USED
CANCELLED -> terminal
USED -> terminal
```

Do not implement a full state machine until real data, scheduler behavior, and KakaoPay retry behavior are verified.

## DB Level Audit

### Schema Dump

Use schema-only dumps first. Never commit dumps that contain production or user data.

```bash
mysqldump \
  --no-data \
  --single-transaction \
  --skip-comments \
  -h <rds-endpoint> \
  -u <db-user> \
  -p \
  <db-name> > schema-only.sql
```

### SQL Checks

```sql
SHOW TABLES;
SHOW CREATE TABLE real_ticket;
SHOW CREATE TABLE temp_ticket;
SHOW CREATE TABLE member;
SHOW CREATE TABLE amateur_rounds;
SHOW INDEX FROM real_ticket;
SHOW INDEX FROM temp_ticket;
SHOW INDEX FROM member;
```

### Items To Compare

- Entity nullable rules vs actual table nullable rules.
- Unique constraints and indexes.
- Foreign keys for member, ticket, amateur round, and show relations.
- `real_ticket.kakao_tid` uniqueness/index status.
- `temp_ticket.reservation_status` and `created_at` index for scheduler.
- Query patterns used by ticket list, admin ticket search, and payment duplicate checks.
- Current `ddl-auto=update` behavior under dev/prod profiles.

## Authentication And Authorization Audit

### Items To Verify

- `/auth/dev/login` and `/auth/dev/refresh` are not registered under `prod`.
- SecurityConfig public endpoint list is intentional.
- `POST /kakaoPay/ready` requires authentication.
- `GET /kakaoPay/approve`, `/kakaoPay/cancel`, and `/kakaoPay/fail` remain public callback endpoints.
- Admin APIs consistently require `ADMIN`.
- Performer and audience permissions are separated.
- `@AuthenticationPrincipal` usage is null-safe or protected by authentication rules.
- Redis refresh token lifecycle handles login, refresh, logout, expiry, and refresh-after-logout.

### PermitAll Inventory To Regression-Test

- `OPTIONS /**`
- `/actuator/health`, `/actuator/info`
- `/error`
- `/auth/**`
- KakaoPay callback endpoints
- Swagger/OpenAPI docs
- `/admin/login`
- Public read APIs for photo albums, boards, amateurs
- `/upload/**`

## API Validation And Exception Handling

### Validation Search

```bash
rg -n "@RequestBody" src/main/java
rg -n "@Valid @RequestBody|@RequestBody .*@Valid" src/main/java
```

### DTO Constraint Candidates

- `@NotNull` for required IDs, enum values, and date fields.
- `@NotBlank` for required strings.
- `@Positive` for quantity, price, page, and size values.
- `@Size` for user-provided text.
- `@Valid` for nested DTOs and request lists.

### Exception Policy Direction

- Use `GeneralException` for domain/business errors with known error codes.
- Use validation exceptions for request validation failures.
- Avoid raw `RuntimeException` for expected business errors.
- Replace production `printStackTrace()` with structured logging.
- Keep API error response shape consistent.

## Transaction And External API Audit

### Questions

- Which service methods call KakaoPay inside an open DB transaction?
- What DB state remains if KakaoPay approve succeeds but local DB save fails?
- What DB state remains if KakaoPay cancel succeeds but local ticket cancellation fails?
- Are stock decrease and TempTicket update atomic?
- Are stock restore and TempTicket expiration atomic?
- Can scheduler and callback process the same TempTicket concurrently?

### Recommended Refactor Order

1. Add regression tests for current behavior.
2. Define payment and ticket state transitions.
3. Separate external API calls from long transactions where safe.
4. Add compensation/manual recovery policy.
5. Consider outbox or payment event table only after the simpler behavior is tested.

## Observability And Operations

### Current Focus

- Actuator health endpoint.
- Docker Compose app/Redis logs.
- Nginx access/error logs.
- Deployment smoke test commands.
- Rollback and blue/green verification commands.

### Follow-Up Candidates

| Item | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- |
| CloudWatch log shipping | `chore/cloudwatch-log-agent-dev` | EC2 setup docs, CloudWatch agent config | App/Nginx/Docker logs visible in CloudWatch |
| Smoke test script | `chore/dev-smoke-test-script` | `scripts/smoke-test.sh`, docs | Script exits non-zero on failed checks |
| Rollback runbook | `docs/rollback-runbook` | Deployment docs | Manual rollback drill on dev/staging |
| Request correlation ID | `chore/request-correlation-id` | filter/interceptor/log config | Request ID appears in app logs |

## Safe To Fix Immediately

- Add security/profile regression tests for `/auth/dev/*`.
- Add SecurityConfig endpoint access tests.
- Add request DTO validation for clearly required fields.
- Replace production `printStackTrace()` with logger usage.
- Add a dev/staging smoke test script.
- Expand operational runbook commands.
- Add refresh token lifecycle tests.

## Analyze Before Fixing

- `RealTicket.kakaoTid` unique constraint.
- Existing duplicate ticket/payment data cleanup.
- Entity and RDS schema alignment.
- `ddl-auto=update` replacement with Flyway or Liquibase.
- Cancel/fail/scheduler stock restore race.
- Full admin/performer/audience authorization matrix.
- API null/empty response policy.
- Actuator exposure policy by environment.

## Risky To Change Now

- Full payment state machine rewrite.
- Moving all KakaoPay calls outside transactions without compensation design.
- Adding unique constraints before duplicate data is checked.
- Replacing scheduler behavior without concurrency tests.
- Large service decomposition before regression tests.
- Production migration tooling rollout without staging rehearsal.

## Recommended Work Order

1. `test/security-permitall-regression`
   - Purpose: lock down current authentication boundary.
   - Verification: authenticated/unauthenticated endpoint matrix.

2. `chore/dev-smoke-test-script`
   - Purpose: make deployment verification repeatable.
   - Verification: run script against dev/staging host.

3. `audit/rds-schema-entity-alignment`
   - Purpose: compare live RDS schema with JPA entities.
   - Verification: schema-only dump and index checklist.

4. `test/payment-ticket-consistency-regression`
   - Purpose: add tests before risky payment changes.
   - Verification: repeated approve, concurrent approve, and stock restore race tests.

5. `fix/enforce-real-ticket-kakaotid-uniqueness`
   - Purpose: add DB invariant after duplicate data check.
   - Verification: migration dry-run and duplicate insert failure test.

6. `fix/atomic-temp-ticket-expiration-stock-restore`
   - Purpose: prevent double stock restore.
   - Verification: concurrent callback/scheduler test.

7. `fix/request-dto-validation-baseline`
   - Purpose: reduce invalid input and inconsistent 500s.
   - Verification: invalid payload test suite.

8. `refactor/kakaopay-transaction-boundaries`
   - Purpose: reduce long transactions and define compensation.
   - Verification: failure simulation around external API and DB save.

9. `chore/introduce-db-migration-baseline`
   - Purpose: replace Hibernate schema auto-update with managed migration.
   - Verification: empty DB migration and staging snapshot migration.

10. `chore/actuator-prod-hardening`
    - Purpose: reduce production health endpoint exposure.
    - Verification: prod profile health response check.
