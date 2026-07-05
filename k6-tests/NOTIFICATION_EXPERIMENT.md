# 알림 처리 방식 비교 실험 1

## 목적

공연 승인 API가 승인·좋아요·추천 알림을 생성할 때 다음 두 구조의 API 성능을 비교한다.

| 구분 | 브랜치 | 알림 처리 | 측정 지표 |
|---|---|---|---|
| versionA | `test/versionA` | Spring Event 기반 동기 처리 | API 응답 시간 p95·p99, TPS, 오류율 |
| versionB | `test/versionB` | Outbox·Kafka 기반 비동기 처리 | API 응답 시간 p95·p99, TPS, 오류율 |

두 버전의 차이는 알림 처리 방식이어야 한다. 회원 수, 좋아요 관계, 공연·태그 데이터, 애플리케이션과 DB 자원, k6 옵션은 동일하게 유지한다. 기본 fixture는 공연자 100명과 관객 400명으로 총 500명을 구성한다.

## 테스트 시나리오

스크립트는 관리자 한 명의 실제 사용 흐름에 맞춰 1 VU로 서로 다른 `WAITING` 공연을 순차 승인한다. `SHOW_IDS`와 `SHOW_ID_START`를 생략하면 관리자 승인 목록을 조회해 대상 ID를 자동으로 선별한다. 하나의 공연은 한 번만 승인할 수 있으므로 DB에는 요청 수만큼 중복 없는 승인 대기 공연이 필요하다.

```text
관리자 계정 1개 / k6 VU 1개
  → PATCH /admin/approval/{showId}/approve
  → HTTP 200 확인
  → 승인 API 응답 시간과 처리량 기록
```

VU는 코드에서 1로 고정되어 환경변수로 변경할 수 없다. 관리자가 공연 100개를 순차 승인하는 동일 조건에서 알림 후속 처리 방식만 비교한다.

`versionA`는 알림 저장까지 끝난 뒤 응답하고, `versionB`는 비즈니스 데이터와 Outbox 저장이 커밋되면 응답한다. 따라서 이 실험의 종속 변인은 HTTP 요청 관점의 응답 시간과 TPS다. Kafka 알림의 최종 저장 완료 시간은 Consumer 스케일링 실험에서 별도로 측정한다.

## 데이터 전제 조건

- 관리자 계정이 존재해야 한다.
- 요청 수만큼 상태가 `WAITING`인 공연이 있어야 한다.
- 공연 ID는 중복되면 안 된다.
- 추천 알림 부하를 만들 회원·좋아요·공연 해시태그 관계가 미리 저장되어 있어야 한다.
- A와 B는 각각 `cc_perf_a`, `cc_perf_b`를 사용하되 같은 fixture 상태에서 시작해야 한다. A 실행 후 변경된 DB를 B가 공유하면 비교할 수 없다.
- A와 B에 공연자 100명, 관객 400명, 좋아요 4,000건의 동일한 fixture를 준비한다.

### Fixture 생성

MySQL 8에서 다음 SQL을 실행하면 테스트 공연자, 관객, 좋아요 관계, 추천 기준 공연과 `WAITING` 공연을 일괄 생성한다. 관리자는 애플리케이션의 `AdminInitializer`가 생성한 기본 관리자를 사용한다.

```bash
docker exec -i cc-perf-mysql mysql -uroot -pperfpass cc_perf_a \
  < k6-tests/fixtures/setup-notification-experiment.sql
```

versionB에는 DB 이름만 `cc_perf_b`로 변경해 같은 SQL을 실행한다. Hibernate가 스키마를 만들고 `AdminInitializer`가 관리자를 생성하도록 애플리케이션을 먼저 한 번 실행해야 한다.

기본값은 공연자 100명, 관객 400명, 관객당 좋아요 10개, 승인 대기 공연 100개다.

```sql
SET @fixture_performer_count = 100;
SET @fixture_audience_count = 400;
SET @fixture_likes_per_audience = 10;
SET @fixture_waiting_show_count = 100;
```

각 관객은 자신의 번호를 기준으로 연속된 공연자 10명을 순환해서 좋아요한다. 총 4,000개의 좋아요가 만들어지고 각 공연자는 정확히 40명의 팔로워를 가진다. 공연자마다 `#performance-fixture` 태그의 승인된 과거 공연이 있으므로 신규 공연 하나를 승인할 때 승인 알림 1건, 좋아요 알림 40건, 추천 알림 최대 400건이 생성된다.

공연 100개를 모두 승인했을 때 기대 결과는 다음과 같다.

| 구분 | Notice | MemberNotice |
|---|---:|---:|
| 등록자 승인 알림 | 100 | 100 |
| 공연자 좋아요 회원 알림 | 100 | 4,000 |
| 태그 추천 알림 | 100 | 40,000 |
| 합계 | 300 | 44,100 |

기본 관리자는 다음 계정을 사용한다.

```text
email: seeatheateradmin@gmail.com
password: 애플리케이션의 DEFAULT_PW 환경변수
```

k6는 `ADMIN_EMAIL`을 생략하면 위 이메일을 사용한다. 비밀번호는 `DEFAULT_PW` 또는 `ADMIN_PASSWORD`로 전달해야 한다. 애플리케이션을 먼저 한 번 실행해 `AdminInitializer`가 관리자를 생성한 다음 fixture를 넣는다.

