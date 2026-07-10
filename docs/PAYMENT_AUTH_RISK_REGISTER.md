# Payment And Auth Risk Register

This register tracks payment and authentication risks that can affect release readiness. It reflects the current codebase and dev/staging deployment model. It does not introduce schema or code changes by itself.

Priority:

- P0: Can block login, payment, reservation, or corrupt data.
- P1: Can break major user flows or operations.
- P2: Should be improved but does not block dev/staging if monitored.

## Summary

Current payment defenses include service-level validation, duplicate KakaoPay ready prevention, RealTicket duplicate prevention, and a DB unique constraint on `real_ticket.kakao_tid`. Remaining high-risk areas are transaction boundaries around external KakaoPay calls, cancel/fail/scheduler race conditions, and operational visibility for repeated payment/auth failures.

## Risk Register

| Risk ID | Priority | Risk | Impact | Trigger | Current Defense | Verification | Success Criteria | Remaining Risk | Follow-Up |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PAY-001 | P0 | `/kakaoPay/ready` duplicate call | Duplicate stock decrease or confusing payment state | Double click, retry, browser repeat | Service blocks ready when TempTicket already has `kakaoTid`; FE click guard expected | Unit test and manual repeated ready request | Second ready does not decrease stock | Concurrent calls can still race without DB-level idempotency key | Add idempotency key/state transition lock |
| PAY-002 | P0 | Duplicate RealTicket for same approval | Duplicate ticket issuance | approve callback retry or refresh | `existsByKakaoTid`; DB unique index `uk_real_ticket_kakao_tid` | Query duplicate `kakao_tid`; retry approve | Only one RealTicket exists | Unique violation handling may surface as 500 under race | Catch duplicate key and return idempotent success |
| PAY-003 | P0 | Invalid quantity or stock shortage | Negative/zero reservation or oversell | Bad request, FE bug, concurrent users | DTO/service validation; stock decrease repository method | quantity 0/-1 tests; stock shortage test | Invalid request rejected; stock not changed | High concurrency still needs pessimistic/optimistic lock audit | Add transaction/concurrency tests |
| PAY-004 | P0 | KakaoPay callback URL mismatch | KakaoPay ready 400; payment cannot start | Domain not registered or env mismatch | Env-driven callback URLs | Check Kakao console and app logs | ready returns redirect URL | Console drift can recur | Add release checklist owner/signoff |
| PAY-005 | P0 | KakaoPay external API failure | User cannot pay or approval fails | KakaoPay 400/401/500 | Logs and exception handling | Trigger test ready/approve | Failure is visible and does not corrupt DB | Error response may not be user-friendly | Standardize external API error mapping |
| PAY-006 | P0 | External approve succeeds but DB save fails | Paid user has no ticket | DB outage, constraint failure after external call | Partial duplicate defense only | Simulate DB failure in test env | Failure is detected and compensating action defined | No outbox/compensation state machine yet | Design payment state machine/outbox |
| PAY-007 | P1 | Cancel/fail/scheduler stock restore race | Stock restored more than once | Callback and scheduler overlap | Current status checks | Manual SQL/status review | Stock restored once | stopPayment is not fully atomic | Atomic state update before restore |
| PAY-008 | P1 | `cancelTicket` non-idempotent behavior | Repeated cancellation or inconsistent status | User retry, network retry | Service status checks | Repeat cancel on same RealTicket | Later retries are safe | External cancel/API sequencing remains risky | Add idempotent cancel contract |
| AUTH-001 | P0 | Access token expiry not refreshed | Users suddenly logged out | Expired token, failed interceptor | FE refresh flow; BE refresh endpoint | Expire access token in QA | Valid refresh reissues token | Redis token missing or FE state drift | Add regression tests and monitor refresh failures |
| AUTH-002 | P0 | Refresh token missing/expired in Redis | Users cannot continue session | Redis restart, token TTL, logout | Redis-backed refresh token lifecycle | Redis health and auth flow QA | Client clears session and returns to login | Redis container persistence and monitoring are minimal | Add Redis persistence/alerting plan |
| AUTH-003 | P1 | Duplicate refresh requests | Race causing bad token state | Multiple parallel 401 responses | FE single-flight expected | Browser devtools network | One refresh request for concurrent 401s | Needs FE regression coverage | Add FE auth tests |
| AUTH-004 | P1 | Logout API failure leaves client session | User appears logged in after logout | Network or server failure | FE should clear local session regardless | Manual logout with network failure | Local tokens removed | Server refresh token may remain | Make logout idempotent and monitor failures |
| AUTH-005 | P1 | 401/403 confusion | FE shows wrong UX or hides auth issue | Anonymous vs unauthorized request | Security handlers return 401/403 | Smoke test private/admin endpoints | Anonymous private API 401/403; admin non-admin forbidden | Some controllers may throw raw exceptions | Standardize auth error contract |
| AUTH-006 | P1 | Kakao OAuth optional profile fields missing | Signup/login fails despite Kakao auth | `name`, `nickname`, `phone_number` absent | Optional fallback PR/test coverage expected | Kakao login with minimal profile | Account created with required fields only | Kakao console consent mismatch can recur | Keep OAuth field audit in release checklist |
| AUTH-007 | P1 | Dev/review auth endpoint exposed longer than intended | Temporary bypass remains after review | PR not reverted | Whitelist and DB password verification | SecurityConfig audit | Only intended accounts work | Public endpoint still exists in prod-like env | Track revert issue and deadline |
| S3-001 | P1 | S3 image 403 or presigned URL failure | Posters/images broken | Missing object, private bucket, bad key | Presigned GET fallback in detail flow | Open show detail images | Images load or fallback shown | List endpoints may still return raw URLs | Add list endpoint presigned strategy |
| OPS-001 | P1 | Health is UP but business flow broken | Release appears healthy while payment/login fail | Health only checks infrastructure | Smoke test and acceptance tests | Run smoke + manual QA | Critical flows pass | No synthetic business monitoring yet | Add scheduled smoke job |

