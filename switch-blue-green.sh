#!/bin/bash
# 🔄 개선된 블루그린 배포 스크립트

set -e

NGINX_CONF="/etc/nginx/sites-available/default"

# 📝 로깅 함수
log() {
    echo "[$(date '+%H:%M:%S')] $1"
}

log "🚀 블루그린 배포 시작"

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

log "🎯 $CURRENT_ENV → $TARGET_ENV 전환 시작"

# 📦 새 환경 배포
log "📦 $TARGET_ENV 컨테이너 준비 중..."

# 기존의 target 컨테이너 제거
log "   🗑️ 기존 $TARGET_ENV 컨테이너 제거 중..."
docker compose rm -f $TARGET_SERVICE 2>/dev/null || true

docker compose pull $TARGET_SERVICE

#새 target 컨테이너 생성
log "   🚀 새 $TARGET_ENV 컨테이너 시작 중..."
docker compose up -d --force-recreate $TARGET_SERVICE

# 🏥 헬스체크
log "🏥 $TARGET_ENV 헬스체크 진행 중..."
for i in {1..30}; do
    printf "   ⏳ 시도 %d/30...\r" $i
    sleep 3
    
    if curl -f http://localhost:$TARGET_PORT/actuator/health > /dev/null 2>&1; then
        echo ""
        log "✅ $TARGET_ENV 환경 헬스체크 성공!"
        break
    fi
    
    if [ $i -eq 30 ]; then
        echo ""
        log "❌ $TARGET_ENV 헬스체크 실패 - 배포 중단"
        exit 1
    fi
done

# 🔄 트래픽 전환
log "🔄 트래픽을 $TARGET_ENV로 전환 중..."
if [ "$TARGET_ENV" = "GREEN" ]; then
    # Blue → Green 전환
    sudo sed -i 's/server localhost:8081;/server localhost:8081 down;/' $NGINX_CONF
    sudo sed -i 's/server localhost:8082 down;/server localhost:8082;/' $NGINX_CONF
else
    # Green → Blue 전환
    sudo sed -i 's/server localhost:8082;/server localhost:8082 down;/' $NGINX_CONF
    sudo sed -i 's/server localhost:8081 down;/server localhost:8081;/' $NGINX_CONF
fi

# ⚙️ Nginx 설정 적용
log "⚙️  Nginx 설정 적용 중..."
sudo nginx -t && sudo systemctl reload nginx

# 🛑 current 컨테이너 정리
log "🛑 $CURRENT_ENV 컨테이너 정리 중..."


# 먼저 docker compose로 시도
log "   📦 Docker Compose로 $STOP_SERVICE 중지 시도..."
if docker compose stop $STOP_SERVICE; then
    log "   ✅ Docker Compose로 성공적으로 중지됨"
else
    log "   ⚠️  Docker Compose 중지 실패, 직접 컨테이너 중지 시도..."

    # 컨테이너가 실행 중인지 확인
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

log "✨ 블루그린 배포 성공!"