## 실행

### versionA

실행 전 애플리케이션을 종료하고 `cc_perf_a`를 초기화한다.

```bash
docker exec cc-perf-mysql mysql -uroot -pperfpass -e "DROP DATABASE IF EXISTS cc_perf_a; CREATE DATABASE cc_perf_a CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

versionA 애플리케이션을 실행해 스키마와 관리자를 생성한 다음 fixture를 넣는다. 이후 다음 스크립트를 실행한다.

반복 실행 결과가 덮어써지지 않도록 자동 번호 실행 스크립트를 권장한다. 기존 `runN` 파일 중 가장 큰 번호 다음 값으로 저장한다.

```bash
export DEFAULT_PW='<애플리케이션과 동일한 값>'
VARIANT=versionA bash k6-tests/scripts/run-notification-experiment.sh
```

결과 파일은 실행할 때마다 다음처럼 증가한다.

```text
experiment1-versionA-run1.json
experiment1-versionA-run2.json
experiment1-versionA-run3.json
```

공연 100개가 모두 승인되므로 새로운 run을 실행하기 전에는 DB 초기화와 fixture 삽입을 다시 수행해야 한다. 실행 번호 증가는 결과 파일 보존을 위한 기능이며 DB를 자동 초기화하지 않는다.

### versionB

`cc_perf_b`를 초기화하고 Kafka·Redis를 실행한다.

```bash
docker exec cc-perf-mysql mysql -uroot -pperfpass -e "DROP DATABASE IF EXISTS cc_perf_b; CREATE DATABASE cc_perf_b CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker compose up -d redis kafka kafka-ui
```

versionB 애플리케이션을 `cc_perf_b`에 연결해 실행하고 동일 fixture를 넣은 뒤 `VARIANT`만 변경한다.

```bash
VARIANT=versionB bash k6-tests/scripts/run-notification-experiment.sh
```

아래는 결과 파일명을 직접 지정해 실행하는 방식이다.

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e DEFAULT_PW='<애플리케이션과 동일한 값>' \
  -e VARIANT=versionA \
  -e DATASET_USERS=500 \
  -e REQUESTS=100 \
  -e SUMMARY_FILE=k6-tests/results/experiment1-versionA-users500.json \
  k6-tests/scripts/notification-sync-vs-async-test.js
```

기본적으로 공연 ID 입력은 필요 없다. 특정 공연만 측정하거나 두 DB에서 대상 ID를 명시적으로 맞추고 싶다면 연속 ID의 시작값 또는 쉼표 목록을 전달한다.

```bash
-e SHOW_ID_START=10001
```

```bash
-e SHOW_IDS=101,205,309,412
```

다른 관리자 계정을 사용하려면 다음 값을 전달한다. 로그인 요청은 승인 전용 커스텀 메트릭에서 제외된다.

```bash
-e ADMIN_EMAIL='admin@example.com' -e ADMIN_PASSWORD='<PASSWORD>'
```

다음으로 DB를 동일한 초기 스냅샷으로 복구하고 `test/versionB` 애플리케이션을 실행한 뒤, `VARIANT`와 결과 파일명만 바꿔 같은 명령을 실행한다.

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e DEFAULT_PW='<애플리케이션과 동일한 값>' \
  -e VARIANT=versionB \
  -e DATASET_USERS=500 \
  -e REQUESTS=100 \
  -e SUMMARY_FILE=k6-tests/results/experiment1-versionB-users500.json \
  k6-tests/scripts/notification-sync-vs-async-test.js
```

## 결과 해석

결과 JSON의 `responseTimeMs.p95`, `responseTimeMs.p99`, `throughputPerSecond`, `errorRate`를 비교한다. `approval_requests`는 승인 요청만 세므로 관리자 로그인 등 준비 요청 때문에 TPS가 왜곡되지 않는다.

versionA의 API 응답시간에는 승인과 441건의 알림 저장이 포함된다. versionB의 API 응답시간에는 승인과 Outbox 저장까지만 포함되고 Kafka 발행과 Consumer의 알림 저장은 포함되지 않는다. 따라서 이 실험 결과는 알림 생성 작업을 요청에 동기 결합했을 때와 비동기로 분리했을 때의 사용자 관점 API 성능 차이를 의미한다.

versionA는 k6 종료 직후, versionB는 Consumer 처리가 끝난 뒤 다음 결과를 검증한다.

```sql
SELECT type, COUNT(*) FROM notice GROUP BY type;

SELECT n.type, COUNT(*)
FROM member_notice mn
JOIN notice n ON n.id = mn.notice_id
GROUP BY n.type;

SELECT COUNT(*) FROM member_notice;
```

최종 `MemberNotice`는 두 버전 모두 44,100건이어야 한다. versionB는 추가로 Outbox 상태와 Kafka lag를 확인한다.

```sql
SELECT status, COUNT(*) FROM outbox_event GROUP BY status;
```

모든 Outbox가 `PUBLISHED`이고 Kafka lag가 0이 되기 전에 알림 건수를 비교하면 안 된다. API 응답시간과 별도로 Kafka 알림 생성 완료시간을 측정하려면 상태 조회 또는 Kafka·DB 메트릭을 이용한 별도 end-to-end 실험이 필요하다.
