#!/bin/bash
# 🔄 블루그린 배포 스크립트 (자동 롤백 지원)

set -e

NGINX_CONF="/etc/nginx/sites-available/default"
DEPLOYMENT_LOG="/tmp/deployment.log"

# 📝 로깅 함수
log() {
    local message="[$(date '+%H:%M:%S')] $1"
    echo "$message"
    echo "$message" >> "$DEPLOYMENT_LOG"
}

# 🏥 헬스체크 함수 호출
health_check() {
    local port=$1
    local env_name=$2

    log "🏥 $env_name 헬스체크 진행 중..."

    if ./health-check.sh "$port" "$env_name"; then
        log "✅ $env_name 환경 헬스체크 성공!"
        return 0
    else
        log "❌ $env_name 헬스체크 실패"
        return 1
    fi
}

# 🔄 트래픽 전환 함수
switch_traffic() {
    local target_env=$1

    log "🔄 트래픽을 $target_env로 전환 중..."

    if [ "$target_env" = "GREEN" ]; then
        # Blue → Green 전환
        sudo sed -i 's/server localhost:8081;/server localhost:8081 down;/' $NGINX_CONF
        sudo sed -i 's/server localhost:8082 down;/server localhost:8082;/' $NGINX_CONF
    else
        # Green → Blue 전환
        sudo sed -i 's/server localhost:8082;/server localhost:8082 down;/' $NGINX_CONF
        sudo sed -i 's/server localhost:8081 down;/server localhost:8081;/' $NGINX_CONF
    fi

    # Nginx 설정 검증 및 적용
    if sudo nginx -t; then
        sudo systemctl reload nginx
        log "✅ Nginx 설정 적용 완료"
        return 0
    else
        log "❌ Nginx 설정 검증 실패"
        return 1
    fi
}

# 🚀 메인 배포 로직
main() {
    log "🚀 블루그린 배포 시작"

    # 현재 상태 파악을 위한 백업 생성
    sudo cp "$NGINX_CONF" "${NGINX_CONF}.backup"

    # 🔍 현재 활성 환경 정확히 감지
    BLUE_DOWN=$(grep -c "server localhost:8081 down;" $NGINX_CONF || echo "0")
    GREEN_DOWN=$(grep -c "server localhost:8082 down;" $NGINX_CONF || echo "0")

    log "🔍 현재 상태 - Blue down: $BLUE_DOWN, Green down: $GREEN_DOWN"

    if [ "$BLUE_DOWN" -eq "1" ]; then
        # Blue가 down 상태 = Green이 활성 → Blue로 전환
        CURRENT_ENV="GREEN"
        TARGET_ENV="BLUE"
        TARGET_PORT="8081"
        TARGET_SERVICE="app-blue"
        STOP_SERVICE="app-green"
        STOP_CONTAINER="ccapp-green"
        log "🟢 GREEN → 🔵 BLUE"

    elif [ "$GREEN_DOWN" -eq "1" ]; then
        # Green이 down 상태 = Blue가 활성 → Green으로 전환
        CURRENT_ENV="BLUE"
        TARGET_ENV="GREEN"
        TARGET_PORT="8082"
        TARGET_SERVICE="app-green"
        STOP_SERVICE="app-blue"
        STOP_CONTAINER="ccapp-blue"
        log "🔵 BLUE → 🟢 GREEN"

    else
        # 초기 상태 또는 둘 다 활성 → Green으로 전환
        CURRENT_ENV="BLUE"
        TARGET_ENV="GREEN"
        TARGET_PORT="8082"
        TARGET_SERVICE="app-green"
        STOP_SERVICE="app-blue"
        STOP_CONTAINER="ccapp-blue"
        log "🔧 초기 상태 감지 → 🟢 GREEN으로 전환"
    fi

    # 배포 상태 저장 (롤백용)
    echo "CURRENT_ENV=$CURRENT_ENV" > /tmp/deployment_state
    echo "TARGET_ENV=$TARGET_ENV" >> /tmp/deployment_state
    echo "TARGET_PORT=$TARGET_PORT" >> /tmp/deployment_state
    echo "TARGET_SERVICE=$TARGET_SERVICE" >> /tmp/deployment_state
    echo "STOP_SERVICE=$STOP_SERVICE" >> /tmp/deployment_state
    echo "STOP_CONTAINER=$STOP_CONTAINER" >> /tmp/deployment_state

    log "🎯 $CURRENT_ENV → $TARGET_ENV 전환 시작"

    # 📦 새 환경 배포
    log "📦 $TARGET_ENV 컨테이너 준비 중..."

    # 기존의 target 컨테이너 제거
    log "   🗑️ 기존 $TARGET_ENV 컨테이너 제거 중..."
    docker stop $TARGET_SERVICE 2>/dev/null || true
    docker compose rm -f $TARGET_SERVICE 2>/dev/null || true

    docker compose pull $TARGET_SERVICE

    # 새 target 컨테이너 생성
    log "   🚀 새 $TARGET_ENV 컨테이너 시작 중..."
    if ! docker compose up -d --force-recreate $TARGET_SERVICE; then
        log "❌ $TARGET_ENV 컨테이너 시작 실패 - 롤백 시작"
        ./rollback.sh
        exit 1
    fi

    # 🏥 헬스체크
    if ! health_check "$TARGET_PORT" "$TARGET_ENV"; then
        log "❌ 헬스체크 실패 - 자동 롤백 시작"
        ./rollback.sh
        exit 1
    fi

    # 🔄 트래픽 전환
    if ! switch_traffic "$TARGET_ENV"; then
        log "❌ 트래픽 전환 실패 - 자동 롤백 시작"
        ./rollback.sh
        exit 1
    fi

    # 전환 후 최종 헬스체크
    sleep 5
    if ! health_check "$TARGET_PORT" "$TARGET_ENV"; then
        log "❌ 전환 후 헬스체크 실패 - 자동 롤백 시작"
        ./rollback.sh
        exit 1
    fi

    # 🛑 기존 환경 정리
    log "🛑 $CURRENT_ENV 컨테이너 정리 중..."

    # 먼저 docker compose로 시도
    log "   📦 Docker Compose로 $STOP_SERVICE 중지 시도..."
    if docker compose stop $STOP_SERVICE; then
        log "   ✅ Docker Compose로 성공적으로 중지됨"
    else
        log "   ⚠️  Docker Compose 중지 실패, 직접 컨테이너 중지 시도..."

        if docker ps --format "table {{.Names}}" | grep -q "^$STOP_CONTAINER$"; then
            log "   🔍 $STOP_CONTAINER 컨테이너 발견, 직접 중지 중..."
            if docker stop $STOP_CONTAINER; then
                log "   ✅ $STOP_CONTAINER 컨테이너 성공적으로 중지됨"
            else
                log "   ❌ $STOP_CONTAINER 컨테이너 중지 실패"
            fi
        else
            log "   ℹ️  $STOP_CONTAINER 컨테이너가 이미 중지되어 있음"
        fi
    fi

    log "🔍 최종 컨테이너 상태 확인..."
    RUNNING_CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep ccapp || echo "없음")
    log "   실행 중인 ccapp 컨테이너: $RUNNING_CONTAINERS"

    log "🎉 $TARGET_ENV 환경 전환 완료!"

    # 🧹 정리
    log "🧹 미사용 이미지 정리 중..."
    docker image prune -f

    # 백업 파일 정리
    sudo rm -f "${NGINX_CONF}.backup"
    rm -f /tmp/deployment_state

    log "✨ 블루그린 배포 성공!"
}

# 스크립트 실행
main "$@"
