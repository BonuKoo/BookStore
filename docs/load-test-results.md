# k6 부하 테스트 결과 (Phase 6)

결제 확정(`POST /v1/toss/confirm`) → Transactional Outbox → RabbitMQ(PC2) → 워커 4종(PC3) →
완결 수신(is_payment_done) 파이프라인을 k6로 부하 테스트한 결과.

**측정 환경**
- SUT: core-spa(PC1, Intel i7-6700 4C/8T) `loadtest` 프로파일 — `LoadTestPaymentExecutorStub`(@Primary)가
  Toss 실호출을 대체하고, confirm 이후 경로(상태전이·Outbox·발행·워커·완결)는 전부 실코드.
- 브로커: RabbitMQ(PC2, 192.168.0.6) / 워커 3종: notification·settlement·ledger(PC3, 192.168.0.2)
- MySQL: PC1 로컬(core2_spa). k6: PC1 실행(VU 20, 부하 생성기 자원 부담 낮음).
- 관리 API 큐 통계는 5초 집계 지연이 있으므로 추세만 신뢰. 각 지표는 시드→부하→verify 순으로 격리 측정.
- 측정일: 2026-07-22

---

## 1. S1 — HikariCP 병목 발견 → 해소 (전/후 비교, 핵심 서사)

동일 조건(1000건 시드, VU 20, shared-iterations)에서 커넥션 풀 튜닝 전후를 비교.

| 지표 | 개선 전 (2026-07-15, pool=10) | 개선 후 (2026-07-22, pool=64) |
|---|---|---|
| 성공률 | **0.33%** (2/602) | **100%** (1000/1000) |
| p95 지연 | **60s** (타임아웃) | **163.5ms** |
| p90 / median | — | 128ms / 64ms |
| 처리량 | 사실상 0 | **242.5 req/s** |
| HikariCP | pool=10 고갈 (`active=10, waiting=45`) | pool=64, 대기 없음 |
| 임계값 | 전부 실패 | `p95<2s` ✓, `실패율<1%` ✓ |

**서사**: 1차 부하에서 첫 요청만 성공하고 나머지가 30초 후 `CannotCreateTransactionException`으로 전멸했다.
원인은 애플리케이션 코드가 아니라 **HikariCP 기본 풀 크기(10)가 동시 요청 20을 감당 못한 용량 결함**.
평시 트래픽(로그인 사용자 1명씩 순차 결제)에서는 절대 드러나지 않던 한계를, 동시성 20에서 즉시 규명했다.
`application-loadtest.yml`에서 풀을 64로 상향하니 p95가 **60초 → 163ms로 약 367배 개선**, 전건 성공.

> 주의: 기본 `application.yml`의 hikari 블록이 `spring.hikari`(잘못된 위치)에 있어 무시되고 기본 풀 10이
> 적용되던 것이 근본 원인. loadtest 프로파일에서 `spring.datasource.hikari`로 올바르게 재지정.

## 2. 비동기 파이프라인 드레인 (S1 부하 직후)

1000건 confirm 후 워커 4종이 처리한 결과 (`verify_pipeline.py`):

| 항목 | 결과 |
|---|---|
| stock_deduction (재고 차감) | 1000/1000 ✅ |
| wallet_transactions (정산) | 1000/1000 ✅ |
| ledger_transactions (장부) | 1000/1000 ✅ |
| ledger_entries (복식부기 분개) | 2000 (= 2×1000, 차변+대변) ✅ |
| DLQ 유입 | 0건 ✅ |
| **is_payment_done (최종 완결)** | **998/1000 ⚠️ — §4 버그** |

**동기 vs 비동기 분리**: confirm 동기 응답 p95는 163ms인데, 그 뒤 재고·정산·장부는 비동기로 처리된다.
이 간극이 곧 **MQ 도입으로 사용자 대기에서 분리해 낸 작업량** — 사용자는 163ms에 결제 완료를 확인하고,
무거운 후속 처리는 뒤에서 흐른다.

## 3. 오버셀링 실험 — "결제 성공 ≠ 재고 확보" 실증

재고 5개 상품에 20건을 동시 confirm(`seed_oversell.py 20 5` + `oversell_confirm.js`, VU=주문수=20):

