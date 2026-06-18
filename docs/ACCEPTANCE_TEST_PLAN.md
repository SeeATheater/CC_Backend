# Acceptance Test Plan

This plan defines manual acceptance tests before dev/staging release. It focuses on user-visible flows: login, show browsing, reservation, KakaoPay, performer registration, admin approval, auth expiry, and basic cleanup.

Do not record real passwords, tokens, Authorization headers, or payment secrets in QA notes.

## Test Environment

| Item | Value |
| --- | --- |
| Frontend | `https://seeatheater.store` |
| Backend | `https://api.seeatheater.store` |
| Backend profile | dev/staging uses `SPRING_PROFILES_ACTIVE=dev` |
| DB | dev/staging RDS only |
| Redis | Docker Compose `redis` container |
| S3 | dev/staging bucket via presigned URLs |

## Test Accounts And Data

Use dedicated dev/staging test accounts only. If a password is needed, share it out-of-band and do not paste it into logs or tickets.

| Account Type | Required State | Notes |
| --- | --- | --- |
| Audience | ACTIVE, role `AUDIENCE` | Used for login, reserve, KakaoPay ready, my page |
| Performer | ACTIVE, role `PERFORMER` | Used for show registration and registered show detail |
| Admin | ACTIVE, role `ADMIN` | Used for approval/rejection and admin-only API checks |
| Public shows | APPROVED and visible | Shows should have future rounds and tickets |
| Payment data | No stale pending test tickets | Clean failed QA attempts when they block retest |

## Scenario A: General User Login And Reservation

| Field | Detail |
| --- | --- |
| Purpose | Verify a user can log in, browse shows, create a reservation, and reach KakaoPay ready. |
| Preconditions | Audience test account exists; at least one approved show has future rounds and tickets. |
| Steps | 1. Open FE. 2. Log in as audience. 3. Open show list. 4. Open show detail. 5. Click reservation. 6. Select round, ticket, quantity. 7. Submit reserve. 8. Trigger KakaoPay ready. |
| Expected Result | User reaches KakaoPay payment redirect URL or payment window. No duplicate ready call from repeated click. |
| APIs/DB | `GET /amateurs/ranking`, `GET /amateurs/{id}`, `POST /tickets/{showId}/reserve`, `POST /kakaoPay/ready?tempTicketId=...`, `temp_ticket`, `amateur_rounds.total_ticket` |
| Logs | `/kakaoPay/ready`, reservation service logs, DB constraint logs |
| Cleanup | Expire/cancel test TempTickets if they block repeat testing. |

## Scenario B: KakaoPay Return Flow

| Field | Detail |
| --- | --- |
| Purpose | Verify approve/cancel/fail callbacks preserve reservation and stock consistency. |
| Preconditions | KakaoPay ready succeeds and callback URLs point to backend domain. |
| Steps | 1. From KakaoPay window, approve one payment. 2. Repeat with cancel. 3. Repeat with fail if available. 4. Refresh/revisit callback URL only if safe. |
| Expected Result | Approve creates one RealTicket. Cancel/fail do not create RealTicket and restore/expire TempTicket according to current policy. Duplicate approve does not create duplicate RealTicket. |
| APIs/DB | `GET /kakaoPay/approve`, `/cancel`, `/fail`, `temp_ticket.reservation_status`, `real_ticket.kakao_tid`, `amateur_rounds.total_ticket` |
| Logs | KakaoPay request/response status, `Duplicate entry`, `DataIntegrityViolationException` |
| Cleanup | Cancel test tickets where supported; otherwise mark test records for later cleanup. |

## Scenario C: Performer Flow

| Field | Detail |
| --- | --- |
| Purpose | Verify performer can register a show and view registered show details. |
| Preconditions | Performer account exists and can access performer routes. S3 upload env is configured. |
| Steps | 1. Log in as performer. 2. Open show registration. 3. Upload poster. 4. Fill show info, rounds, tickets. 5. Submit. 6. Open my registered performances. |
| Expected Result | Show is created with correct address, poster, rounds, tickets, and approval status. Registered detail includes `roadAddress` and `detailAddress`. |
| APIs/DB | Performer show APIs, presigned upload API, `amateur_show`, `amateur_rounds`, `amateur_ticket`, `image` |
| Logs | S3, show registration, validation errors |
| Cleanup | Delete or reject test show if it should not remain public. |

## Scenario D: Admin Flow

| Field | Detail |
| --- | --- |
| Purpose | Verify admin can approve/reject shows and non-admin users cannot access admin APIs. |
| Preconditions | Admin account exists; at least one waiting show exists. |
| Steps | 1. Log in as admin. 2. Open admin dashboard. 3. Check waiting show list. 4. Approve one show. 5. Verify public exposure. 6. Reject another show if available. 7. Try admin route as non-admin. |
| Expected Result | Admin actions succeed; approved shows become public; non-admin access returns 401/403. |
| APIs/DB | `/admin/**`, `amateur_show.approval_status`, public show APIs |
| Logs | Admin controller logs, access denied logs |
| Cleanup | Restore approval state if the show was only for test. |

## Scenario E: Auth Expiry, Refresh, And Logout

| Field | Detail |
| --- | --- |
| Purpose | Verify session lifecycle and client cleanup behavior. |
| Preconditions | User can log in and browser devtools are available. |
| Steps | 1. Log in. 2. Call private API. 3. Expire/replace access token in test environment. 4. Verify refresh behavior. 5. Remove refresh token and retry. 6. Logout. |
| Expected Result | Valid refresh keeps user logged in; invalid refresh clears session; logout clears local auth state even if API fails. |
| APIs/DB | `/auth/refresh`, `/auth/logout`, Redis refresh token, private APIs |
| Logs | Auth logs, Redis errors, 401/403 responses |
| Cleanup | Clear browser storage. |

## Public/Private API Baseline

Run before and after release:

```bash
./scripts/dev-smoke-test.sh https://api.seeatheater.store
```

Expected checks include:

- `/actuator/health` is public and UP.
- Swagger is reachable in dev/staging.
- `/kakaoPay/ready` rejects anonymous users.
- `/kakaoPay/approve`, `/cancel`, `/fail` are public callbacks and reach validation.
- My page and admin endpoints reject anonymous users.

## QA Result Template

```md
# Acceptance Test Result

- Date/time:
- Tester:
- FE URL:
- BE URL:
- FE commit/deploy:
- BE commit/image:
- DB profile:
- Test accounts used:

## Passed
- [ ] Login
- [ ] Reservation
- [ ] KakaoPay ready
- [ ] KakaoPay approve/cancel/fail
- [ ] Performer registration
- [ ] Admin approval
- [ ] Refresh/logout
- [ ] Smoke test

## Failed / Blockers
| Scenario | Symptom | Logs/API | Owner | Next Action |
| --- | --- | --- | --- | --- |

## Cleanup
- [ ] Test TempTickets reviewed
- [ ] Test RealTickets reviewed
- [ ] Test shows reviewed
- [ ] Browser storage cleared
```
