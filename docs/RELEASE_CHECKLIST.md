# Release Checklist

This checklist is the go/no-go gate for dev/staging release of the See A Theater backend. It reflects the current deployment model: Docker Hub -> EC2 -> Docker Compose blue/green -> Nginx -> Spring Boot dev profile with RDS, Redis container, S3, Kakao OAuth, and KakaoPay.

Do not paste secrets, tokens, passwords, or Authorization headers into this document or release notes.

## Release Inputs

| Item | Expected Value | How To Check | Success Criteria | If It Fails |
| --- | --- | --- | --- | --- |
| Backend API URL | `https://api.seeatheater.store` | Browser or `curl -i https://api.seeatheater.store/actuator/health` | HTTPS responds and health is `UP` | Check DNS, Nginx, active app container, Security Group 80/443 |
| Frontend URL | `https://seeatheater.store` | Browser open | App loads without asset, CORS, or routing failure | Check FE deployment and browser console |
| FE API base URL | Backend API domain | Browser devtools Network tab | API calls go to `https://api.seeatheater.store` | Check `VITE_APP_API_URL` and redeploy FE |
| Backend profile | `SPRING_PROFILES_ACTIVE=dev` for dev/staging | EC2 `.env` and container env | App uses `DEV_DB_URL` | Check `/home/ubuntu/ccapp/.env` and recreate app container |
| Docker image | Expected Docker Hub image/tag | GitHub Actions logs and EC2 `docker images` | Active container uses intended image | Check GitHub variables/secrets and Docker Hub push |

## Go/No-Go Checklist

| Check Item | How To Check | Success Criteria | Failure Location |
| --- | --- | --- | --- |
| Health endpoint | `curl -i https://api.seeatheater.store/actuator/health` | HTTP 200 and health status `UP` | App logs, DB, Redis, Docker healthcheck |
| Docker containers | `docker compose ps` on EC2 | Active app container and `redis` are `running` or `healthy` | `docker logs <container>` |
| Nginx reverse proxy | `curl -i https://api.seeatheater.store/actuator/health` and `sudo nginx -t` | No 502/504, Nginx config valid | `/var/log/nginx/error.log`, upstream port |
| Blue/green active target | Inspect Nginx upstream and app ports | Nginx points to one healthy app container | `switch-blue-green.sh`, `rollback.sh`, Nginx config |
| Public/private smoke test | `./scripts/dev-smoke-test.sh https://api.seeatheater.store` | All checks pass | `docs/PUBLIC_ENDPOINT_BASELINE.md`, SecurityConfig, app logs |
| FE API target | Browser devtools Network | FE calls API domain, not localhost or stale IP | FE env and Vercel/deploy config |
| CORS | Browser request from FE origin | No browser CORS failure | `CORS_ALLOWED_ORIGINS`, SecurityConfig, Nginx |
| Kakao Developers domains | Kakao Developers console | `https://seeatheater.store` and `https://api.seeatheater.store` are registered as needed | Kakao console platform/domain settings |
| KakaoPay callback URLs | KakaoPay env and app logs | approve/cancel/fail point to backend API domain | `KAKAOPAY_APPROVE_URL`, `KAKAOPAY_CANCEL_URL`, `KAKAOPAY_FAIL_URL` |
| KakaoPay ready | Authenticated user clicks payment | Response returns payment redirect URL | KakaoPay logs, callback domain, request payload |
| KakaoPay approve/cancel/fail | Return from KakaoPay flow | Callback reaches backend and redirects/returns expected state | App logs for `/kakaoPay/approve`, `/cancel`, `/fail` |
| General user login | FE login flow | Access token issued and protected pages load | Auth logs, browser storage, Redis refresh token |
| Reservation flow | Login -> show detail -> ticket select -> reserve | TempTicket is created and ready can be called | `temp_ticket`, stock in `amateur_rounds` |
| My page ticket view | After reservation/payment | Ticket state appears correctly | MyTicket API logs, `real_ticket` |
| Performer show registration | Performer account creates show | Show is created in waiting/expected status | Performer API logs, S3 upload, `amateur_show` |
| Admin approval | Admin approves/rejects waiting show | Approved show appears publicly; rejected show stays hidden | Admin API logs, `amateur_show.approval_status` |
| Access token refresh | Expired access token with valid refresh | Refresh succeeds and user stays logged in | `/auth/refresh`, Redis, FE interceptor |
| Refresh failure | Invalid/missing refresh token | Client clears session and returns to login | Browser storage, auth logs |
| Logout | User clicks logout | Client session is cleared even if API fails | FE auth state, `/auth/logout`, Redis |
| QA/demo data cleanup | Check known prefixes | No stale broken QA data is public | SELECT-only checks, cleanup SQL reviewed before execution |
| Rollback readiness | Review scripts and active container | `rollback.sh` and previous container/image are available | `/tmp/rollback.log`, Docker image list |

## Required Commands

```bash
# Public smoke baseline
./scripts/dev-smoke-test.sh https://api.seeatheater.store

# EC2 container status
cd /home/ubuntu/ccapp
docker compose ps

docker logs --tail=200 ccapp-blue
docker logs --tail=200 ccapp-green
docker logs --tail=200 redis

# Nginx
sudo nginx -t
sudo tail -n 100 /var/log/nginx/error.log
sudo tail -n 100 /var/log/nginx/access.log

# Health
curl -i https://api.seeatheater.store/actuator/health
```

## SELECT-Only DB Checks

Use the dev/staging database only. Do not run against production.

```sql
SELECT id, name, approval_status, status, `start`, `end`
FROM amateur_show
WHERE approval_status = 'APPROVED'
ORDER BY id DESC;

SELECT amateur_show_id, round_number, performance_date_time, total_ticket
FROM amateur_rounds
WHERE performance_date_time >= NOW()
ORDER BY amateur_show_id, performance_date_time;

SELECT reservation_status, COUNT(*)
FROM temp_ticket
GROUP BY reservation_status;

SELECT reservation_status, COUNT(*)
FROM real_ticket
GROUP BY reservation_status;

SELECT kakao_tid, COUNT(*) AS cnt
FROM real_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;
```

## Go/No-Go Decision

Release can proceed only if:

- `/actuator/health` is `UP`.
- Smoke test passes.
- Active app container is healthy behind Nginx.
- FE calls the correct API URL.
- Login, reserve, KakaoPay ready, and admin approval are manually verified at least once.
- Duplicate `/kakaoPay/ready` on the same TempTicket is verified not to double-decrease stock.
- Duplicate KakaoPay approve is verified not to create more than one RealTicket for the same `kakao_tid`.
- Kakao Developers and KakaoPay callback domains match the deployed URLs.
- No known P0 risk in `docs/PAYMENT_AUTH_RISK_REGISTER.md` remains unverified.

Release should stop if:

- Health is DOWN or Nginx returns 502/504.
- KakaoPay ready fails repeatedly.
- Login/refresh fails for normal users.
- Reservation creates inconsistent TempTicket/stock state.
- Admin cannot approve shows or non-admin can access admin APIs.
