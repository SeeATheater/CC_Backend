import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// 📊 검색 전용 메트릭
export const searchErrorRate = new Rate('search_errors');
export const searchResponseTime = new Trend('search_response_time');
export const fullTextSearchTime = new Trend('fulltext_search_time');
export const likeSearchTime = new Trend('like_search_time');
export const searchRequestCount = new Counter('search_requests');
export const authErrorRate = new Rate('auth_errors');

// 🌐 환경변수 설정
const baseUrl = __ENV.BASE_URL || 'https://api.seeatheater.site';
const testEmail = __ENV.TEST_EMAIL || 'user@test.com';

// ⚙️ 검색 성능 테스트 설정
export const options = {
    stages: [
        { duration: '1m', target: 5 },    // 1분간 5명 사용자로 증가
        { duration: '3m', target: 5 },    // 3분간 5명 사용자 유지 (일반 검색 부하)
        { duration: '1m', target: 15 },   // 1분간 15명 사용자로 증가
        { duration: '3m', target: 15 },   // 3분간 15명 사용자 유지 (높은 검색 부하)
        { duration: '1m', target: 0 },    // 1분간 0명으로 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],     // 95%의 요청이 2초 이하
        http_req_failed: ['rate<0.1'],         // 실패율 10% 이하
        search_errors: ['rate<0.05'],          // 검색 에러율 5% 이하
        search_response_time: ['p(90)<1500'],  // 90%의 검색이 1.5초 이하
        fulltext_search_time: ['p(95)<1000'],  // Full-Text 검색 95%가 1초 이하
        auth_errors: ['rate<0.02'],            // 인증 에러율 2% 이하
    },
};

console.log(`🔍 검색 성능 테스트 대상: ${baseUrl}`);
console.log(`👤 테스트 계정: ${testEmail}`);

// 🔐 JWT 토큰 획득 (수정된 버전)
function getJWTToken() {
    // 쿼리 파라미터로 email 전달
    const loginUrl = `${baseUrl}/login/dev?email=${encodeURIComponent(testEmail)}`;

    console.log(`🔐 토큰 발급 요청: ${loginUrl}`);

    const loginResponse = http.post(loginUrl, null, {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
    });

    const loginSuccess = check(loginResponse, {
        '토큰 발급 성공': (r) => r.status === 200,
        '토큰 발급 응답시간 < 1000ms': (r) => r.timings.duration < 1000,
        '토큰 응답 형식 확인': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.hasOwnProperty('accessToken');
            } catch (e) {
                console.error(`토큰 응답 파싱 실패: ${e}, Body: ${r.body}`);
                return false;
            }
        },
    });

    authErrorRate.add(!loginSuccess);

    if (loginSuccess) {
        try {
            const responseBody = JSON.parse(loginResponse.body);
            console.log('✅ JWT 토큰 발급 성공');
            return responseBody.accessToken;
        } catch (e) {
            console.error(`토큰 파싱 실패: ${e}`);
            return null;
        }
    } else {
        console.error(`❌ 토큰 발급 실패 - Status: ${loginResponse.status}, Body: ${loginResponse.body}`);
        return null;
    }
}

// 🔍 검색 키워드 풀 (다양한 검색 패턴)
const searchKeywords = [
    // 일반적인 검색어
    '공연', '연극', '뮤지컬', '콘서트', '전시',

    // 짧은 검색어 (인덱스 효율성 테스트)
    '서울', '부산', '대구', '인천', '광주',

    // 긴 검색어 (Full-Text Search 효율성 테스트)
    '서울 강남구 공연장', '클래식 음악 콘서트', '현대 미술 전시회',

    // 특수 문자 포함 검색어
    'K-POP', 'BTS 콘서트', '2024 페스티벌',

    // 존재하지 않을 가능성이 높은 검색어 (빈 결과 테스트)
    'xyz123', 'nonexistent', 'abcdefghijk',

    // 한글 + 영문 조합
    '서울 concert', '부산 musical', 'Seoul 공연',
];

