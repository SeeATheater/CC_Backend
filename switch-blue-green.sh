#!/bin/bash
# 🔄 블루그린 배포 스크립트

set -e

NGINX_CONF="/etc/nginx/sites-available/default"
COMPOSE_PROJECT_NAME="ccapp"

# 로깅 함수
log() {
    echo "[$(date '+%H:%M:%S')] $1"
}

# 롤백 함수
rollback() {
    local reason=$1
    log "🚨 롤백 실행: $reason"

    # 실패한 컨테이너 정리
    docker compose --project-name $COMPOSE_PROJECT_NAME down --remove-orphans --timeout 30

    # 이전 상태로 복구
    if [ "$CURRENT_ENV" = "BLUE" ]; then
        docker compose --project-name $COMPOSE_PROJECT_NAME up -d app-blue
    else
        docker compose --project-name $COMPOSE_PROJECT_NAME up -d app-green
    fi

    exit 1
}

# 헬스체크
health_check() {
    local port=$1
    local max_attempts=30

    log "🏥 포트 $port 헬스체크 시작..."

    for attempt in $(seq 1 $max_attempts); do
        printf "   ⏳ 시도 %d/%d...\r" $attempt $max_attempts

        # 다층 헬스체크
        if curl -f --connect-timeout 5 --max-time 10 http://localhost:$port/actuator/health > /dev/null 2>&1; then
            # 추가 안정성 확인
            sleep 2
            if curl -f --connect-timeout 5 --max-time 10 http://localhost:$port/actuator/health > /dev/null 2>&1; then
                echo ""
                log "✅ 헬스체크 성공!"
                return 0
            fi
        fi

        sleep 3
    done

    echo ""
    log "❌ 헬스체크 실패"
    return 1
}


# 컨테이너 정리
cleanup_container() {
    local service=$1
    log "🛑 $service 컨테이너 완전 정리 중..."

    # 1단계: Graceful shutdown (SIGTERM)
    docker compose --project-name $COMPOSE_PROJECT_NAME stop $service --timeout 30 2>/dev/null || true

    # 2단계: 완전 제거
    docker compose --project-name $COMPOSE_PROJECT_NAME rm -f $service 2>/dev/null || true

    # 3단계: 혹시 남은 컨테이너 직접 정리 (fallback)
    local container_pattern="ccapp.*$service"
    local remaining_containers=$(docker ps -aq --filter "name=$container_pattern" 2>/dev/null || true)

    if [ -n "$remaining_containers" ]; then
        log "   ⚠️ 남은 컨테이너 발견, 직접 정리 중..."
        echo "$remaining_containers" | xargs -r docker stop --time 30 2>/dev/null || true
        echo "$remaining_containers" | xargs -r docker rm -f 2>/dev/null || true
    fi
}

log "🚀 블루그린 배포 시작"

# 🔍 현재 활성 환경 정확히 감지
BLUE_DOWN=$(grep -c "server localhost:8081 down;" $NGINX_CONF || echo "0")
GREEN_DOWN=$(grep -c "server localhost:8082 down;" $NGINX_CONF || echo "0")

log "🔍 현재 상태 - Blue down: $BLUE_DOWN, Green down: $GREEN_DOWN"

if [ "$BLUE_DOWN" -eq "1" ] && [ "$GREEN_DOWN" -eq "0" ]; then
    # Green 활성 → Blue로 전환
    CURRENT_ENV="GREEN"
    TARGET_ENV="BLUE"
    TARGET_PORT="8081"
    TARGET_SERVICE="app-blue"
    STOP_SERVICE="app-green"
    log "🟢 GREEN → 🔵 BLUE"
    
elif [ "$GREEN_DOWN" -eq "1"  ] && [ "$BLUE_DOWN" -eq "0" ]; then
    # Blue 활성 → Green으로 전환
    CURRENT_ENV="BLUE"
    TARGET_ENV="GREEN"
    TARGET_PORT="8082"
    TARGET_SERVICE="app-green"
    STOP_SERVICE="app-blue"
    log "🔵 BLUE → 🟢 GREEN"
    
