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

## Deployment Steps

1. Prepare EC2 with Docker, Docker Compose, and optional Nginx.
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
