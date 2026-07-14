# paymentModel 3종 분석 및 RabbitMQ 전환 가이드라인

> 분석일: 2026-07-14. 대상: `F:\Project\paymentModel\{payment, wallet, ledger}` (Kafka/Spring Cloud Stream 기반)
> 목적: 세 프로젝트의 기능을 **RabbitMQ 기반으로 재구현**하기 위한 사전 분석.
> 원본은 수정하지 않는다. payment 기능은 core-spa에 이식, wallet/ledger는 별도 워커로 PC2/PC3 배포 예정.

---

## 0. 한눈에 보기

```
[payment 서비스]                     [wallet 서비스]              [ledger 서비스]
결제 승인(Toss) → SUCCESS 전이       topic "payment" 구독          topic "payment" 구독
  → Outbox 저장(같은 트랜잭션)         → 멱등 필터                   → 멱등 필터
  → AFTER_COMMIT 발행                 → 판매자별 지갑 잔액 가산       → 복식부기(차변=대변) 기록
  → 실패 시 릴레이 스케줄러 재발행       → topic "wallet"로 완결 통지   → topic "ledger"로 완결 통지
                                      ↘________________________↙
[payment 서비스] topic "wallet"/"ledger" 구독 → is_wallet_updated / is_ledger_updated → completeIfDone
                (⚠️ 이 수신부는 원본에서 빈 클래스 — 미구현)
```

- **전달 보장**: at-least-once + 멱등 컨슈머 (전 구간 일관)
- **멱등키**: orderId (Outbox.idempotencyKey UNIQUE, WalletTransaction.idempotencyKey)
- **완성도**: payment ≈ 완성(수신부 제외) / wallet ≈ 동작(디버그 코드 잔존) / **ledger = 미완성(컴파일 불가)**

## 1. payment 프로젝트 (118 파일, Boot 3.3.8)

**core-spa의 원형이다.** PaymentEvent/PaymentOrder/PaymentStatusUpdateCommand/DispatchEventMessagePort/TossPaymentExecutor 등 클래스명이 core-spa와 거의 일치 — core-spa에 주석으로 남아있던 outbox seam들이 여기엔 실제 구현으로 존재한다.

### 핵심 구현 (core-spa에 없는 것)

1. **Transactional Outbox** — `Outbox` 엔티티(idempotencyKey UNIQUE, status INIT/SENT/FAILURE, payload/metadata JSON, partitionKey)
2. **`PaymentOutboxService.insertOutbox(command)`** — SUCCESS 전이 트랜잭션 **안에서** outbox 행 저장 (발행할 사실을 DB에 원자적으로 커밋 → 발행 유실 원천 차단)
3. **`PaymentEventMessageSender`** — `@TransactionalEventListener(AFTER_COMMIT)`로 발행 (우리 Phase 2와 동일 패턴!) + **발행 성공/실패를 outbox 상태에 기록** (`markMessageAsSent`/`markMessageAsFailure`)
4. **`PaymentEventMessageRelayService`** — `@Scheduled(fixedDelay=1s)` + `@Async`로 pending(INIT/FAILURE) outbox를 재발행하는 릴레이. AFTER_COMMIT 발행이 실패해도(브로커 다운 등) 스케줄러가 따라잡는다
5. **partitionKey** — orderId 해시로 Kafka 파티션 지정(동일 주문 순서 보장)

### 미구현 (빈 클래스)
- `WalletEventMessageHandler`, `LedgerEventMessageHandler` — wallet/ledger 완결 통지를 받아 `is_wallet_updated`/`is_ledger_updated`를 갱신하고 `completeIfDone()`을 호출해야 하는 자리

## 2. wallet 프로젝트 (42 파일, 헥사고날)

- **입력**: topic `payment` (group `wallet-service`, max-attempts 5, DLQ 설정 — 설정 오타 `dqp-name` 있음)
- **`SettlementService.processSettlement`** (@Transactional):
  1. `DuplicateMessageFilterPort.isAlreadyProcess` — WalletTransaction 존재 여부로 멱등 확인
  2. orderId로 PaymentOrder 목록 로드 → **판매자별 그룹핑**
  3. `Wallet.addBalance` — 잔액 가산, `@Version` **낙관적 락**으로 충돌 감지 (BigDecimal)
  4. `WalletEventMessage(SUCCESS, {orderId})`를 topic `wallet`로 발행
- **주목할 설계**: 이미 처리된 메시지여도 WalletEventMessage를 **다시 발행** — "정산 완료 후 크래시로 통지 발행에 실패"한 경우를 재전달로 복구하기 위함 (완결 통지도 at-least-once)
- **`LockManager`** — ReentrantLock 기반 **JVM 내부** 락 (sellerId 정렬 획득으로 데드락 회피, 타임아웃 3s, 유휴 정리). 단일 인스턴스에서만 유효 — 분산 환경에선 무력
- 상태: 동작하는 수준이나 System.out 디버그 다수, 인덱스 주석 처리 등 다듬기 전

### wallet의 동시성 접근 vs 우리 재고 차감 구현 비교

| | wallet (원본) | core-spa 재고 차감 (기구현) |
|---|---|---|
| 갱신 방식 | 엔티티 로드 → addBalance → 저장 (read-modify-write) | 조건부 원자 UPDATE 한 문장 |
| 충돌 제어 | @Version 낙관적 락 + 재시도 + JVM 락 | 경합 자체가 없음 (DB가 직렬화) |
| 멱등성 | WalletTransaction 존재 확인 (조회 후 판단) | UNIQUE 제약 INSERT (DB가 최종 방어) |

