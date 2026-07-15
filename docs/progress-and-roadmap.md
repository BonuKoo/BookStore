# core-mq 진행 현황 & 로드맵 (마스터 문서)

> 기준일: 2026-07-14 (M1/M2/M3 갱신: 2026-07-15). 이 문서가 전체 현황의 허브다. 각론은 아래 문서 참조:
> - [distributed-mq-plan.md](distributed-mq-plan.md) — 확정 플랜(Phase 0~6)과 진행 기록
> - [codebase-state.md](codebase-state.md) — core-spa 코드 현황 (Phase 2 착수 전 스냅샷)
> - [paymentmodel-analysis.md](paymentmodel-analysis.md) — paymentModel 3종 분석 + M1~M4 로드맵

---

## 1. 완료된 것 (전부 2026-07-14 검증 완료)

### 인프라 (Phase 0~1) ✅
- 3-PC 메시 네트워크: PC1(192.168.0.5, core-spa+MySQL) ↔ PC2(192.168.0.6, RabbitMQ) ↔ PC3(192.168.0.2, 워커)
- PC2: RabbitMQ 네이티브 설치, `coreapp` 계정(management 태그), 전용 vhost `core_vhost`, 방화벽 5672/15672

### 사전 코드 정리 ✅ (`e7c3b44`)
- `incrementFailedCount` 중복 증가 버그 수정
- `@Transactional` 통일(jakarta→spring) + private 무효 어노테이션 제거
- `TossPaymentController` 자체 try/catch 제거 → GlobalExceptionHandler 위임

### Phase 2 — 프로듀서 ✅ (`3fe51a4`, `868cd9a`)
- 토폴로지: `payment.exchange`(topic) → `payment.confirmed.queue` / `payment.failed.queue`
- 발행 경로: 상태 전이 트랜잭션 내부에선 도메인 이벤트만 → `@TransactionalEventListener(AFTER_COMMIT)` → `RabbitDispatchEventMessageAdapter`(DispatchEventMessagePort 구현) → 브로커
- 페이로드: orderId(자연 멱등키), buyer/orderName/totalAmount/approvedAt, 판매자별 items[]+quantity. 날짜는 ISO-8601(Boot ObjectMapper 재사용)
- 검증: JUnit 통합테스트 3건(커밋→발행 1회 / 롤백→미발행 / FAILURE→failed 발행) + **경로 A e2e**(가짜 paymentKey → FAILURE → 큐 적재, UTF-8 바이트 정합까지 확인)

### Phase 3 — notification-worker 🔶 부분 완료 (`688b6ff`, 별도 repo)
- 완료: 프로젝트 구현(리스너 2개+SlackNotifier), 타입 매퍼 INFERRED 처리, **PC1 로컬 실기동으로 실소비 검증**, bootJar 빌드
- 남음: PC3에서 실행 + e2e (jar는 PC3에 복사됨), Slack 봇 토큰 발급(선택 — 없어도 로그로 검증 가능)

