/**
 * k6 오버셀링 시나리오: N건 동시 confirm vs 재고 S개.
 *
 * confirm_load.js와 달리 VU 수 = 주문 수 — 전 주문을 한꺼번에 쏴서
 * "마지막 재고를 두고 결제가 동시에 확정되는" 경쟁을 만든다.
 * 기대 결과: HTTP는 전건 200 SUCCESS(결제는 다 성공한다는 게 핵심!),
 * 재고 차감은 컨슈머 쪽에서 S건만 성공하고 N-S건이 DLQ로 빠진다.
 * 사후 판정은 verify_oversell.py.
 *
 * 실행: k6 run oversell_confirm.js
 *       k6 run -e BASE_URL=http://192.168.0.5:8080 oversell_confirm.js  (PC3에서)
 */
import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';

const orders = new SharedArray('orders', () => JSON.parse(open('./oversell_orders.json')));
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    scenarios: {
        oversell: {
            executor: 'shared-iterations',
            vus: orders.length,        // 전 주문 동시 발사
            iterations: orders.length, // 주문당 정확히 1회
            maxDuration: '2m',
        },
    },
    thresholds: {
        // 결제 자체는 전건 성공해야 한다 — 실패가 있다면 오버셀링 실험이 아니라
        // confirm 경로 문제(HikariCP 등)이므로 실험 무효
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },
};

export default function () {
    const o = orders[exec.scenario.iterationInTest];
    const res = http.post(
        `${BASE_URL}/v1/toss/confirm`,
        JSON.stringify({ paymentKey: `ov-key-${o.orderId}`, orderId: o.orderId, amount: o.amount }),
        { headers: { 'Content-Type': 'application/json' } },
    );
    check(res, {
        'status 200': (r) => r.status === 200,
        'body SUCCESS': (r) => r.status === 200 && r.body.includes('SUCCESS'),
    });
}
