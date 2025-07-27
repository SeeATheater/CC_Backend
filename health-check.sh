#!/bin/bash
# 🏥 헬스체크 스크립트 (간소화 버전)

set -e

PORT=${1:-8080}
ENV_NAME=${2:-"APP"}
MAX_ATTEMPTS=${3:-30}
SLEEP_INTERVAL=${4:-3}

log() {
    echo "[$(date '+%H:%M:%S')] $1"
}

# 기본 헬스체크 함수
check_health() {
    local port=$1
    local attempts=$2
    local interval=$3

    for i in $(seq 1 $attempts); do
        printf "   ⏳ 시도 %d/%d...\r" $i $attempts
        sleep $interval

        # HTTP 헬스체크
        if curl -f -s --max-time 10 "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
            echo ""
            return 0
        fi

        # 마지막 시도에서 실패한 경우
        if [ $i -eq $attempts ]; then
            echo ""
            return 1
        fi
    done
}

# 메인 실행
main() {
    log "🏥 $ENV_NAME 헬스체크 시작 (포트: $PORT)"

    if check_health "$PORT" "$MAX_ATTEMPTS" "$SLEEP_INTERVAL"; then
        log "✅ $ENV_NAME 헬스체크 완료!"
        exit 0
    else
        log "❌ $ENV_NAME 헬스체크 실패"
        exit 1
    fi
}

# 스크립트가 직접 실행될 때만 main 함수 호출
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
