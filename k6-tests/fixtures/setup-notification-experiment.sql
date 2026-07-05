-- MySQL 8.x 전용 알림 성능 실험 fixture
-- 100명의 공연자와 400명의 관객으로 총 500명 규모를 구성한다.
SET @fixture_performer_count = 100;
SET @fixture_audience_count = 400;
SET @fixture_likes_per_audience = 10;
SET @fixture_waiting_show_count = 100;

SET SESSION cte_max_recursion_depth = 20000;

START TRANSACTION;

-- 공연자 100명 생성
INSERT INTO member (
    username, name, role, email, phone, password, active_status,
    kakao_id, created_at, updated_at
)
WITH RECURSIVE sequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM sequence
    WHERE number < @fixture_performer_count
)
SELECT
    CONCAT('perf-notification-performer-', LPAD(number, 3, '0')),
    CONCAT('알림실험공연자', number),
    'PERFORMER',
    CONCAT('perf-notification-performer-', number, '@fixture.local'),
    NULL,
    NULL,
    'ACTIVE',
    CONCAT('perf-notification-performer-', number),
    NOW(),
    NOW()
FROM sequence
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    name = VALUES(name),
    role = VALUES(role),
    active_status = VALUES(active_status),
    updated_at = NOW();

-- 관객 400명 생성
INSERT INTO member (
    username, name, role, email, phone, password, active_status,
    kakao_id, created_at, updated_at
)
WITH RECURSIVE sequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM sequence
    WHERE number < @fixture_audience_count
)
SELECT
    CONCAT('perf-notification-audience-', LPAD(number, 3, '0')),
    CONCAT('알림실험관객', number),
    'AUDIENCE',
    CONCAT('perf-notification-audience-', number, '@fixture.local'),
    NULL,
    NULL,
    'ACTIVE',
    CONCAT('perf-notification-audience-', number),
    NOW(),
    NOW()
FROM sequence
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    name = VALUES(name),
    role = VALUES(role),
    active_status = VALUES(active_status),
    updated_at = NOW();

-- 기존 fixture 좋아요를 제거하고 순환 그래프로 재구성한다.
-- 관객 i는 공연자 i부터 i+9까지 좋아요하며 100번 다음은 1번으로 순환한다.
DELETE FROM member_like
WHERE liker_id IN (
    SELECT id
    FROM member
    WHERE email LIKE 'perf-notification-audience-%@fixture.local'
);

INSERT INTO member_like (liker_id, performer_id, performer_name)
WITH RECURSIVE audiences AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM audiences
    WHERE number < @fixture_audience_count
), offsets AS (
    SELECT 0 AS distance
    UNION ALL
    SELECT distance + 1
    FROM offsets
    WHERE distance + 1 < @fixture_likes_per_audience
)
SELECT
    audience.id,
    performer.id,
    performer.name
FROM audiences
CROSS JOIN offsets
JOIN member audience
  ON audience.email = CONCAT(
      'perf-notification-audience-',
      audiences.number,
      '@fixture.local'
  )
JOIN member performer
  ON performer.email = CONCAT(
      'perf-notification-performer-',
      MOD(audiences.number - 1 + offsets.distance, @fixture_performer_count) + 1,
      '@fixture.local'
  );

-- 이 SQL은 알림 이력이 없는 깨끗한 실험 DB에서 실행하는 것을 전제로 한다.
DELETE FROM amateur_show
WHERE name LIKE '[PERF-NOTIFICATION]%';

-- 공연자마다 공통 태그를 가진 승인된 과거 공연 하나를 생성한다.
-- 신규 공연 승인 시 100명의 공연자를 좋아한 관객 400명이 추천 대상이 된다.
INSERT INTO amateur_show (
    name, start, end, performer_name, hall_name,
    road_address, detail_address, runtime, hashtag, summary,
    status, approval_status, member_id, total_sold_ticket,
    created_at, updated_at
)
WITH RECURSIVE sequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM sequence
    WHERE number < @fixture_performer_count
)
SELECT
    CONCAT('[PERF-NOTIFICATION] APPROVED HISTORY ', LPAD(number, 3, '0')),
    DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY),
    DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY),
    performer.name,
    '알림 성능 실험 공연장',
    '서울시 테스트구',
    'fixture',
    90,
    '#performance-fixture',
    '추천 대상 산출용 승인 공연',
    'ENDED',
    'APPROVED',
    performer.id,
    0,
    NOW(),
    NOW()
FROM sequence
JOIN member performer
  ON performer.email = CONCAT(
      'perf-notification-performer-',
      sequence.number,
      '@fixture.local'
  );

-- 승인 대기 공연 100개를 공연자 100명에게 하나씩 배정한다.
INSERT INTO amateur_show (
    name, start, end, performer_name, hall_name,
    road_address, detail_address, runtime, hashtag, summary,
    status, approval_status, member_id, total_sold_ticket,
    created_at, updated_at
)
WITH RECURSIVE sequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM sequence
    WHERE number < @fixture_waiting_show_count
)
SELECT
    CONCAT('[PERF-NOTIFICATION] WAITING ', LPAD(number, 3, '0')),
    DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),
    DATE_ADD(CURRENT_DATE, INTERVAL 40 DAY),
    performer.name,
    '알림 성능 실험 공연장',
    '서울시 테스트구',
    'fixture',
    90,
    '#performance-fixture',
    '동기·비동기 알림 처리 성능 비교용 공연',
    'YET',
    'WAITING',
    performer.id,
    0,
    NOW(),
    NOW()
FROM sequence
JOIN member performer
  ON performer.email = CONCAT(
      'perf-notification-performer-',
      MOD(sequence.number - 1, @fixture_performer_count) + 1,
      '@fixture.local'
  );

COMMIT;

-- 기대 결과: 공연자 100, 관객 400, 좋아요 4,000, 과거 공연 100, 대기 공연 100
SELECT role, COUNT(*) AS fixture_member_count
FROM member
WHERE email LIKE 'perf-notification-%@fixture.local'
GROUP BY role;

SELECT COUNT(*) AS fixture_like_count
FROM member_like
WHERE liker_id IN (
    SELECT id
    FROM member
    WHERE email LIKE 'perf-notification-audience-%@fixture.local'
);

SELECT performer_id, COUNT(*) AS follower_count
FROM member_like
WHERE liker_id IN (
    SELECT id
    FROM member
    WHERE email LIKE 'perf-notification-audience-%@fixture.local'
)
GROUP BY performer_id
ORDER BY performer_id
LIMIT 10;

SELECT approval_status, COUNT(*) AS fixture_show_count
FROM amateur_show
WHERE name LIKE '[PERF-NOTIFICATION]%'
GROUP BY approval_status;