### M1 — Transactional Outbox 이식 ✅ (2026-07-15) — 코드·테스트 완료
- 신규 `com.bookService.core.domain.payment.outbox` 패키지: `Outbox` 엔티티(idempotency_key=orderId UNIQUE, status INIT/SUCCESS/FAILURE, payload/metadata JSON — Kafka partitionKey 제거) + `OutboxStatus` + `OutboxRepository`(mark/pending 쿼리) + `PaymentOutboxService`
- **3단 구성**: ① `insertOutbox`를 상태 전이 트랜잭션 내부에서 호출(REQUIRED, 발행 사실을 원자 커밋) → ② `PaymentEventMessagePublishListener`가 AFTER_COMMIT 즉시발행 후 SUCCESS/FAILURE 마킹(실패해도 rethrow 안 함) → ③ `PaymentEventMessageRelayService`(`@Scheduled(fixedDelay=1s)`+`@Async`)가 미확정(INIT/FAILURE, 유예 10s 경과) 행 재발행. `@EnableScheduling` 추가
- SUCCESS·FAILURE 둘 다 outbox 경유(원본은 SUCCESS만). 즉시발행+릴레이 백업 하이브리드 유지
- DDL 수동 적용됨: `outbox` 테이블 (`docs/ddl/outbox.sql`, core2_spa에 적용 완료)
- 검증: 통합테스트 4건(저장+SENT 마킹 / 발행실패→FAILURE / 롤백→미저장 원자성 / 릴레이 INIT→재발행 SUCCESS) 전부 통과 + Phase 2 테스트 회귀 없음
- **브로커 실발행 e2e 완료 (2026-07-15, PC2 복구 후)**: 유예시간 경과한 stale INIT 행을 실DB에 심고 실제 앱(bootRun)을 PC2 브로커에 붙여 기동 → 릴레이가 실제로 감지·발행·SUCCESS 마킹까지 전 과정을 로그로 확인. `payment.confirmed.queue` 메시지 수 2→3건으로 정확히 1건 증가해 브로커 도달을 직접 확인. 팬아웃된 stock.deduction.queue는 가짜 isbn에 대해 설계대로 `InsufficientStockException`을 로그만 남기고 안전하게 삼킴(부작용 없음). **M1 전 과정(코드+단위테스트+브로커 e2e) 완결**
- ⚠️ 이 검증으로 `payment.confirmed.queue`의 대기 메시지가 **2건→3건**으로 늘었다 (orderId=`m1-relay-e2e-verify`, 최소 payload). Phase 3 첫 소비 테스트 시 PC3 워커가 3건을 소비하게 됨 — 원래 "2건" 기준 문서·기대치와 다르니 참고

### M2 — settlement-worker ✅ (2026-07-15) — 코드·테스트·브로커 e2e 전부 완료
- 신규 독립 프로젝트 `core/settlement-worker` (notification-worker와 같은 골격, Boot 3.4.8/Java 21, PC3 배포 대상). 아직 git 미초기화 — 커밋/원격 설정은 요청 시 진행
- 도메인: `Wallet`(seller_id UNIQUE, balance, **@Version 없음** — 원본의 낙관적 락 대신 원자 UPSERT로 동시성 해결) / `WalletTransaction`(UNIQUE(order_id, seller_id) — 멱등 가드 겸 감사기록, wallet_id FK 없음— 신규 판매자의 첫 정산 시 wallets 행 존재에 의존하지 않기 위함)
- **2계층 동시성** (재고 차감과 같은 원리, "부족" 개념이 없어 all-or-nothing 불필요): ① `WalletTransactionRepository` UNIQUE 멱등 가드(saveAndFlush 선행) ② `WalletRepository.creditBalance`의 `INSERT..ON DUPLICATE KEY UPDATE balance=balance+?` 원자 UPSERT. 판매자별로 `SellerWalletCreditor`(별도 빈, 독립 트랜잭션)에서 처리해 한 판매자 실패가 다른 판매자의 이미 커밋된 정산을 되돌리지 않음(재고차감의 "주문단위 all-or-nothing"과 의도적으로 다른 설계 — 잔액 가산은 실패 모드가 없어 all-or-nothing이 불필요)
- `SettlementService`가 payload의 items를 sellerId로 그룹핑·합산 후 판매자별 처리, 처리 결과와 무관하게 항상 `WalletCompletedMessage` 반환(원본의 "이미 처리돼도 재통지" 설계 유지) → `SettlementListener`가 소비 후 `WalletCompletedPublisher`로 `settlement.wallet.completed` 발행(auto-ack, outbox 없음 — 재고차감과 같은 신뢰성 수준, Phase 4에서 격상 예정)
- core2_spa에 과거 실험 흔적인 `wallets`/`wallet_transactions`(원본 낙관락 스키마, 0 rows) 존재 확인 → 사용자 승인 받아 DROP 후 M2 설계에 맞게 재생성(`docs/ddl/wallet.sql`, settlement-worker 프로젝트 내)
- 검증: 통합테스트 9건 전부 통과 — `SellerWalletCreditorIntegrationTest` 5건(최초생성/누적/멱등/동시중복멱등/동시서로다른주문 lost-update 없음) + `SettlementServiceIntegrationTest` 4건(다판매자 그룹핑/한판매자다항목합산/빈items/중복메시지 재통지)
- **브로커 실발행 e2e 완료**: 실앱(bootRun)을 PC2에 붙이고 기본 익스체인지로 `wallet.settlement.queue`에 직접 발행(payment.exchange 팬아웃 우회 — payment.confirmed.queue/stock.deduction.queue 오염 없음)해 소비→양쪽 판매자 지갑 크레딧→완결통지 발행까지 로그로 전 과정 확인. 이후 PC2가 재차 절전 다운(검증 완료 후 발생, 무관)
- 잔여: PC3 배포(현재는 PC1 dev), git 초기화/커밋, Phase 4(manual ack+DLQ+publisher confirms)