→ RabbitMQ 재구현 시 **우리 방식(원자 UPDATE + UNIQUE 멱등)을 기본으로 하되**, 낙관적 락 방식을 비교군으로 남겨 부하 실험(Phase 5/6)에서 대조하면 학습 가치가 크다.

## 3. ledger 프로젝트 (41 파일, 헥사고날) — ⚠️ 미완성

- **도메인은 완성**: `DoubleLedgerEntry` — 생성자에서 **차변=대변 금액 불변식 강제**. `Ledger.createDoubleLedgerEntry`가 주문 항목마다 (CREDIT to계정, DEBIT from계정, LedgerTransaction) 생성. `Account`/`LedgerEntry`/`LedgerTransaction` 엔티티, `FinanceType.PAYMENT_ORDER`용 계정쌍 로드
- **서비스는 미완성**: `DoubleLedgerEntryRecordService.recordDoubleLedgerEntry`에 저장 로직과 return문이 없음(메인 경로), `@Service` 미부착 → **빌드 실패 확인됨**
- 재구현 시 저장부(생성된 DoubleLedgerEntry 목록 → LedgerEntry/LedgerTransaction 영속화)와 완결 통지 발행을 우리가 완성해야 한다. core2_spa DB에 이미 `ledger_entries`/`ledger_transactions` 테이블이 존재(과거 실험 흔적) — 스키마 재활용 검토

## 4. Kafka → RabbitMQ 전환 매핑표

| Kafka (Spring Cloud Stream) | RabbitMQ (우리 스택) | 비고 |
|---|---|---|
| topic `payment` | `payment.exchange` + routing key `payment.confirmed` | **이미 존재** — 신규 큐만 바인딩 |
| consumer group `wallet-service` | 전용 큐 `wallet.settlement.queue` | 큐 자체가 그룹 역할 |
| topic `wallet` / `ledger` (완결 통지) | routing key `settlement.wallet.completed` / `settlement.ledger.completed` + 각 큐 | payment(core-spa)가 구독 |
| partition key (순서 보장) | 단일 큐 + 단일 컨슈머 = 자연 직렬화. 스케일아웃 시 consistent-hash exchange | Phase 5 실험감 |
| `enable-dlq` / max-attempts | `x-dead-letter-exchange` + manual ack + reject(requeue=false) | Phase 4에서 배선 |
| `required-acks: all` | publisher confirms (correlated) | Phase 4 |
| `StreamBridge.send(binding, msg)` | `RabbitTemplate.convertAndSend(exchange, rk, msg)` | |
| `Consumer<Message<T>>` 함수형 바인딩 | `@RabbitListener` | |
| Confluent Cloud (외부 SaaS) | PC2 로컬 브로커 | 외부 의존 제거 |

## 5. 재구현 로드맵 제안

**M1 — payment 이식 = core-spa Outbox 완성** (기존 플랜 Phase 6 항목의 조기 실행)
- `Outbox` 엔티티/테이블 + `PaymentOutboxService` + Sender의 sent/failure 마킹 + 1s 릴레이 스케줄러를 core-spa에 이식
- 기존 `LoadPendingPaymentEventMessagePort`/주석 seam이 정확히 이 자리 — Phase 2에서 만든 AFTER_COMMIT 발행 경로를 outbox 기반으로 승격
- partitionKey는 RabbitMQ에선 불필요 → 제거(또는 metadata에 보존만)

**M2 — settlement-worker 신규 프로젝트** (wallet 재구현, PC3 배포)
- notification-worker와 같은 골격(Boot+amqp), DB는 PC1 MySQL 접속(core2_spa 또는 전용 스키마)
- `wallet.settlement.queue` ← `payment.confirmed` 바인딩, 멱등(wallet_transaction UNIQUE) + 원자 UPDATE 잔액 가산
- 처리 후 `settlement.wallet.completed` 발행 (원본의 "이미 처리돼도 재통지" 설계 유지)

**M3 — ledger-worker 신규 프로젝트** (ledger 완성+재구현)
- 복식부기 도메인(불변식 포함) 이식 + **미완성 저장부를 완성** + `settlement.ledger.completed` 발행

**M4 — 완결 수신부** (원본의 빈 클래스 구현)
- core-spa가 두 completed 큐를 구독 → `is_wallet_updated`/`is_ledger_updated` 갱신 → `completeIfDone()` → `is_payment_done` — 미완이던 결제 완결 흐름의 최종 마무리

**Phase 4(신뢰성)와의 관계**: M2/M3의 정산·장부 큐가 manual ack + DLQ + publisher confirms를 배선할 실전 대상이 된다 (돈 도메인이라 신뢰성 장치의 의미가 선명).

## 6. 원본에서 배울 점 / 바로잡을 점

**배울 점** — Outbox 3단 구성(저장→발행→릴레이), 완결 통지의 at-least-once 재발행, 복식부기 불변식을 생성자에서 강제, 판매자별 그룹핑 후 일괄 정산.

**바로잡을 점** — read-modify-write 잔액 갱신(→ 원자 UPDATE), JVM 락(분산 무력, 큐 직렬화로 대체), 조회 후 판단식 멱등(→ UNIQUE 제약), System.out 디버그, ledger 미완성부, 빈 완결 수신부.