else
    # 초기 상태 또는 둘 다 활성 → Green으로 전환
    CURRENT_ENV="BLUE"
    TARGET_ENV="GREEN"
    TARGET_PORT="8082"
    TARGET_SERVICE="app-green"
    STOP_SERVICE="app-blue"
    log "🔧 초기 상태 감지 → 🟢 GREEN으로 전환"

    docker compose --project-name $COMPOSE_PROJECT_NAME down --remove-orphans --timeout 30
fi

log "🎯 $CURRENT_ENV → $TARGET_ENV 전환 시작"

# 📦 새 환경 배포
log "📦 $TARGET_ENV 컨테이너 준비 중..."

# 기존의 target 컨테이너 제거
cleanup_container $TARGET_SERVICE

#최신 이미지 pull
docker compose --project-name $COMPOSE_PROJECT_NAME pull $TARGET_SERVICE

#새 target 컨테이너 생성
log "   🚀 새 $TARGET_ENV 컨테이너 시작 중..."
docker compose --project-name $COMPOSE_PROJECT_NAME up -d --force-recreate $TARGET_SERVICE

# 🏥 헬스체크
if ! health_check $TARGET_PORT; then
    rollback "헬스체크 실패"
fi

# 🔄 트래픽 전환
log "🔄 트래픽을 $TARGET_ENV로 전환 중..."

# 백업 생성
sudo cp $NGINX_CONF $NGINX_CONF.backup

if [ "$TARGET_ENV" = "GREEN" ]; then
    # Blue → Green 전환
    sudo sed -i \
        -e 's/server localhost:8081;/server localhost:8081 down;/' \
        -e 's/server localhost:8082 down;/server localhost:8082;/' \
        $NGINX_CONF
else
    # Green → Blue 전환
    sudo sed -i \
        -e 's/server localhost:8082;/server localhost:8082 down;/' \
        -e 's/server localhost:8081 down;/server localhost:8081;/' \
        $NGINX_CONF
fi

# Nginx 설정 적용
log "⚙️ Nginx 설정 적용 중..."
if sudo nginx -t; then
    sudo systemctl reload nginx
    log "✅ Nginx 설정 적용 완료"
else
    log "❌ Nginx 설정 오류, 롤백"
    sudo cp $NGINX_CONF.backup $NGINX_CONF
    rollback "Nginx 설정 오류"
fi


# 🛑 current 컨테이너 정리
if [ "$CURRENT_ENV" != "UNKNOWN" ]; then
    log "🛑 이전 환경($CURRENT_ENV) 정리 중..."
    cleanup_container $STOP_SERVICE
fi


log "🔍 최종 컨테이너 상태 확인..."
log "🔍 최종 컨테이너 상태 확인..."
RUNNING_CONTAINERS=$(docker compose --project-name $COMPOSE_PROJECT_NAME ps --format "table" | grep -E "(app-blue|app-green)" || echo "없음")
log "$RUNNING_CONTAINERS"

# 실행 중인 ccapp 컨테이너 개수 확인
CONTAINER_COUNT=$(docker ps --filter "name=ccapp" --format "{{.Names}}" | wc -l)
log "📊 실행 중인 ccapp 컨테이너 수: $CONTAINER_COUNT"

if [ "$CONTAINER_COUNT" -ne 1 ]; then
    log "⚠️ 경고: 예상과 다른 컨테이너 수 ($CONTAINER_COUNT개)"
    # 비상 정리
    docker compose --project-name $COMPOSE_PROJECT_NAME down --remove-orphans --timeout 30
    docker compose --project-name $COMPOSE_PROJECT_NAME up -d $TARGET_SERVICE
fi


# 🧹 정리
log "🧹 미사용 이미지 정리 중..."
docker image prune -f
docker container prune -f

# 백업 파일 정리
sudo rm -f $NGINX_CONF.backup

log "🎉 $TARGET_ENV 환경 전환 완료!"
log "✨ 블루그린 배포 성공!"
