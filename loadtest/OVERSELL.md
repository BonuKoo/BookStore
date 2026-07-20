# 오버셀링 재현 실험 러너북 (포트폴리오 2-4 "잔여 리스크" 실증)

## 실험이 증명하려는 것

재고 차감을 결제 확정 이후로 미룬 현재 구조에서, 마지막 재고를 두고 결제가
동시에 확정되면 **결제는 전부 성공하는데 재고 차감은 일부만 성공**한다.
그 차감 실패분이 (1) 음수 재고 없이 정확히 격리되고 (2) DLQ에 남고
(3) DB 쿼리만으로 "환불 대상"이 완전 식별됨을 수치로 남긴다.
→ 이 식별 쿼리가 향후 자동 환불 컨슈머(Toss cancel API 연동)의 입력 정의.

기대 결과 (기본값 N=20, S=5): 결제 성공 20/20 · 재고 0(음수 없음) ·
차감 성공 5 · DLQ 적재 15 · 환불 대상 식별 15.

## 사전 조건

| 구성요소 | 필요 여부 |
|---|---|
| core-spa (:8080, **loadtest 프로파일**) | 필수 — PSP 스텁으로 confirm 즉시 승인. `./gradlew bootRun --args='--spring.profiles.active=loadtest'` |
| MySQL (127.0.0.1:3306) | 필수 (Windows 서비스라 항상 가동) |
| PC2 RabbitMQ (192.168.0.6) | 필수 — confirm → outbox → 브로커 → 재고 차감 컨슈머 경로 |
| PC3 워커 3종 | **불필요** — 재고 차감 컨슈머는 core-spa 자신. 단, 워커가 떠 있으면 ov- 주문의 wallet/ledger도 정상 처리됨(무해). 꺼져 있으면 해당 큐에 적체만 됨(역시 무해) |

주의: 시더의 상품 seller_id=999라 sellerId NPE(미배포 수정 건)와는 무관하게 안전.

## 실행 순서

```bash
cd /f/Project/movedFromC/core/core-spa/loadtest

# 1. 시드: 주문 20건 vs 재고 5개 (ov- 프리픽스, 기존 lt-/실데이터 무영향)
/c/Python313/python seed_oversell.py 20 5

# 2. 동시 confirm (VU 20 = 전 주문 한꺼번에)
k6 run oversell_confirm.js
# 원격이면: k6 run -e BASE_URL=http://192.168.0.5:8080 oversell_confirm.js

# 3. 드레인 대기 후(수 초면 충분) 판정
/c/Python313/python verify_oversell.py
# WARN(재고≠0)이 나오면 컨슈머 드레인 미완 — 몇 초 후 재실행
```

## 결정적(브로커 무관) 최소 재현 — 통합테스트

`OversellRefundTargetIntegrationTest` — 재고 1개에 SUCCESS 주문 2건을 심고
차감 실패 주문이 환불 대상 쿼리로 정확히 식별되는지 계약으로 고정.

```bash
./gradlew test --tests '*OversellRefundTargetIntegrationTest'
```

## 정리

- DB: `seed_oversell.py` 재실행이 ov- 행 전부 정리 후 재시드 (별도 정리 불필요)
- DLQ: ov- 메시지 15건이 남는다. verify는 비파괴 peek(requeue)이므로 실험 후
  관리 UI(192.168.0.6:15672)에서 `stock.deduction.queue.dlq` purge 권장
  — 단, **다른 실험의 DLQ 메시지가 섞여 있으면 purge 대신 놔둘 것** (전체 삭제라서)

## 실측 결과 (2026-07-20, HikariCP pool=64)

4개 시나리오 전부 PASS. 결제는 언제나 전건 SUCCESS(오버셀링 여부와 무관),
차감은 재고 수만큼 정확히, 초과분은 DLQ 격리, 환불 대상은 DB로 완전 식별.

| 시나리오 | 주문 N | 재고 S | 결제 성공 | 차감 성공 | 남은 재고 | DLQ 격리 | 환불 대상 | 판정 |
|---|---|---|---|---|---|---|---|---|
| 기본 | 20 | 5 | 20/20 | 5 | 0 | 15 | 15 | PASS |
| 확대 | 50 | 10 | 50/50 | 10 | 0 | 40 | 40 | PASS |
| 극단(핫로우) | 30 | 1 | 30/30 | 1 | 0 | 29 | 29 | PASS |
| **대조군** | 20 | 25 | 20/20 | 20 | 5 | 0 | 0 | PASS |

- **극단(30/1)**: 30건이 단 1개 재고를 동시 경합 → 원자 조건부 UPDATE가 정확히 1건만
  통과, 29건 격리, 음수 0. 락 없이 동시성이 지켜짐을 보이는 가장 강한 증거.
- **대조군(20/25)**: 재고가 충분하면 전건 차감 성공·DLQ 0·환불 대상 0.
  오버셀링 판정이 **거짓 양성(false positive)을 내지 않음**을 보이는 필수 대조.
  (verify의 "재고==0" 체크는 오버셀링 전용 가정이라 대조군에선 WARN이 뜨지만,
  남은 재고 5는 25-20의 정답이고 의미 있는 지표는 전부 PASS)
- k6 VU=N 동시. 50 VU에서 pool=40이면 4건 타임아웃 → pool=64로 해소(전건 성공).

### 실행 중 드러난 발견 2가지 (그 자체가 포트폴리오 소재)

1. **HikariCP 풀이 기본 10에 묶여 있던 진짜 원인**: `application.yml`의 hikari 블록이
   `spring.datasource.hikari`가 아니라 `spring.hikari`(잘못된 들여쓰기)에 있어
   `maximum-pool-size: 120`이 **무시**되고 있었다. Phase 6에서 "기본 풀 10 고갈"로
   기록된 병목의 근본 원인. loadtest 프로파일에 올바른 경로로 pool=40을 재지정해 해결
   (VU 20 동시 confirm이 p95 601ms로 전건 성공). **application.yml 본체 수정은 별도 과제.**
2. **결제 상태의 진짜 위치**: confirm 경로는 `payment_orders.payment_status`만 SUCCESS로
   갱신하고 `payment_event.payment_status`는 NOT_STARTED로 남긴다(완결은 is_payment_done).
   따라서 환불 대상 식별 쿼리는 반드시 payment_orders 기준이어야 한다.

## 포트폴리오 기입 문구 (실측 완료)

> 재고 5개 상품에 20건의 결제를 동시 확정시키는 실험으로 오버셀링을 재현 —
> 결제 20/20 성공(p95 601ms), 재고 차감은 원자적 조건부 UPDATE로 정확히 5건(음수 없음),
> 초과 15건은 DLQ로 격리. "결제 성공(payment_orders) & 재고 차감 기록 없음" 쿼리로
> 환불 대상 15건과 환불액을 DB만으로 완전 식별해, 자동 환불 컨슈머의 입력 계약을 확보.
