# Backend Operational Audit Plan

## Overall Summary

This document is the post-deployment audit plan for improving backend operational stability and data consistency after the new AWS dev/staging redeployment.

Scope:

- Payment and ticket consistency
- RDS schema and entity alignment
- Authentication and authorization boundaries
- API validation and exception handling
- Transaction boundaries and external API calls
- Observability and operational runbooks

Non-goals for this document:

- No production code changes
- No business logic changes
- No database schema changes
- No `SecurityConfig` changes
- No real secret values

Recommended operating principle:

- Fix low-risk visibility and validation issues first.
- Analyze real RDS data before adding unique constraints or changing ticket/payment state transitions.
- Treat payment, ticket stock, refund, and scheduler behavior as high-risk areas that require tests and rollback plans before implementation.

## Severity Classification

### Critical

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| RealTicket duplicate issuance and DB uniqueness | Application-level duplicate prevention exists, but `RealTicket.kakaoTid` has no DB-level unique constraint. Concurrent approve retries can still be risky without a database invariant. | `fix/enforce-real-ticket-kakaotid-uniqueness` | `RealTicket.java`, DB migration files, `RealTicketRepository.java`, KakaoPay tests | Duplicate data SQL, migration dry-run, concurrent approve retry test |
| Stock restore duplication across cancel/fail/scheduler | `stopPayment()` restores stock for `PENDING` TempTicket, while scheduler can also expire pending tickets. Concurrent execution may restore stock more than once unless state update and stock restore are atomic. | `fix/atomic-temp-ticket-expiration-stock-restore` | `KakaoPayBusinessService.java`, `TempTicketExpireScheduler.java`, `TempTicketRepository.java`, concurrency tests | Concurrent cancel/fail/scheduler test, stock count assertion |
| Payment approval external call inside transactional service | `KakaoPayBusinessService` is class-level `@Transactional`; external KakaoPay approve/cancel calls can occur inside DB transactions. Long transactions and partial failures can cause consistency problems. | `refactor/kakaopay-transaction-boundaries` | `KakaoPayBusinessService.java`, `KakaoPayService.java`, payment tests | Transaction boundary tests, failure simulation, DB state assertions |
| Payment success but DB save failure | If KakaoPay approval succeeds but DB confirmation or RealTicket creation fails, the system needs a compensation policy. | `fix/payment-approve-compensation-policy` | `KakaoPayBusinessService.java`, error handling policy, tests | Mock DB failure after approve, verify compensation/manual recovery record |

### High

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| `ddl-auto=update` in dev/prod config | Hibernate can silently mutate schema and hide schema drift. This is risky before production migration. | `chore/introduce-db-migration-baseline` | `application.yml`, `application-prod.yml`, Flyway or Liquibase files | Schema dump comparison, migration from empty DB, migration from staging snapshot |
| Entity and RDS schema mismatch | JPA annotations do not necessarily match the live RDS schema created by `ddl-auto=update`. | `audit/rds-schema-entity-alignment` | Documentation first, then entity/migration files | RDS schema dump, `SHOW CREATE TABLE`, entity annotation checklist |
| Admin authorization consistency | Role hierarchy exists, but admin endpoints should be audited for consistent `ADMIN` enforcement and no controller-level gaps. | `fix/admin-authorization-audit` | `admin/**Controller.java`, method security annotations, tests | Unauthorized/performer/audience/admin API tests |
| `@AuthenticationPrincipal` null safety | Many controllers directly use `member.getId()` or pass `member` assuming authentication. Security rules should match these assumptions. | `fix/auth-principal-null-safety-audit` | Controllers using `@AuthenticationPrincipal`, tests | Unauthenticated request matrix, 401/403 assertions |
| API validation gaps | Multiple controllers use `@RequestBody` without `@Valid`; DTO constraints should be reviewed. | `fix/request-dto-validation-baseline` | Request DTOs, controllers, exception handler | Invalid payload tests, error response format assertions |
| Exception handling inconsistency | `IllegalArgumentException`, `IllegalStateException`, `GeneralException`, `ResponseStatusException`, and `printStackTrace()` are mixed. | `fix/exception-handling-consistency` | Global exception handler, affected services/controllers | Representative error contract tests |

