/**
 * k6 부하 스크립트 (Phase 6): 결제 확정 → MQ 전체 경로.
 *
 * POST /v1/toss/confirm 에 시드된 주문을 1건씩 쏜다. loadtest 프로파일의
 * PSP 스텁 덕분에 confirm 이후의 실제 경로(상태 전이 → Outbox → 브로커 →
 * 워커 3종 → 완결 수신)가 전부 실코드로 돈다. HTTP 지표는 k6가,
 * 파이프라인 완결 지표는 verify_pipeline.py가 측정한다.
 *
 * 실행: k6 run confirm_load.js            (기본 VU 20)
 *       k6 run -e VUS=50 confirm_load.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';

const orders = new SharedArray('orders', () => JSON.parse(open('./orders.json')));
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    scenarios: {
        confirm: {
            executor: 'shared-iterations',
            vus: __ENV.VUS ? parseInt(__ENV.VUS, 10) : 20,
            iterations: orders.length, // 주문당 정확히 1회 — 멱등 재호출은 별도 시나리오에서
            maxDuration: '10m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<2000'],
    },
};

export default function () {
    // iterationInTest는 전 VU에 걸쳐 유일 → 주문을 정확히 한 번씩 소진
    const o = orders[exec.scenario.iterationInTest];
    const res = http.post(
        `${BASE_URL}/v1/toss/confirm`,
        JSON.stringify({ paymentKey: `lt-key-${o.orderId}`, orderId: o.orderId, amount: o.amount }),
        { headers: { 'Content-Type': 'application/json' } },
    );
    check(res, {
        'status 200': (r) => r.status === 200,
        'body SUCCESS': (r) => r.status === 200 && r.body.includes('SUCCESS'),
    });
}