### M3 — ledger-worker ✅ (2026-07-15) — 코드·테스트 완료, 브로커 e2e는 PC2 복구 후 잔여
- 신규 독립 프로젝트 `core/ledger-worker` (settlement-worker와 같은 골격). 아직 git 미초기화
- 원본(paymentModel ledger)은 **컴파일 불가**였다 — `DoubleLedgerEntryRecordService.recordDoubleLedgerEntry`가 분개 목록만 만들고 저장 로직·return문이 아예 없었음(분석 문서 §3에서 이미 확인). 이 저장부를 완성하는 것이 M3의 핵심
- 도메인: `DoubleLedgerEntry`(생성자에서 차변=대변 금액 불변식 강제 — 원본 설계 그대로 이식) / `LedgerTransaction`(**UNIQUE(order_id, seller_id, product_id)** — 원본의 nullable+비UNIQUE idempotency_key를 실제 멱등 보장 키로 교체) / `LedgerEntry`(CREDIT·DEBIT 각 1행, transaction_id/account_id FK) / `Account`(REVENUE, ITEM_BUYER 두 계정, name UNIQUE)
- **항목 단위 처리, 판매자별 합산 안 함** — M2(wallet)는 판매자 잔액이 목적이라 같은 판매자 항목을 합산했지만, 장부는 항목별 감사 추적성이 핵심이라 원본처럼 항목(판매자-상품 줄)마다 독립 분개로 기록(`LedgerLineRecorder`, 별도 빈·독립 트랜잭션 — 한 줄 실패가 다른 줄의 커밋을 되돌리지 않음)
- 멱등 가드(saveAndFlush 선행) → `DoubleLedgerEntry` 불변식 검증 → REVENUE/ITEM_BUYER 계정 조회 → CREDIT+DEBIT 2행 저장. `LedgerRecordingService`가 항목들을 순회해 각각 위임, 결과와 무관하게 항상 `LedgerCompletedMessage` 반환(원본의 "재통지" 설계는 M2와 동일하게 적용) → `settlement.ledger.completed` 발행(auto-ack, outbox 없음 — Phase 4 격상 대상)
- core2_spa의 기존 `accounts`/`ledger_entries`/`ledger_transactions`(원본 스키마 그대로, 전부 0 rows) 존재 확인 → 사용자 승인 받아 DROP 후 재생성(`docs/ddl/ledger.sql`, REVENUE/ITEM_BUYER 계정 시드 포함)
- 검증: 통합테스트 7건 전부 통과 — `LedgerLineRecorderIntegrationTest` 4건(정상 이중분개/멱등/같은판매자 다른상품 독립기록/동시중복멱등) + `LedgerRecordingServiceIntegrationTest` 3건(다항목 독립기록/빈items/중복메시지 재통지)
- PC2가 다운된 상태(절전 이슈 재발, M2 e2e 직후 발생해 아직 미해결)에서도 `@SpringBootTest` 컨텍스트 기동·테스트는 정상 통과함(M1에서 확인한 것과 같은 패턴 — RabbitMQ 연결 실패가 컨텍스트 기동 자체를 막지 않음)
- **브로커 실발행 e2e 완료 (2026-07-15, PC2 복구 후)**: 실앱(bootRun)을 PC2에 붙이고 기본 익스체인지로 `ledger.recording.queue`에 직접 발행(payload 이스케이핑을 sed 대신 Python `json.dumps`로 정확히 처리 — 첫 시도는 sed 이스케이핑 실패로 깨진 JSON 발행→ `MessageConversionException`, 메시지는 auto-ack라 무한루프 없이 자연 소멸했음). 판매자 2명 결제 확정 이벤트 발행 → `ledger_transactions` 2행 + `ledger_entries` 4행(REVENUE CREDIT/ITEM_BUYER DEBIT 각 2쌍, 금액 정확) 생성 확인 + `settlement.ledger.completed.queue` 메시지 수 0→1 증가로 완결통지 발행까지 확인
- 잔여: PC3 실배포, git 초기화/커밋