### Medium

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| SecurityConfig `permitAll` inventory | Current public endpoints should be documented and regression-tested so future changes do not accidentally expose user APIs. | `test/security-permitall-regression` | `SecurityConfig.java` tests only, docs | Spring Security request matcher tests |
| `/auth/dev/*` profile guard regression | Current controller uses `(local | dev) & !prod`; this should be verified in profile-based tests. | `test/dev-auth-profile-guard` | `DevAuthController` tests, context tests | dev profile registers bean, prod profile does not |
| Refresh token lifecycle | Redis refresh token save/validate/logout behavior should be tested with expiry and logout flows. | `test/refresh-token-lifecycle` | `TokenProvider.java`, `RefreshTokenService.java`, auth tests | Login, refresh, logout, refresh-after-logout tests |
| Public actuator health detail | `/actuator/health` is public and `show-details: always`; useful in dev/staging but too verbose for production. | `chore/actuator-prod-hardening` | `application-prod.yml`, deployment docs | Prod profile health response check |
| Logging and operational commands | App, Nginx, and Docker log commands should be documented and eventually centralized. | `docs/ops-runbook-logs` | `docs/DEPLOYMENT_PLAN.md`, runbook docs | Manual command verification on EC2 |
| Smoke test automation | Manual smoke tests exist in documentation, but repeatable script would reduce deployment risk. | `chore/dev-smoke-test-script` | `scripts/smoke-test.sh`, docs | Run script against dev/staging host |

### Low

| Item | Risk | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- | --- |
| Remove `printStackTrace()` | Stack traces are printed directly in some controllers/converters/tests. Replace with structured logging where production code is affected. | `chore/remove-printstacktrace-production` | `KakaoPayController.java`, `PhotoAlbumServiceImpl.java`, `AmateurConverter.java` | Compile, error path tests |
| API empty/null response policy | Some APIs return empty lists while others may return null fields. Define response conventions. | `docs/api-response-policy` | API docs, DTO notes | Contract review |
| Service size and responsibility split | Large services such as payment, board, photo album, and amateur show services are harder to test. | `refactor/service-responsibility-split-plan` | Design doc first | No code verification until implementation PR |
| Docker image tag immutability | Deployment currently still uses mutable image tags. This affects rollback traceability. | `chore/docker-immutable-image-tags` | GitHub Actions, deploy scripts, docs | Deploy by commit SHA tag and rollback test |

## Payment And Ticket Consistency

### Current Observations

- `KakaoPayBusinessService.completePayment()` checks `RealTicketRepository.existsByKakaoTid(tempTicket.getKakaoTid())` before issuing a RealTicket.
- `RealTicket.kakaoTid` is mapped with `@Column(name = "kakao_tid")`, but no unique constraint is declared at the entity level.
- `TempTicket.kakaoTid` is also stored and used for KakaoPay approve.
- `confirmReservation()` returns early if TempTicket is already `RESERVED`.
- `stopPayment()` restores stock and changes TempTicket to `EXPIRED` only when current status is `PENDING`.
- `KakaoPayBusinessService` is class-level transactional, while KakaoPay external API calls are made inside service methods.

### Existing Duplicate Data Check SQL

Run these against a read-only staging snapshot first:

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
- Repeated approve for the same TempTicket does not create additional RealTickets.
- Concurrent repeated approve creates exactly one RealTicket.
- Cancel callback and fail callback racing against scheduler restore stock once.
- Approve after TempTicket expiration fails without issuing a RealTicket.
- KakaoPay approve succeeds but RealTicket creation fails: verify defined compensation behavior.

### Recommended State Transition Audit

Document legal TempTicket transitions before refactoring:

```text
PENDING -> RESERVED
PENDING -> EXPIRED
RESERVED -> no further TempTicket transition
EXPIRED -> terminal
```

Document legal RealTicket transitions:

```text
RESERVED -> CANCELLED
RESERVED -> USED
CANCELLED -> terminal
USED -> terminal
```

Do not implement a full state machine before confirming real data, scheduler behavior, and KakaoPay redirect retry behavior.

## DB Level Audit

### RDS Schema Dump Commands

Use a staging-safe account and never commit dump files containing production data.

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

### Schema Items To Check

- `real_ticket.kakao_tid`: nullable, unique, index status.
- `temp_ticket.kakao_tid`: nullable and index status.
- Ticket ownership columns: `member_id`, `amateur_rounds_id`, `amateur_ticket_id`.
- Reservation status columns: enum string length and nullable status.
- FK constraints between ticket, member, amateur round, and amateur ticket tables.
- Indexes for:
  - member ticket list: `real_ticket.member_id`
  - member and status ticket list: `real_ticket.member_id`, `real_ticket.reservation_status`
  - admin search: `real_ticket.show_title`, `real_ticket.reservation_status`
  - payment duplicate check: `real_ticket.kakao_tid`
  - pending expiration: `temp_ticket.reservation_status`, `temp_ticket.created_at`

