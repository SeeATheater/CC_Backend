#!/bin/bash

# 1. 실행 중인 Spring Boot 프로세스 종료
PID=$(ps -ef | grep 'backend-0.0.1-SNAPSHOT.jar' | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
  echo "[INFO] 기존 프로세스 종료 (PID: $PID)"
  kill -9 $PID
  sleep 2
else
  echo "[INFO] 종료할 기존 프로세스가 없습니다."
fi

# 2. 최신 JAR 파일 찾기 (여러 개 있을 경우 최신 파일로 실행)
JAR_FILE=$(ls -t *SNAPSHOT.jar | grep -v 'plain' | head -n 1)

if [ ! -f "$JAR_FILE" ]; then
  echo "[ERROR] JAR 파일을 찾을 수 없습니다. 배포를 중단합니다."
  exit 1
fi

echo "[INFO] 실행할 JAR 파일: $JAR_FILE"

# 환경변수 값 확인 (디버깅용, 실제 배포 시 민감정보는 주석처리 권장)
echo "[DEBUG] JWT_SECRET: $JWT_SECRET"


# 3. JAR 실행 (환경변수는 이미 세션에 세팅되어 있다고 가정)
echo "[INFO] Spring Boot 애플리케이션 실행"
nohup java -jar "$JAR_FILE" > nohup.out 2>&1 &

sleep 3

# 4. 실행 상태 확인
NEW_PID=$(ps -ef | grep 'backend-0.0.1-SNAPSHOT.jar' | grep -v grep | awk '{print $2}')
if [ -n "$NEW_PID" ]; then
  echo "[SUCCESS] 애플리케이션이 정상적으로 실행되었습니다. (PID: $NEW_PID)"
else
  echo "[ERROR] 애플리케이션 실행에 실패했습니다. nohup.out 로그를 확인하세요."
fi
