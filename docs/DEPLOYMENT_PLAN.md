# Dev/Staging Deployment Plan

This repository currently keeps the existing deployment path:

GitHub Actions -> Docker Hub -> EC2 -> Docker Compose

The first redeployment target is a new AWS dev/staging environment, not the final production setup. ECR, OIDC, ECS/Fargate, and a redesigned blue/green strategy are intentionally left for later PRs.

## Minimum AWS Resources

- EC2 instance with Docker and Docker Compose
- RDS MySQL-compatible database
- Redis from `docker-compose.yml` for the first dev/staging deployment
- S3 bucket for presigned URL uploads
- Security groups for HTTP/HTTPS, SSH, EC2-to-RDS, and S3 access
- Docker Hub repository for the application image

## GitHub Configuration

Required GitHub Secrets:

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `HOST`
- `PRIVATE_KEY`

Optional GitHub Variables:

- `DOCKER_IMAGE` defaults to `ccapp`
- `DOCKER_TAG` defaults to `latest`
- `DEPLOY_USER` defaults to `ubuntu`
- `DEPLOY_PATH` defaults to `/home/ubuntu/ccapp`

The workflow still runs on `develop` push and pull request. `workflow_dispatch` is also available for manual dev/staging redeploys.

## EC2 Runtime Configuration

Create `/home/ubuntu/ccapp/.env` from `.env.example` and fill it with the new AWS dev/staging values. Do not commit real secret values.

The `APP_IMAGE` value in `.env` should match the image pushed by GitHub Actions:

```env
APP_IMAGE=your-dockerhub-username/ccapp:latest
```

For the first dev/staging deployment, use:

```env
SPRING_PROFILES_ACTIVE=dev
REDIS_HOST=redis
```

The application containers are sized for the current dev/staging EC2 Docker Compose setup:

- `app-blue` and `app-green` use `mem_limit: 800m`.
- `JAVA_TOOL_OPTIONS` sets `-Xms256m -Xmx512m`.
- `redis` is only reachable on the Docker Compose internal network. Do not open Redis to the public internet.
- Docker logs use the `json-file` driver with rotation to avoid unbounded disk growth.

The `800m` app memory limit is intentional. During the first AWS dev deployment, the app repeatedly exited with code `137` under `400m` after Spring Boot startup. Keep EC2 instance sizing aligned with running one active app container, one standby/target app container during blue/green deployment, and Redis.

## GitHub Actions Deployment

The workflow keeps the existing deployment path:

```text
GitHub Actions -> Docker Hub -> EC2 -> Docker Compose -> Nginx
```

Current behavior:

- `push` to `develop` runs build, Docker Hub push, and EC2 deploy.
- `pull_request` to `develop` runs the build path but skips Docker push/deploy.
- `workflow_dispatch` is available for manual dev/staging redeploys.
- Docker image and tag are configurable with GitHub Variables.
- Docker Hub credentials and EC2 SSH credentials are read from GitHub Secrets.

Keep these as follow-up improvements, not part of the first dev/staging stabilization:

- Use immutable commit SHA Docker tags instead of `latest`.
- Run tests in CI instead of `./gradlew build -x test`.
- Move from Docker Hub secrets to ECR/OIDC later.

## Nginx Blue/Green Setup

`switch-blue-green.sh` and `rollback.sh` expect the Nginx upstream entries below in `/etc/nginx/sites-available/default`.

```nginx
upstream ccapp_backend {
    server localhost:8081;
    server localhost:8082 down;
}

server {
    listen 80 default_server;
    listen [::]:80 default_server;

    server_name _;

    location / {
        proxy_pass http://ccapp_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Validate Nginx on EC2:

```bash
sudo nginx -t
sudo systemctl reload nginx
curl -i http://localhost/actuator/health
```

Check blue/green state:

```bash
grep -n 'server localhost:8081\|server localhost:8082' /etc/nginx/sites-available/default
docker compose ps
docker logs ccapp-blue --tail=100
docker logs ccapp-green --tail=100
```

## AWS Security Group Checklist

EC2 inbound rules:

- `22`: allow only trusted operator IPs.
- `80`: public for dev/staging HTTP access.
- `443`: public only after HTTPS is configured.
- `8081` and `8082`: do not expose publicly; Nginx proxies to these ports locally.
- `6379`: do not expose publicly; Redis is internal to Docker Compose.

RDS inbound rules:

- `3306`: source must be the EC2 security group only.

S3 access:

- S3 is controlled by IAM policy, not security groups.
- Keep bucket public access block enabled.
- Grant only the actions needed for presigned upload/read/delete flows.

Do not write real IPs, passwords, access keys, or secret values into this repository.

## Smoke Test Checklist

Run these after deployment:

```bash
curl -i http://localhost/actuator/health
curl -i http://localhost:8081/actuator/health
docker compose ps
docker exec redis redis-cli ping
```

From a local machine, use the public dev/staging base URL:

```bash
curl -i http://<dev-api-host>/actuator/health
curl -i http://<dev-api-host>/amateurs/ranking
curl -i -X POST "http://<dev-api-host>/kakaoPay/ready?tempTicketId=1"
```

Expected results:

- `/actuator/health` returns `200` and shows DB/Redis as `UP`.
- `/swagger-ui/index.html` opens through Nginx.
- Redis responds with `PONG` from inside EC2.
- S3 presigned URL APIs work with an authenticated user and valid S3 IAM credentials.
- Public read APIs return successful responses or validation errors, not network failures.
- Admin login works with the configured initial admin password.
- `POST /kakaoPay/ready` is blocked for unauthenticated requests. Depending on Spring Security handling, this may be `401` or `403`; the important behavior is that unauthenticated access is not allowed.
- `GET /kakaoPay/approve`, `/kakaoPay/cancel`, and `/kakaoPay/fail` remain public callback endpoints.

## Deployment Steps

1. Prepare EC2 with Docker, Docker Compose, and Nginx. Nginx is required for this dev/staging deployment because public traffic enters through port `80` and is reverse-proxied to the active blue/green app container.
2. Create `/home/ubuntu/ccapp`.
3. Add `.env` on EC2 using `.env.example` as the template.
4. Configure GitHub Secrets and optional Variables.
5. Push to `develop` or run the workflow manually.
6. Verify health with `/actuator/health`.

## Follow-up Work

- Move Docker registry from Docker Hub to ECR.
- Use GitHub OIDC instead of long-lived AWS keys.
- Revisit blue/green deployment after dev/staging is stable.
- Move runtime secrets to AWS Systems Manager Parameter Store or Secrets Manager.
- Replace `ddl-auto=update` with a managed migration strategy before production.
- Add HTTPS/domain routing before production OAuth and KakaoPay verification.