## Payment State Checks

Use SELECT-only checks before and after payment QA:

```sql
SELECT id, reservation_status, kakao_tid, quantity, amateur_round_id, amateur_ticket_id, member_id, created_at, updated_at
FROM temp_ticket
ORDER BY id DESC
LIMIT 20;

SELECT id, reservation_status, kakao_tid, quantity, amateur_rounds_id, member_id, reserve_date_time
FROM real_ticket
ORDER BY id DESC
LIMIT 20;

SELECT kakao_tid, COUNT(*) AS cnt
FROM real_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;

SELECT id, amateur_show_id, round_number, performance_date_time, total_ticket
FROM amateur_rounds
WHERE performance_date_time >= NOW()
ORDER BY performance_date_time;
```

## Auth Contract Checks

| Case | Expected BE Response | Expected FE Behavior |
| --- | --- | --- |
| Anonymous private API | 401 or 403 based on current security handler | Redirect/login prompt; no infinite retry |
| Non-admin admin API | 403 | Show access denied or redirect safely |
| Expired access + valid refresh | Refresh returns new access token | Retry original request once |
| Expired/missing refresh | Refresh fails | Clear local auth state and go to login |
| Logout success or failure | API may succeed/fail | Client clears local auth state either way |

## Current Defense Layers

| Layer | Current Defense | Gap |
| --- | --- | --- |
| FE | Button duplicate guard expected for payment; refresh single-flight expected | Needs regression testing after FE changes |
| Controller/DTO | Ticket/payment validation baseline | Other domains still need validation expansion |
| Service | ready duplicate check, quantity checks, member ownership checks | Stop/cancel transaction boundary remains risky |
| DB | `real_ticket.kakao_tid` unique index | No full payment state machine or idempotency table |
| Ops | Smoke test, health check, Docker/Nginx logs | No automated scheduled synthetic monitor |

## P0 Release Gate

Do not release if any of these are unverified:

- KakaoPay ready succeeds for an authenticated user.
- Duplicate ready call does not double-decrease stock.
- Approve creates at most one RealTicket.
- Anonymous `/kakaoPay/ready` is blocked.
- KakaoPay callbacks remain public.
- Login/refresh/logout works without infinite retry.
- Redis and DB are both UP in health.