### `ddl-auto=update` Risk

Current config includes `ddl-auto: update` in common and prod profile configuration. This is acceptable only as a temporary dev/staging convenience. Before production:

- Baseline the current schema.
- Introduce Flyway or Liquibase.
- Change production schema management away from Hibernate auto-update.
- Test migration on a staging snapshot.

## Authentication And Authorization Audit

### Items To Verify

- `/auth/dev/login` and `/auth/dev/refresh` remain unavailable when `prod` profile is active.
- `SecurityConfig` public endpoint list is intentional and tested.
- `POST /kakaoPay/ready` requires authentication.
- `GET /kakaoPay/approve`, `/kakaoPay/cancel`, and `/kakaoPay/fail` remain public callback endpoints.
- Admin APIs consistently require `ADMIN` role.
- Performer-only and audience-only actions are separated.
- Controllers using `@AuthenticationPrincipal(expression = "member")` do not rely on public endpoints.
- Logout handles missing or invalid authentication consistently.
- Refresh token Redis lifecycle is tested for login, refresh, logout, expiry, and refresh reuse after logout.

### SecurityConfig PermitAll Inventory

Current public categories to audit:

- `OPTIONS /**`
- `/actuator/health`, `/actuator/info`
- `/error`
- `/auth/**`
- KakaoPay callback endpoints
- Swagger and OpenAPI docs
- `/admin/login`
- Public read APIs for photo albums, boards, amateurs
- `/upload/**`

Important follow-up:

- Since `/auth/**` is broad, profile tests for `DevAuthController` are essential.
- Since `/upload/**` is public, confirm whether this path is only a generated S3 object path or an application endpoint that can mutate state.

## API Validation And Exception Handling

### Validation Audit Targets

Search pattern:

```bash
rg -n "@RequestBody" src/main/java
rg -n "@Valid @RequestBody|@RequestBody .*@Valid" src/main/java
```

Review each request DTO for:

- `@NotNull` for required IDs and enum fields.
- `@NotBlank` for required strings.
- `@Positive` or `@PositiveOrZero` for quantity, price, page, and size fields.
- `@Size` for bounded text fields.
- `@Valid` for nested DTOs and lists.

### Exception Consistency

Current code mixes several exception paths:

- `GeneralException`
- `IllegalArgumentException`
- `IllegalStateException`
- `ResponseStatusException`
- raw `RuntimeException`
- direct `printStackTrace()`

Recommended direction:

- Use `GeneralException` for business errors with defined error codes.
- Use validation exceptions for invalid request DTOs.
- Use structured logging instead of `printStackTrace()`.
- Keep one API error response format for controllers.
- Add tests for representative 400, 401, 403, 404, 409, and 500 responses.

## Transactions And External API Calls

### Audit Questions

- Which methods call KakaoPay inside an open DB transaction?
- What DB state exists when KakaoPay approve succeeds but local DB save fails?
- What happens if KakaoPay cancel succeeds but local ticket cancellation fails?
- Are stock decrease and TempTicket state transitions atomic?
- Are stock restore and TempTicket expiration atomic?
- Can scheduler and callback process the same TempTicket at the same time?

### Recommended Refactoring Direction

Do not jump directly into a full rewrite. Recommended sequence:

1. Add tests that reproduce current behavior.
2. Extract transaction boundary decisions into smaller private/application methods.
3. Move external API calls outside long transactions where possible.
4. Add compensation or manual recovery records for unresolved external/local mismatches.
5. Consider outbox or payment event table only after current flows are covered by tests.

## Observability And Operations

### Current Dev/Staging Checks

- `/actuator/health` is public.
- Docker Compose health checks target app containers.
- Redis runs inside Docker Compose.
- Nginx proxies public port 80 to blue/green app ports.
- Deployment documentation includes smoke test commands.

### Recommended Operational Improvements