### M4 — core-spa 완결 수신부 ✅ (2026-07-15) — 코드·테스트·브로커 e2e 전부 완료
- 원본에서도 **빈 클래스**(`WalletEventMessageHandler`/`LedgerEventMessageHandler`, 클래스 본문 없음)였던 자리. core-spa에는 이미 도메인 로직(PaymentEvent/PaymentOrder의 `confirmWalletUpdate`/`confirmLedgerUpdate`/`completeIfDone`)과 헥사고날 포트/어댑터(`LoadPaymentPort`/`CompletePaymentPort`/`PaymentPersistentAdapter`)가 준비돼 있었으나, **`PaymentPersistentAdapter.complete()`가 `return;`만 하는 완전 no-op 스텁**이었고 `PaymentEventRepository4Query`의 `handleWalletUpdate`/`handleLedgerUpdate`(벌크 UPDATE 로직은 이미 작성돼 있었음)가 통째로 주석 처리돼 있었음 — 이 배선을 마무리하는 것이 M4의 핵심
- 배선: querydsl `handleWalletUpdate`/`handleLedgerUpdate` 주석 해제 → `PaymentEventRepository`/`JpaPaymentEventRepository`에 위임 메서드 추가 → `CompletePaymentPort` 확장 → `PaymentPersistentAdapter`의 no-op `complete()`를 실제 위임으로 교체
- **버그 수정 1건(M4 경로에서 실제로 마주침)**: `PaymentEventRepository4QueryImpl.getPaymentEventAndOrders`가 매칭되는 주문이 없을 때 `null.setPaymentOrders(...)`로 NPE — orderId가 없는 통지(예: 존재하지 않는 주문 참조)를 받으면 크래시하는 기존 버그. null 체크 추가로 수정
- 신규: `PaymentCompletionService`(wallet/ledger 통지 각각 처리 → 플래그 반영 → `completeIfDone()` → 둘 다 참이면만 `complete()` 호출) + `PaymentCompletionListener`(`@RabbitListener` 2개) + `CompletedEventMessage` dto + RabbitMqConfig에 완결 큐 2종 바인딩 추가
- **버그 수정 2건**: core-spa의 `jsonMessageConverter`가 `TypePrecedence` 기본값(TYPE_ID)이라 settlement-worker/ledger-worker가 보내는 `__TypeId__` 헤더(이 프로젝트에 없는 클래스명)를 만나면 역직렬화 실패 — notification-worker가 이미 썼던 것과 같은 INFERRED 전환으로 수정(자기 자신의 기존 리스너들에도 안전)
- 멱등성: `confirmWalletUpdate`/`confirmLedgerUpdate`는 boolean 플래그를 true로 세팅할 뿐이라 중복 통지에 별도 가드 불필요
- 검증: 통합테스트 5건(지갑만 도착시 미완결 / 양쪽 도착시 완결 / 순서 반대(장부→지갑)도 완결 / 중복통지 멱등 / 존재안하는 orderId 무시) 전부 통과 + core-spa 전체 회귀 없음
- **브로커 실발행 e2e 완료**: core-spa 실앱을 PC2에 붙였더니 M2/M3 e2e 때 남겨둔 합성 완결 메시지 2건(존재하지 않는 orderId)이 **먼저 실행된 테스트 컨텍스트에서 이미 안전하게 소비됨**(크래시 없음 — NPE 수정이 실전에서 검증된 셈). 이어서 **실제 주문**을 DB에 만들고 wallet-completed → ledger-completed 순으로 발행해 `is_wallet_updated`(중간엔 true, ledger는 false) → 최종 `is_wallet_updated=is_ledger_updated=is_payment_done=true`까지 정확히 확인
- **M1~M4 로드맵 전체 완결** (코드+테스트+브로커 e2e 전부)

