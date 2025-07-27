#!/bin/bash
# 🔙 자동 롤백 스크립트

set -e

NGINX_CONF="/etc/nginx/sites-available/default"
ROLLBACK_LOG="/tmp/rollback.log"

log() {
    local message="[$(date '+%H:%M:%S')] $1"
    echo "$message"
    echo "$message" >> "$ROLLBACK_LOG"
}

# 긴급 롤백 함수
emergency_rollback() {
    log "🚨 긴급 롤백 시작!"

    # Nginx 설정 백업 복구
    if [ -f "${NGINX_CONF}.backup" ]; then
        log "📋 Nginx 설정 백업 복구 중..."
        sudo cp "${NGINX_CONF}.backup" "$NGINX_CONF"

        if sudo nginx -t; then
            sudo systemctl reload nginx
            log "✅ Nginx 설정 복구 완료"
        else
            log "❌ Nginx 설정 복구 실패"
            # 안전한 기본 설정으로 복구
            restore_safe_config
        fi
    else
        log "⚠️ Nginx 백업 파일이 없습니다. 안전한 설정으로 복구합니다."
        restore_safe_config
    fi
}

# 안전한 설정 복구
restore_safe_config() {
    log "🛡️ 안전한 기본 설정으로 복구 중..."

    # 두 환경 모두 활성화 (부하분산)
    sudo sed -i 's/server localhost:8081 down;/server localhost:8081;/' $NGINX_CONF
    sudo sed -i 's/server localhost:8082 down;/server localhost:8082;/' $NGINX_CONF

    if sudo nginx -t; then
        sudo systemctl reload nginx
        log "✅ 안전한 설정 적용 완료"
    else
        log "❌ 치명적 오류: Nginx 설정 복구 불가"
        exit 1
    fi
}

# 메인 롤백 로직
main() {
    log "🔙 롤백 프로세스 시작"

    # 배포 상태 파일이 있는지 확인
    if [ ! -f "/tmp/deployment_state" ]; then
        log "⚠️ 배포 상태 파일을 찾을 수 없습니다. 긴급 롤백을 수행합니다."
        emergency_rollback
        exit 0
    fi

    # 배포 상태 로드
    source /tmp/deployment_state

    log "🔍 롤백 대상: $TARGET_ENV → $CURRENT_ENV"

    # 실패한 컨테이너 중지
    log "🛑 실패한 $TARGET_ENV 컨테이너 중지 중..."
    docker stop $TARGET_SERVICE 2>/dev/null || true
    docker compose rm -f $TARGET_SERVICE 2>/dev/null || true

    # 이전 환경 재시작 (만약 중지되었다면)
    log "🔄 이전 $CURRENT_ENV 환경 복구 중..."

    if [ "$CURRENT_ENV" = "BLUE" ]; then
        CURRENT_PORT="8081"
        CURRENT_SERVICE="app-blue"
    else
        CURRENT_PORT="8082"
        CURRENT_SERVICE="app-green"
    fi

    # 이전 환경이 실행 중인지 확인
    if ! docker ps | grep -q $CURRENT_SERVICE; then
        log "🚀 이전 $CURRENT_ENV 환경 재시작 중..."
        docker compose up -d $CURRENT_SERVICE

        # 이전 환경 헬스체크
        log "🏥 이전 $CURRENT_ENV 환경 헬스체크..."
        if ./health-check.sh "$CURRENT_PORT" "$CURRENT_ENV" 20 2; then
            log "✅ 이전 $CURRENT_ENV 환경 정상 복구"
        else
            log "❌ 이전 $CURRENT_ENV 환경 복구 실패"
            # 두 환경 모두 문제가 있을 수 있으므로 긴급 조치
            emergency_rollback
            exit 1
        fi
    else
        log "✅ 이전 $CURRENT_ENV 환경이 이미 실행 중입니다"
    fi

    # Nginx 설정 롤백
    log "🔄 Nginx 트래픽 설정 롤백 중..."
    if [ "$CURRENT_ENV" = "BLUE" ]; then
        # Green에서 Blue로 롤백
        sudo sed -i 's/server localhost:8082;/server localhost:8082 down;/' $NGINX_CONF
        sudo sed -i 's/server localhost:8081 down;/server localhost:8081;/' $NGINX_CONF
    else
        # Blue에서 Green으로 롤백
        sudo sed -i 's/server localhost:8081;/server localhost:8081 down;/' $NGINX_CONF
        sudo sed -i 's/server localhost:8082 down;/server localhost:8082;/' $NGINX_CONF
    fi

    # Nginx 설정 적용
    if sudo nginx -t; then
        sudo systemctl reload nginx
        log "✅ Nginx 설정 롤백 완료"
    else
        log "❌ Nginx 설정 롤백 실패 - 긴급 복구 시도"
        emergency_rollback
        exit 1
    fi

    # 롤백 후 최종 검증
    log "🔍 롤백 완료 검증 중..."
    if ./health-check.sh "$CURRENT_PORT" "$CURRENT_ENV" 10 2; then
        log "✅ 롤백 성공! $CURRENT_ENV 환경으로 복구되었습니다."

        # 정리 작업
        sudo rm -f "${NGINX_CONF}.backup"
        rm -f /tmp/deployment_state

        # 실패한 이미지 정리
        log "🧹 실패한 배포 정리 중..."
        docker image prune -f

        log "🎉 롤백 프로세스 완료!"
    else
        log "❌ 롤백 검증 실패 - 시스템 상태를 확인해주세요"
        exit 1
    fi
}

# 스크립트 실행
main "$@"