| Item | Recommended branch | Expected files | Verification |
| --- | --- | --- | --- |
| CloudWatch log shipping | `chore/cloudwatch-log-agent-dev` | EC2 setup docs, CloudWatch agent config | App, Nginx, Docker logs visible in CloudWatch |
| Smoke test script | `chore/dev-smoke-test-script` | `scripts/smoke-test.sh`, docs | Script exits non-zero on failed health/API checks |
| Deployment rollback drill | `docs/rollback-runbook` | Deployment docs | Manual rollback on dev/staging |
| Actuator production hardening | `chore/actuator-prod-hardening` | `application-prod.yml`, docs | Prod health does not expose unnecessary details |
| Error log correlation | `chore/request-correlation-id` | filter/interceptor/log config | Request ID appears in app logs |

### Incident Commands To Document

```bash
docker compose ps
docker logs ccapp-blue --tail=200
docker logs ccapp-green --tail=200
docker logs redis --tail=100
sudo tail -n 200 /var/log/nginx/error.log
sudo tail -n 200 /var/log/nginx/access.log
curl -i http://localhost/actuator/health
docker exec redis redis-cli ping
```

## Safe To Fix Immediately

These are low-risk and can be done without changing business policy:

- Add security/profile regression tests for `/auth/dev/*`.
- Add SecurityConfig endpoint access tests.
- Add request DTO validation to clearly required fields.
- Replace production `printStackTrace()` with logger usage.
- Add smoke test script for dev/staging.
- Expand deployment runbook with rollback and log commands.
- Add repository/service tests for refresh token logout lifecycle.

## Analyze Before Fixing

These require real schema/data review or a behavior matrix before implementation:

- `RealTicket.kakaoTid` unique constraint.
- Existing duplicate payment/ticket data cleanup.
- Entity nullable/index/FK alignment with RDS.
- `ddl-auto=update` migration to Flyway or Liquibase.
- Stock restore race between callback and scheduler.
- Full admin/performer/audience authorization matrix.
- API response null/empty conventions.
- Actuator exposure policy by environment.

## Risky To Change Now

These should wait until tests and data audit are complete:

- Full payment state machine rewrite.
- Moving all KakaoPay external calls outside transactions without compensation design.
- Introducing DB unique constraints before duplicate data is checked and cleaned.
- Replacing scheduler behavior without concurrency tests.
- Large service decomposition of payment, board, photo album, or amateur show flows.
- Production migration tooling rollout without staging snapshot rehearsal.

## Recommended Work Order

1. `test/security-permitall-regression`
   - Purpose: lock down current authentication boundary.
   - Files: Security tests, profile context tests.
   - Verification: unauthenticated/authenticated request matrix.

2. `chore/dev-smoke-test-script`
   - Purpose: make deployment verification repeatable.
   - Files: `scripts/smoke-test.sh`, deployment docs.
   - Verification: run against dev/staging host.

3. `audit/rds-schema-entity-alignment`
   - Purpose: compare real RDS schema with JPA entities.
   - Files: audit doc only at first.
   - Verification: schema-only dump and table/index checklist.

4. `test/payment-ticket-consistency-regression`
   - Purpose: add tests before risky payment changes.
   - Files: KakaoPay, TempTicket, RealTicket tests.
   - Verification: repeated approve, concurrent approve, cancel/fail/scheduler races.

5. `fix/enforce-real-ticket-kakaotid-uniqueness`
   - Purpose: add DB invariant after duplicate data check.
   - Files: migration file, entity/repository tests.
   - Verification: migration dry-run, duplicate insert failure test.

6. `fix/atomic-temp-ticket-expiration-stock-restore`
   - Purpose: prevent double stock restore.
   - Files: payment service, scheduler, repository, tests.
   - Verification: concurrent callback/scheduler test.

7. `fix/request-dto-validation-baseline`
   - Purpose: reduce invalid input and inconsistent 500s.
   - Files: DTOs, controllers, exception tests.
   - Verification: invalid payload test suite.

8. `refactor/kakaopay-transaction-boundaries`
   - Purpose: reduce long transactions and define compensation.
   - Files: payment service structure and tests.
   - Verification: failure simulation around external API and DB save.

9. `chore/introduce-db-migration-baseline`
   - Purpose: move away from `ddl-auto=update`.
   - Files: Flyway or Liquibase baseline, application config.
   - Verification: empty DB migration, staging snapshot migration.

10. `chore/actuator-prod-hardening`
    - Purpose: reduce production operational exposure.
    - Files: prod config, docs.
    - Verification: prod profile health response check.

## Final Notes

- This audit plan intentionally separates documentation, tests, data checks, and risky refactors.
- Payment and ticket consistency must be protected at both application and database levels.
- The next most valuable PR is not a refactor; it is a test/audit PR that proves current behavior before changing it.
