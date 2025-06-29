#!/bin/bash
# switch-blue-green.sh

set -e

NGINX_CONF="/etc/nginx/sites-available/default"

# 현재 활성 환경 확인
IS_GREEN=$(grep -c "server localhost:8081 down;" $NGINX_CONF || echo "0")

if [ "$IS_GREEN" -eq "0" ]; then
    echo "### BLUE => GREEN 전환 ###"

    echo "1. Green 이미지 pull 및 컨테이너 시작"
    docker compose pull app-green
    docker compose up -d app-green

    echo "2. Green 환경 헬스체크"
    for i in {1..30}; do
        echo "헬스체크 시도 $i/30..."
        sleep 3

        if curl -f http://localhost:8082/actuator/health > /dev/null 2>&1; then
            echo "Green 환경 헬스체크 성공"
            break
        fi

        if [ $i -eq 30 ]; then
            echo "Green 환경 헬스체크 실패"
            exit 1
        fi
    done

    echo "3. Nginx 설정을 Green으로 변경"
    sudo sed -i 's/server localhost:8081;/server localhost:8081 down;/' $NGINX_CONF
    sudo sed -i 's/server localhost:8082 down;/server localhost:8082;/' $NGINX_CONF

    echo "4. Nginx 설정 테스트 및 재로드"
    sudo nginx -t && sudo systemctl reload nginx

    echo "5. Blue 컨테이너 중지"
    docker compose stop app-blue

    echo "### GREEN 환경으로 전환 완료 ###"

else
    echo "### GREEN => BLUE 전환 ###"

    echo "1. Blue 이미지 pull 및 컨테이너 시작"
    docker compose pull app-blue
    docker compose up -d app-blue

    echo "2. Blue 환경 헬스체크"
    for i in {1..30}; do
        echo "헬스체크 시도 $i/30..."
        sleep 3

        if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo "Blue 환경 헬스체크 성공"
            break
        fi

        if [ $i -eq 30 ]; then
            echo "Blue 환경 헬스체크 실패"
            exit 1
        fi
    done

    echo "3. Nginx 설정을 Blue로 변경"
    sudo sed -i 's/server localhost:8082;/server localhost:8082 down;/' $NGINX_CONF
    sudo sed -i 's/server localhost:8081 down;/server localhost:8081;/' $NGINX_CONF

    echo "4. Nginx 설정 테스트 및 재로드"
    sudo nginx -t && sudo systemctl reload nginx

    echo "5. Green 컨테이너 중지"
    docker compose stop app-green

    echo "### BLUE 환경으로 전환 완료 ###"
fi

echo "6. 사용하지 않는 이미지 정리"
docker image prune -f
