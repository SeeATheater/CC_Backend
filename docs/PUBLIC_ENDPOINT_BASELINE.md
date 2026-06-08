# Public Endpoint Baseline

This document captures the current dev/staging public exposure baseline before DNS, HTTPS, OAuth callback, KakaoPay callback, or frontend base URL changes.

The goal is not to change security policy in this PR. The goal is to make the current expectation repeatable with `scripts/dev-smoke-test.sh`.

## Current Public/Private Expectations

| Endpoint | Method | Expected anonymous result | Reason |
| --- | --- | --- | --- |
| `/actuator/health` | `GET` | `200`, body contains `"status":"UP"` | Public health check for deployment and Nginx/Compose verification. |
| `/swagger-ui/index.html` | `GET` | `200` | Swagger is currently public in dev/staging. Revisit before production. |
| `/amateurs/ranking` | `GET` | `200` | Public read API. |
| `/boards` | `GET` | `400` without `boardType` | Public read API that reaches request validation without authentication. |
| `/kakaoPay/ready?tempTicketId=1` | `POST` | `401` or `403` | User action. Anonymous requests must be blocked. |
| `/kakaoPay/approve` | `GET` | `400` without required params | Public KakaoPay callback endpoint. Missing params should fail validation, not authentication. |
| `/kakaoPay/cancel` | `GET` | `400` without required params | Public KakaoPay callback endpoint. Missing params should fail validation, not authentication. |
| `/kakaoPay/fail` | `GET` | `400` without required params | Public KakaoPay callback endpoint. Missing params should fail validation, not authentication. |
| `/myTickets/list` | `GET` | `401` or `403` | User ticket API. Anonymous requests must be blocked. |
| `/member/myPage` | `GET` | `401` or `403` | User profile API. Anonymous requests must be blocked. |
| `/admin/dashboard/approval` | `GET` | `401` or `403` | Admin API. Anonymous requests must be blocked. |

## Profile-Sensitive Endpoints

`/auth/dev/login` and `/auth/dev/refresh` are intentionally profile-sensitive.

Current expectation:

- Available only when `local` or `dev` profile is active and `prod` is not active.
- Not registered when `prod` profile is active.
- Not included in the smoke script because calling it can mint tokens in dev/staging when seeded test accounts exist.

Follow-up recommendation:

- Add profile-level regression tests in a dedicated security test PR.

## Known Follow-Up Review Items

These are intentionally not changed in this PR:

- `/auth/**` is broadly `permitAll`; controller-level behavior and profile guards need regression tests.
- Swagger/OpenAPI docs are public in dev/staging; production exposure policy should be revisited.
- `/actuator/health` exposes details in dev/staging; production exposure policy should be revisited.
- `/upload/**` is currently public in `SecurityConfig`; confirm whether this path is only a generated object path or an application endpoint that can mutate state.

## Smoke Test Usage

Run against the current HTTP endpoint:

```bash
./scripts/dev-smoke-test.sh http://<dev-api-host>
```

After DNS/HTTPS is configured, run the same script against the HTTPS domain:

```bash
./scripts/dev-smoke-test.sh https://<dev-api-domain>
```

Optional timeout override:

```bash
SMOKE_TIMEOUT_SECONDS=20 ./scripts/dev-smoke-test.sh http://<dev-api-host>
```