### 재고 차감 컨슈머 ✅ (`01b45f9`) — 실용 도메인 1호
- `stock.deduction.queue` ← `payment.confirmed` 팬아웃 바인딩 (프로듀서 무변경, core-spa 자기 소비)
- 동시성 3계층: UNIQUE 멱등(stock_deduction) / 조건부 원자 UPDATE(lost update·음수 차단) / 주문 단위 all-or-nothing
- 검증: 멀티스레드 통합테스트 5건 + 브로커 e2e(재고 50→48, 중복 발행 시 48 유지)
- DDL 수동 적용됨: `payment_orders.quantity`, `stock_deduction` 테이블

### 분석/문서/도구 ✅
- paymentModel 3종 분석 완료(`6ded8f3`) — payment=core-spa 원형(Outbox 완비), wallet=동작(취약한 동시성), ledger=미완성(컴파일 불가)
- k6 스크립트 현행화: `k6/book/checkout/8currentMaster/checkout-stress.js` (엔드포인트·상태코드 의미 수정) — **아직 실행 안 함**

---

## 2. 앞으로 할 일 (우선순위 순)

### A. paymentModel 재구현 M1~M4 (메인 트랙)
| 단계 | 내용 | 비고 |
|---|---|---|
| ~~M1~~ ✅ | ~~core-spa에 Transactional Outbox 이식~~ **완료 (2026-07-15)** — 위 §1 참조 (코드+테스트+브로커 e2e 전부) | partitionKey 제거, SUCCESS/FAILURE 둘 다 outbox 경유 |
| ~~M2~~ ✅ | ~~settlement-worker 신규~~ **완료 (2026-07-15)** — 위 §1 참조 (코드+테스트+브로커 e2e 전부). 잔여: PC3 실배포, git 초기화 | 원자 UPSERT+UNIQUE 멱등, 원본(낙관락) 방식은 폐기(비교군으로 남기지 않고 대체) |
| ~~M3~~ ✅ | ~~ledger-worker 신규~~ **완료 (2026-07-15)** — 위 §1 참조 (코드+테스트+브로커 e2e 전부, PC2 복구 후 검증 완료). 잔여: PC3 실배포, git 초기화 | 원본 컴파일 불가 저장부 완성, 항목단위 처리(합산 안 함) |
| ~~M4~~ ✅ | ~~core-spa가 completed 2종 구독~~ **완료 (2026-07-15)** — 위 §1 참조 (코드+테스트+브로커 e2e 전부) | 원본에서도 빈 클래스였던 최종 완결. **M1~M4 로드맵 전체 완결** |

**M1~M4 메인 트랙 전부 완료.** 다음은 §2-B의 Phase 3/4/5/6 마무리, 또는 PC3 실배포.

