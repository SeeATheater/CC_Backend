import http from 'k6/http';
import exec from 'k6/execution';
import { check, fail } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const variant = __ENV.VARIANT || 'versionA';
const datasetUsers = Number(__ENV.DATASET_USERS || 500);
const requestCount = Number(__ENV.REQUESTS || 100);
const vus = 1;

export const approvalRequests = new Counter('approval_requests');
export const approvalSuccesses = new Counter('approval_successes');
export const approvalErrors = new Rate('approval_errors');
export const approvalResponseTime = new Trend('approval_response_time', true);

export const options = {
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        approve_shows: {
            executor: 'shared-iterations',
            vus,
            iterations: requestCount,
            maxDuration: __ENV.MAX_DURATION || '10m',
            gracefulStop: '0s',
            tags: {
                experiment: 'notification-sync-vs-async',
                variant,
                dataset_users: String(datasetUsers),
            },
        },
    },
    thresholds: {
        approval_errors: ['rate==0'],
    },
};

function buildExplicitShowIds() {
    if (__ENV.SHOW_IDS) {
        return __ENV.SHOW_IDS.split(',')
            .map((value) => value.trim())
            .filter((value) => value.length > 0);
    }

    if (!__ENV.SHOW_ID_START) {
        return null;
    }

    const start = Number(__ENV.SHOW_ID_START);
    if (!Number.isInteger(start) || start <= 0) {
        fail('SHOW_ID_START는 1 이상의 정수여야 합니다.');
    }

    return Array.from({ length: requestCount }, (_, index) => String(start + index));
}

function discoverWaitingShowIds(token) {
    const selected = [];
    let page = 0;
    let totalPages = 1;
    const pageSize = Math.min(Math.max(requestCount * 2, 100), 1000);
    const fixtureKeyword = '[PERF-NOTIFICATION]';

    while (page < totalPages && selected.length < requestCount) {
        const response = http.get(
            `${baseUrl}/admin/approval/showList?page=${page}&size=${pageSize}` +
            `&keyword=${encodeURIComponent(fixtureKeyword)}`,
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                    Accept: 'application/json',
                },
                tags: { endpoint: 'approval-fixture-discovery', measured: 'false' },
            },
        );

        if (response.status !== 200) {
            fail(`승인 대기 공연 자동 조회 실패: status=${response.status}, body=${response.body}`);
        }

        const result = response.json()?.result || response.json();
        const content = result?.content;
        if (!Array.isArray(content)) {
            fail(`승인 목록 응답 형식을 해석할 수 없습니다: ${response.body}`);
        }

        for (const show of content) {
            if (show.approvalStatus === 'WAITING' && show.showId != null) {
                selected.push(String(show.showId));
                if (selected.length === requestCount) break;
            }
        }

        totalPages = Number(result.totalPages || 0);
        page += 1;
    }

    if (selected.length < requestCount) {
        fail(
            `WAITING 공연이 부족합니다: 필요=${requestCount}, 조회=${selected.length}. ` +
            '성능 측정 전에 동일한 fixture를 versionA/versionB DB에 준비해야 합니다.',
        );
    }
    return selected;
}

function resolveAdminToken() {
    if (__ENV.ADMIN_TOKEN) {
        return __ENV.ADMIN_TOKEN;
    }

    const adminEmail = __ENV.ADMIN_EMAIL || 'seeatheateradmin@gmail.com';
    const adminPassword = __ENV.ADMIN_PASSWORD || __ENV.DEFAULT_PW;
    if (!adminPassword) {
        fail('ADMIN_TOKEN, ADMIN_PASSWORD 또는 DEFAULT_PW 중 하나가 필요합니다.');
    }

    const response = http.post(
        `${baseUrl}/admin/login`,
        JSON.stringify({
            email: adminEmail,
            password: adminPassword,
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { endpoint: 'admin-login', measured: 'false' },
        },
    );

    const loginSucceeded = check(response, {
        '관리자 로그인 성공': (result) => result.status === 200,
    });
    if (!loginSucceeded) {
        fail(
            `관리자 로그인 실패: email=${adminEmail}, ` +
            `status=${response.status}, body=${response.body}`,
        );
    }

    const body = response.json();
    const token = body?.result?.accessToken || body?.accessToken;
    if (!token) {
        fail(`로그인 응답에서 accessToken을 찾을 수 없습니다: ${response.body}`);
    }
    return token;
}

export function setup() {
    const token = resolveAdminToken();
    const showIds = buildExplicitShowIds() || discoverWaitingShowIds(token);
    if (showIds.length < requestCount) {
        fail(`공연 ID가 부족합니다: 필요=${requestCount}, 입력=${showIds.length}`);
    }
    if (new Set(showIds).size !== showIds.length) {
        fail('SHOW_IDS에는 중복되지 않은 공연 ID만 입력해야 합니다.');
    }

    return {
        token,
        showIds,
    };
}

export default function (data) {
    // iterationInTest는 시나리오 전체에서 유일하므로 각 공연은 정확히 한 번만 승인한다.
    // 동일 공연을 재승인해 발생하는 409 응답이 동기/비동기 성능 수치에 섞이는 것을 방지한다.
    const showId = data.showIds[exec.scenario.iterationInTest];
    const response = http.patch(
        `${baseUrl}/admin/approval/${showId}/approve`,
        null,
        {
            headers: {
                Authorization: `Bearer ${data.token}`,
                Accept: 'application/json',
            },
            tags: {
                endpoint: 'approve-show',
                measured: 'true',
                variant,
            },
        },
    );

    const succeeded = check(response, {
        '공연 승인 성공': (result) => result.status === 200,
    });

    approvalRequests.add(1, { variant, dataset_users: String(datasetUsers) });
    approvalSuccesses.add(succeeded ? 1 : 0, { variant, dataset_users: String(datasetUsers) });
    approvalErrors.add(!succeeded, { variant, dataset_users: String(datasetUsers) });
    approvalResponseTime.add(response.timings.duration, { variant, dataset_users: String(datasetUsers) });

    if (!succeeded) {
        console.error(`승인 실패: showId=${showId}, status=${response.status}, body=${response.body}`);
    }
}

function value(data, metricName, field) {
    return data.metrics[metricName]?.values?.[field] ?? null;
}

export function handleSummary(data) {
    const completedRequests = value(data, 'approval_requests', 'count');
    const result = {
        experiment: 'notification-sync-vs-async',
        variant,
        datasetUsers,
        configuredRequests: requestCount,
        vus,
        requests: completedRequests,
        successes: value(data, 'approval_successes', 'count'),
        errorRate: value(data, 'approval_errors', 'rate'),
        responseTimeMs: {
            average: value(data, 'approval_response_time', 'avg'),
            p95: value(data, 'approval_response_time', 'p(95)'),
            p99: value(data, 'approval_response_time', 'p(99)'),
            max: value(data, 'approval_response_time', 'max'),
        },
        throughputPerSecond: value(data, 'approval_requests', 'rate'),
    };

    const output = JSON.stringify(result, null, 2);
    if (completedRequests == null || completedRequests === 0) {
        return {
            stdout: '\n테스트 요청이 실행되지 않아 기존 결과 파일을 유지합니다.\n',
        };
    }

    const summaryFile = __ENV.SUMMARY_FILE || `k6-tests/results/experiment1-${variant}.json`;
    return {
        stdout: `\n${output}\n`,
        [summaryFile]: `${output}\n`,
    };
}
