# k6 부하 테스트 — 결제 확정 → MQ 전체 경로 (Phase 6)

confirm API에 부하를 걸어 **Outbox → RabbitMQ(PC2) → 워커 3종(PC3) → 완결 수신**
파이프라인 전체의 처리량과 드레인 시간을 측정한다. Toss 실결제는
`loadtest` 프로파일의 PSP 스텁(`LoadTestPaymentExecutorStub`)이 대체하고,
그 이후 경로는 전부 실코드가 돈다.

## 사전 조건
- PC2 RabbitMQ 기동, PC3 워커 3종(notification/settlement/ledger) 기동
- core-spa를 **loadtest 프로파일로** 기동:
  ```
  ./gradlew bootRun --args='--spring.profiles.active=loadtest'
  ```
  (기본 프로파일로 뜨면 스텁이 없어 실제 Toss를 호출하다 실패한다)

## 실행 순서 (이 디렉터리에서)
```bash
# 1. 주문 N건 시드 (payment_event/payment_orders NOT_STARTED + 전용 상품 재고 리셋)
/c/Python313/python seed_orders.py 1000

# 2. k6 부하 (VU 조절: -e VUS=50)
k6 run confirm_load.js

# 3. 파이프라인 완결 검증 (k6 종료 직후 실행)
/c/Python313/python verify_pipeline.py
```

## 측정 지표
| 계층 | 지표 | 도구 |
|---|---|---|
| HTTP(confirm) | RPS, p95/p99 지연, 실패율 | k6 |
| 파이프라인 | 전건 is_payment_done 도달까지 드레인 시간 | verify_pipeline.py |
| 브로커 | 큐 적체 추이, DLQ 유입(0이어야 함) | verify_pipeline.py (관리 API, 통계 5초 지연) |
| 정합성 | stock/wallet/ledger 행 수 == N, 재고 감소 == N, 분개 == 2N | verify_pipeline.py |

## 주의
- 시더는 `lt-` 프리픽스 주문만 정리·재생성한다. 실데이터에 영향 없음.
- ID는 1천만 대역이라 hibernate 시퀀스와 충돌하지 않는다.
- 부하 상품은 `loadtest-isbn-001` 하나 — 재고 차감의 조건부 원자 UPDATE가
  같은 행에 몰리므로 **행 잠금 경합이 병목이 되는지 관찰하는 것 자체가 실험 포인트**.