| 판정 | 결과 |
|---|---|
| 결제 | 20건 전부 HTTP 200 SUCCESS (결제는 재고와 무관하게 성공) |
| 재고 | 정확히 0 — **음수로 가지 않음** (조건부 원자 UPDATE 계약 준수) |
| 재고 차감 | 5건만 성공 |
| DLQ 격리 | 재고 부족 15건이 정확히 `stock.deduction.queue.dlq`로 격리 |
| 환불 대상 식별 | "SUCCESS인데 stock_deduction 없는 주문" 15건을 **DB 쿼리만으로 완전 식별** |

**의미**: 분산 결제 시스템에서 "결제 승인"과 "재고 확보"는 별개 트랜잭션이다. 동시 주문이 마지막 재고를
두고 경쟁하면 결제는 다 성공할 수 있고, 초과분은 유실되지 않고 DLQ에 격리되어 **환불 대상으로 정확히
식별 가능**하다. 이 식별 쿼리가 곧 향후 자동 환불 컨슈머의 입력 정의가 된다.

## 4. 부하 테스트가 발견한 동시성 결함 (완결 수신부 race condition)

**S1의 is_payment_done이 1000건 중 2건(0.2%)만 미완결**로 남았다. 두 건 모두
`is_wallet_updated=1 AND is_ledger_updated=1`(양쪽 완료)인데 `is_payment_done=0` — 명백한 불변식 위반.

**근본 원인**: `PaymentCompletionService.apply()`가 **락 없는 read-modify-write**.
wallet 완결 통지와 ledger 완결 통지는 서로 다른 큐에서 **병렬 소비**되는데:

```
스레드A(wallet)                     스레드B(ledger)
─────────────────                   ─────────────────
read: wallet=0, ledger=0            read: wallet=0, ledger=0   ← 둘 다 상대 업데이트 전 스냅샷
set wallet=1, persist               set ledger=1, persist
completeIfDone? (dto: w=1,l=0)      completeIfDone? (dto: w=0,l=1)
  → 미완 (ledger=0)                   → 미완 (wallet=0)
─────────────────                   ─────────────────
최종 DB: wallet=1, ledger=1  →  그러나 아무도 완결 처리를 안 함 → is_payment_done 영구 0
```

각 트랜잭션이 자기가 읽은 스냅샷 기준으로만 완결을 판단하고, 상대 트랜잭션의 커밋을 보지 못한다.
lock_bench.py에서 실증한 lost update와 정확히 같은 종류의 결함이다.

**가치**: 순차 처리(평시)에서는 절대 나타나지 않고 동시 완결 통지가 겹칠 때만 0.2% 발생 —
**부하 테스트가 아니었으면 발견 불가능한 결함**. 이 프로젝트가 재고 차감엔 원자 UPDATE를 잘 적용했으면서
완결 수신부엔 그 교훈이 빠졌음을 부하가 드러냈다.

**수정 방향(미적용)**: ① 완결 판정을 DB 조건부 원자 UPDATE로
(`UPDATE payment_event SET is_payment_done=1 WHERE order_id=? AND 미완 order 없음`), 또는
② `getPaymentEventAndOrders`에서 `SELECT ... FOR UPDATE`로 payment_event를 잠가 두 통지를 직렬화.
①이 lock_bench.py 결론(원자 UPDATE > 락)과 일관된다.

## 5. 재현

```bash
cd core-spa/loadtest
# S1
PYTHONIOENCODING=utf-8 /c/Python313/python seed_orders.py 1000
/c/xk6/k6 run confirm_load.js
PYTHONIOENCODING=utf-8 /c/Python313/python verify_pipeline.py
# 오버셀링
PYTHONIOENCODING=utf-8 /c/Python313/python seed_oversell.py 20 5
/c/xk6/k6 run oversell_confirm.js
PYTHONIOENCODING=utf-8 /c/Python313/python verify_oversell.py
```
전제: core-spa `loadtest` 프로파일 기동, PC2/PC3 워커 생존. 시드는 `lt-`/`ov-` 프리픽스만 정리(실데이터 무영향).
