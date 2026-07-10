# Operations Monitoring

This document defines the minimum monitoring and incident response baseline for dev/staging. It focuses on what is available today: Spring Boot Actuator health, Docker Compose, Nginx logs, application logs, RDS checks, Redis container checks, smoke tests, and manual DB SELECTs.

Prometheus/Grafana are not currently required for this release. They are listed as follow-up improvements because the project has Actuator but no Prometheus dependency or exposed `/actuator/prometheus` endpoint today.

## Current Observability Facts

| Area | Current State |
| --- | --- |
| Actuator dependency | Present: `spring-boot-starter-actuator` |
| Exposed Actuator endpoints | `health,info` |
| Health details | `show-details: always` in current config |
| Prometheus dependency | Not present |
| Prometheus endpoint | Not exposed |
| Deployment | Docker Compose blue/green behind Nginx |
| Redis | Docker Compose `redis` container |
| Logs | Docker json-file log rotation configured for app and Redis |
| Smoke test | `scripts/dev-smoke-test.sh` |

This is a dev/staging baseline. Public `/actuator/health` with `show-details: always` is useful while validating RDS and Redis connectivity, but it is too verbose for production-facing operation. Before production release, harden Actuator exposure by limiting health details, restricting endpoint access, or separating internal health checks from public liveness checks.

## Health Check Baseline

| Check | Command/API | Healthy Criteria |
| --- | --- | --- |
| Backend health | `curl -i https://api.seeatheater.store/actuator/health` | HTTP 200 and status `UP` |
| Container status | `docker compose ps` | Active app and Redis running/healthy |
| Nginx config | `sudo nginx -t` | Syntax OK and test successful |
| Nginx proxy | `curl -i https://api.seeatheater.store/actuator/health` | No 502/504 |
| Redis | Actuator health or `docker logs redis` | Redis UP; no repeated connection errors |
| RDS | Actuator health DB component | DB UP; no connection pool failures |

## Core Flow Monitoring

| Flow | Signals To Watch | Failure Symptoms |
| --- | --- | --- |
| Kakao OAuth login | `/auth/kakao/callback`, Kakao profile parsing | 400/500, missing user fields, signup failure |
| Review/general login | `/auth/review/login` while temporary | 401/403/500, wrong role, missing DB password hash |
| Token refresh | `/auth/refresh`, Redis | repeated 401, refresh loop, Redis errors |
| Logout | `/auth/logout` | local session remains, Redis delete failure |
| Reservation | `/tickets/{showId}/reserve` | 400 validation, stock shortage, TempTicket not created |
| KakaoPay ready | `/kakaoPay/ready` | KakaoPay 400/401/500, no redirect URL |
| KakaoPay approve | `/kakaoPay/approve` | RealTicket missing, duplicate key, DB failure |
| KakaoPay cancel/fail | `/kakaoPay/cancel`, `/fail` | stock not restored, TempTicket state wrong |
| Admin approval | `/admin/**` | admin 403, approval state not changed |
| S3 image | presigned GET/PUT APIs and image loads | 403, missing key, broken poster |

## Log Locations And Commands

```bash
# EC2 app directory
cd /home/ubuntu/ccapp

# Containers
docker compose ps
docker logs --tail=200 ccapp-blue
docker logs --tail=200 ccapp-green
docker logs --tail=200 redis

# Follow active logs during QA
docker logs -f --tail=100 ccapp-blue
docker logs -f --tail=100 ccapp-green

# Nginx
sudo nginx -t
sudo tail -n 100 /var/log/nginx/access.log
sudo tail -n 100 /var/log/nginx/error.log

# Smoke test from local machine
./scripts/dev-smoke-test.sh https://api.seeatheater.store
```

Do not paste Authorization headers or token values into logs, issue comments, or documents.

## Log Keywords

Search recent app logs for these keywords when a flow fails:

```bash
docker logs --tail=1000 ccapp-blue 2>&1 | grep -Ei 'kakaoPay|approve|ready|auth/refresh|auth/kakao|PHONENUM_ENCRYPT_FAIL|DataIntegrityViolationException|Duplicate entry|Redis|S3|JWT|403|500'
docker logs --tail=1000 ccapp-green 2>&1 | grep -Ei 'kakaoPay|approve|ready|auth/refresh|auth/kakao|PHONENUM_ENCRYPT_FAIL|DataIntegrityViolationException|Duplicate entry|Redis|S3|JWT|403|500'
```