// 🎯 메인 검색 테스트 함수
export default function () {
    // 🔐 인증 토큰 획득
    const token = getJWTToken();
    if (!token) {
        console.error('JWT 토큰 획득 실패 - 검색 테스트 중단');
        return;
    }

    const authHeaders = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    // 🔍 랜덤 검색 키워드 선택
    const keyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
    const boardType = Math.random() > 0.5 ? 'NORMAL' : 'PROMOTION';
    const page = Math.floor(Math.random() * 3); // 0-2 페이지
    const size = 10;

    console.log(`🔍 검색 테스트: "${keyword}" (${boardType})`);

    // 📊 검색 요청 카운트
    searchRequestCount.add(1);

    // 🔍 게시판 검색 API 호출
    const searchUrl = `${baseUrl}/api/boards/search?boardType=${boardType}&keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`;

    const searchStart = Date.now();
    const searchResponse = http.get(searchUrl, authHeaders);
    const searchDuration = Date.now() - searchStart;

    // 📊 검색 응답시간 기록
    searchResponseTime.add(searchDuration);

    // ✅ 검색 결과 검증
    const searchSuccess = check(searchResponse, {
        '검색 API 응답 성공': (r) => r.status === 200,
        '검색 응답시간 < 2000ms': (r) => searchDuration < 2000,
        '검색 결과 형식 확인': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.hasOwnProperty('content') && Array.isArray(body.content);
            } catch (e) {
                console.error(`검색 결과 형식 오류: ${e}`);
                return false;
            }
        },
        '페이징 정보 확인': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.hasOwnProperty('pageable') && body.hasOwnProperty('last');
            } catch (e) {
                return false;
            }
        },
        'JWT 인증 유지': (r) => r.status !== 401 && r.status !== 403,
    });

    searchErrorRate.add(!searchSuccess);

    // 📈 검색 결과 분석
    if (searchSuccess) {
        try {
            const searchResult = JSON.parse(searchResponse.body);
            const resultCount = searchResult.content.length;

            console.log(`✅ 검색 완료: "${keyword}" → ${resultCount}개 결과 (${searchDuration}ms)`);

            // Full-Text Search vs LIKE 검색 성능 구분 (응답시간 기반 추정)
            if (searchDuration < 500) {
                fullTextSearchTime.add(searchDuration);
            } else {
                likeSearchTime.add(searchDuration);
            }

        } catch (e) {
            console.error(`검색 결과 파싱 실패: ${e}`);
        }
    } else {
        console.error(`❌ 검색 실패: "${keyword}" (Status: ${searchResponse.status})`);
        if (searchResponse.status === 401 || searchResponse.status === 403) {
            console.error('인증 오류 발생 - JWT 토큰 문제일 수 있음');
        }
    }

    // 🔍 빈 검색어 테스트 (10% 확률)
    if (Math.random() < 0.1) {
        const emptySearchUrl = `${baseUrl}/api/boards/search?boardType=${boardType}&keyword=&page=0&size=10`;
        const emptySearchResponse = http.get(emptySearchUrl, authHeaders);

        check(emptySearchResponse, {
            '빈 검색어 처리 확인': (r) => r.status === 200,
        });
    }

    // 🔍 특수 문자 검색 테스트 (5% 확률)
    if (Math.random() < 0.05) {
        const specialKeyword = encodeURIComponent('!@#$%^&*()');
        const specialSearchUrl = `${baseUrl}/api/boards/search?boardType=${boardType}&keyword=${specialKeyword}&page=0&size=10`;
        const specialSearchResponse = http.get(specialSearchUrl, authHeaders);

        check(specialSearchResponse, {
            '특수문자 검색 처리': (r) => r.status === 200 || r.status === 400,
        });
    }

    // 💤 사용자 행동 시뮬레이션 (검색 간격)
    sleep(Math.random() * 3 + 1); // 1-4초 대기
}

// 📊 테스트 완료 후 요약 출력
export function handleSummary(data) {
    const searchRequests = data.metrics.search_requests.values.count || 0;
    const avgSearchTime = data.metrics.search_response_time.values.avg || 0;
    const searchErrorRate = data.metrics.search_errors.values.rate || 0;
    const authErrorRate = data.metrics.auth_errors.values.rate || 0;

    console.log('\n🔍 === 검색 성능 테스트 요약 ===');
    console.log(`📊 총 검색 요청: ${searchRequests}회`);
    console.log(`⏱️ 평균 검색 시간: ${avgSearchTime.toFixed(2)}ms`);
    console.log(`❌ 검색 에러율: ${(searchErrorRate * 100).toFixed(2)}%`);
    console.log(`🔐 인증 에러율: ${(authErrorRate * 100).toFixed(2)}%`);

    return {
        'search-summary.json': JSON.stringify({
            searchRequests,
            avgSearchTime,
            searchErrorRate,
            authErrorRate,
            timestamp: new Date().toISOString(),
        }),
    };
}