### B. 기존 Phase 마무리 (병행 가능)
- **Phase 3 완결**: PC3에서 `java -jar` 실행 → PC1 결제 → PC3 소비 확인 (payment.confirmed.queue에 합성 메시지 **3건** 대기 중 — 원래 2건 + M1 e2e 검증으로 추가된 1건[orderId=`m1-relay-e2e-verify`, 최소 payload]. Slack 토큰은 여유 될 때
- **Phase 2 DoD 잔여**: 경로 B — 프론트엔드로 Toss 샌드박스 실결제 → `payment.confirmed` 적재 확인 (사용자 직접)
- **Phase 4 (신뢰성)**: M2/M3 큐에 manual ack + reject(requeue=false) → DLQ, publisher confirms, 멱등성 검증. 재고 차감의 "재고부족 시 소실 허용"도 이때 DLQ로 교체
- **Phase 5 (장애 주입 3종)**: 브로커 다운/컨슈머 다운/ack 전 강제종료 → `docs/failure-experiments.md`
- **Phase 6 (선택)**: rabbitmq_prometheus + Grafana, k6 부하(동기 vs 비동기 정산 비교 — API p95와 정산 lag 분리 측정)

---

## 3. 보완해야 할 부분 (기술부채 대장)

### 코드 (core-spa)
| 항목 | 심각도 | 처리 시점 |
|---|---|---|
| `PaymentAlreadyProcessedException` → 500 반환 (예외 계층이 어드바이스에 안 잡힘) | 중 | Phase 4 (멱등 응답 정비와 함께) |
| 재고 차감: 재고부족 시 메시지 소실 허용 (auto-ack, 로그만) | 중 | Phase 4 (DLQ) |
| RestCheckoutController에도 자체 try/catch 잔존 (Toss 쪽만 제거했음) | 중 | M1 전후 |
| 헥사고날 이중 구조 (포트 경유 vs 구체 클래스 직접 주입 혼재) | 하 | M1 이식 때 자연 정리 |
| `@Repository`(PaymentStatusUpdateRepository)의 서비스급 책임 + 메시지 빌드까지 추가됨 | 하 | M1 때 Outbox 서비스로 분리 |
| `CheckoutCommandForDev` 네이밍, dead code(`exist` 변수, no-op `complete()`), TODO 주석 다수 | 하 | 여유 시 |

### 워커/인프라
| 항목 | 심각도 | 처리 시점 |
|---|---|---|
| **PC2 절전으로 브로커 소실 재발** (오늘 실제 발생) — 전원 옵션·자동 시작 재확인 필요 | **상** | 즉시 (PC2에서 사용자 작업) |
| RabbitMQ 비밀번호 `1234` — 랩 한정 허용, 외부 노출 금지 유지 | 중 | Phase 6 이후 |
| `application.yml` 미추적 — PC1 재세팅 시 유실 (allowPublicKeyRetrieval 포함). 백업 또는 template 파일 커밋 필요 | 중 | 다음 커밋에서 template 추가 권장 |
| notification-worker: Slack 토큰 placeholder, auto-ack, 원격 repo 없음(로컬 git만) | 하 | Phase 3 완결 시 |
| k6 신규 스크립트 실행 검증 미실시 (시드 유저·카트 전제) | 하 | Phase 6 부하 실험 전 |

### 운영 습관 (이번에 배운 것)
- **auto-ack 컨슈머 기동 = 즉시 소비**: "남겨두기로 한" 메시지가 있는 큐에 컨슈머를 붙이면 사라진다. 소비가 일어나는 작업은 사전에 큐 상태 확인 후 진행
- RabbitAdmin 선언은 lazy(첫 연결 시) — 기동 직후 관리 UI에 안 보여도 이상 아님
- 관리 API `get`은 검증용으로도 파괴적일 수 있음(`ack_requeue_false` 주의). peek은 `reject_requeue_true`