## Incident Priority

### P0

Escalate immediately if any of these happen:

- `/actuator/health` is DOWN.
- Nginx returns repeated 502/504.
- 5xx responses repeat in a short time window.
- KakaoPay ready repeatedly fails.
- KakaoPay approve fails after payment authorization.
- DB connection errors appear.
- Redis connection errors prevent refresh token flows.
- Duplicate RealTicket or stock inconsistency is suspected.

### P1

Investigate same day:

- Refresh failures increase.
- 401/403 responses spike unexpectedly.
- S3 image 403 increases.
- Admin approval API fails.
- Show detail API returns 500.
- Smoke test fails but health remains UP.

## Runbooks

### KakaoPay Ready Failure

| Field | Detail |
| --- | --- |
| Symptom | User clicks payment but no KakaoPay redirect URL is returned. |
| User Impact | Reservation cannot proceed to payment. |
| First Checks | `curl /actuator/health`, app logs for `/kakaoPay/ready`, browser network response. |
| Log Keywords | `kakaoPay`, `ready`, `400`, `401`, `KAKAOPAY` |
| DB Checks | Latest `temp_ticket`, round stock, `kakao_tid` presence. |
| Possible Causes | Callback domain not registered, wrong KakaoPay secret, invalid TempTicket, duplicate ready, stock shortage. |
| Temporary Action | Confirm callback env and Kakao console; retry with clean TempTicket. |
| Follow-Up | Add external API error mapping and alert on ready failure rate. |

### KakaoPay Approve Failure

| Field | Detail |
| --- | --- |
| Symptom | User returns from KakaoPay but ticket is not created. |
| User Impact | Paid user may not see ticket. |
| First Checks | App logs for `/kakaoPay/approve`; check `temp_ticket` and `real_ticket`. |
| Log Keywords | `approve`, `pg_token`, `Duplicate entry`, `DataIntegrityViolationException` |
| DB Checks | `real_ticket.kakao_tid`, `temp_ticket.reservation_status`, round stock. |
| Possible Causes | Duplicate callback, DB constraint failure, external approve API failure, missing TempTicket. |
| Temporary Action | Preserve logs and DB state; do not manually issue ticket without confirming payment state. |
| Follow-Up | Idempotent approve handling and compensation policy. |

### Login Or Refresh Failure

| Field | Detail |
| --- | --- |
| Symptom | User cannot log in or is repeatedly redirected to login. |
| User Impact | Authenticated flows unavailable. |
| First Checks | Browser devtools network, `/auth/refresh` response, Redis health. |
| Log Keywords | `auth/refresh`, `JWT`, `Redis`, `401`, `403` |
| DB Checks | Member role/status for test account. |
| Possible Causes | Redis token missing, expired refresh, bad FE auth state, wrong role, OAuth profile parsing failure. |
| Temporary Action | Clear browser storage and retry; verify Redis container. |
| Follow-Up | Add auth regression tests and monitor refresh failure count. |

### Admin Page 403

| Field | Detail |
| --- | --- |
| Symptom | Admin cannot access admin dashboard or API. |
| User Impact | Show approval is blocked. |
| First Checks | Member role is `ADMIN`; token authority; admin API response. |
| Log Keywords | `AccessDenied`, `AuthorizationDeniedException`, `403` |
| DB Checks | `SELECT email, role, active_status FROM member WHERE email = ...;` |
| Possible Causes | Wrong role, stale token, selected role mismatch in FE, endpoint authority mismatch. |
| Temporary Action | Re-login after verifying DB role. |
| Follow-Up | Admin security regression tests. |

### S3 Image 403

| Field | Detail |
| --- | --- |
| Symptom | Poster or album image does not load. |
| User Impact | Show pages look broken. |
| First Checks | Image URL type, keyName, presigned URL generation logs. |
| Log Keywords | `S3`, `presigned`, `403`, `keyName` |
| DB Checks | `image.file_path`, `image.content_id`, `image.key_name`, `amateur_show.poster_image_url`. |
| Possible Causes | Missing S3 object, private bucket without presigned URL, blank keyName, expired URL. |
| Temporary Action | Reopen page to regenerate URL; verify object exists. |
| Follow-Up | Consistent presigned URL policy for list/detail endpoints. |

### Show Detail 500

| Field | Detail |
| --- | --- |
| Symptom | Show detail page fails. |
| User Impact | Users cannot view or reserve a show. |
| First Checks | API response, app logs, show row and child rows. |
| Log Keywords | `amateurShow`, `NullPointerException`, `S3`, `500` |
| DB Checks | `amateur_show`, `amateur_rounds`, `amateur_ticket`, `image`. |
| Possible Causes | Missing required child data, null address/poster fields, presigned URL failure. |
| Temporary Action | Hide broken show or fix dev/staging seed data. |
| Follow-Up | Add DTO null-safety and detail API regression test. |

### DB Constraint Violation

| Field | Detail |
| --- | --- |
| Symptom | API returns 500 or fails on save. |
| User Impact | Reservation/payment/show registration may fail. |
| First Checks | App logs for `DataIntegrityViolationException` and `Duplicate entry`. |
| Log Keywords | `DataIntegrityViolationException`, `Duplicate entry`, `constraint` |
| DB Checks | Relevant unique indexes and duplicate rows. |
| Possible Causes | Duplicate `kakao_tid`, duplicate email, FK violation, nullable mismatch. |
| Temporary Action | Do not delete production-like data blindly; collect SQL evidence. |
| Follow-Up | Migration baseline and consistent exception mapping. |

### Redis Connection Failure

| Field | Detail |
| --- | --- |
| Symptom | Refresh/logout or health Redis component fails. |
| User Impact | Users may be logged out or unable to refresh. |
| First Checks | `docker compose ps redis`, app health, Redis logs. |
| Log Keywords | `Redis`, `connection`, `timeout` |
| DB Checks | Not applicable. |
| Possible Causes | Redis container down, network issue, wrong `REDIS_HOST`. |
| Temporary Action | Restart Redis/app in dev/staging if safe. |
| Follow-Up | Evaluate ElastiCache or stronger Redis persistence/alerts. |

### Nginx 502/504

| Field | Detail |
| --- | --- |
| Symptom | API domain returns bad gateway or timeout. |
| User Impact | Entire backend unavailable through public URL. |
| First Checks | `sudo nginx -t`, `docker compose ps`, active upstream port. |
| Log Keywords | `upstream`, `connect() failed`, `timed out` |
| DB Checks | Not first-line unless app is crashing on DB startup. |
| Possible Causes | Active app container down, wrong upstream, app port changed, healthcheck failed. |
| Temporary Action | Switch to healthy blue/green container or rollback. |
| Follow-Up | Automate active upstream verification and alerting. |

## Prometheus And Grafana Follow-Up

Current project state:

- Actuator exists.
- `/actuator/health` and `/actuator/info` are exposed.
- Prometheus dependency is not present.
- `/actuator/prometheus` is not exposed.
- Public health details are a dev/staging convenience and should be hardened before production.

Current minimum monitoring is therefore:

- Health endpoint polling.
- Smoke test execution after deploy.
- Docker/Nginx/app log checks.
- RDS/Redis status checks through health and AWS console.
- Manual SQL checks for payment consistency after QA.

Prometheus/Grafana would help with:

- HTTP request rate, latency, and 5xx trends.
- JVM memory and GC behavior.
- DB connection pool saturation.
- Endpoint-level KakaoPay/auth failure rates.
- Alert rules for P0/P1 conditions.

It is not required in the immediate dev/staging release because the project does not currently expose Prometheus metrics and the first operational target is release confidence, not full observability. It should be a follow-up once release flows are stable.

Future tasks:

- Add Micrometer Prometheus registry dependency.
- Expose `/actuator/prometheus` in the appropriate profile.
- Configure Prometheus scrape target.
- Build Grafana dashboards.
- Add alert rules for health DOWN, 5xx rate, KakaoPay failures, refresh failures, DB pool pressure, and JVM memory.
