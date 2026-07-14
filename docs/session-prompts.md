# 세션 프롬프트 모음 (core-mq / paymentModel 재구현)

> 갱신일: 2026-07-14. 새 Claude 세션을 시작할 때 사용하는 프롬프트 템플릿.
> 사용법은 2가지로 나뉜다:
> - **이 계정 세션(PC1, 메모리 있음)**: 한 줄 프롬프트만으로 충분 — 메모리가 맥락을 복원한다.
> - **메모리 없는 세션(PC2/PC3, 또는 새 계정)**: 아래 [공통 컨텍스트 블록]을 맨 위에 붙이고 + 작업 지시를 잇는다.

---

## A. 이 계정용 한 줄 프롬프트 (PC1)

| 작업 | 프롬프트 |
|---|---|
| M1 Outbox 이식 | `core-mq M1(Outbox 이식) 진행하자. docs/paymentmodel-analysis.md 5절 기준.` |
| M2 정산 워커 | `core-mq M2(settlement-worker) 진행하자.` |
| M3 장부 워커 | `core-mq M3(ledger-worker) 진행하자. 원본 미완성 저장부는 우리가 완성한다.` |
| M4 완결 수신부 | `core-mq M4(완결 수신부) 진행하자. completeIfDone까지.` |
| Phase 4 신뢰성 | `core-mq Phase 4 진행하자. M2/M3 큐에 manual ack + DLQ + publisher confirms 배선.` |
| Phase 5 장애 실험 | `core-mq Phase 5 장애 주입 실험 3종 진행하자. 결과는 docs/failure-experiments.md에.` |
| 현황 파악만 | `core-mq 현재 상태 요약해줘. docs/progress-and-roadmap.md 기준.` |

---

## B. 공통 컨텍스트 블록 (메모리 없는 세션용 — 복사해서 프롬프트 맨 위에)

```
[프로젝트 컨텍스트 — 그대로 신뢰하고 시작할 것]

## 무엇을 하는 프로젝트인가
BookStore(core-spa, Spring Boot 3.4.8/Java 21/MySQL/Toss결제)를 중심으로
3대 물리 PC에 RabbitMQ 분산 메시징을 구축하는 랩.
현재는 Kafka 기반 참고 프로젝트(F:\Project\paymentModel\{payment,wallet,ledger})의
기능을 RabbitMQ로 재구현하는 단계(M1~M4).

## 물리 토폴로지
- PC1 (192.168.0.5): core-spa + MySQL(core2_spa, root/1234) — 프로듀서 겸 재고차감 컨슈머
  repo: F:\Project\movedFromC\core\core-spa (GitHub BonuKoo/BookStore, master)
- PC2 (192.168.0.6): RabbitMQ 브로커 전담. 계정 coreapp/1234, vhost core_vhost,
  관리 UI http://192.168.0.6:15672. guest 계정 없음. Docker 아님(네이티브).
- PC3 (192.168.0.2): notification-worker (JDK21 + bootJar 실행.
  소스: F:\Project\movedFromC\core\notification-worker, 로컬 git만 있음)

## RabbitMQ 토폴로지 (모두 durable, 앱이 자동 선언)
- exchange: payment.exchange (topic)
- rk payment.confirmed → payment.confirmed.queue(PC3 알림), stock.deduction.queue(PC1 재고차감)
- rk payment.failed    → payment.failed.queue(PC3 알림)
- 메시지 봉투: {messageType, payload{orderId,...,items[{sellerId,productId,amount,quantity}]}, metadata}
  JSON(ISO-8601 날짜), orderId가 자연 멱등키

## 설계 원칙 (변경 금지)
1. at-least-once + 멱등 컨슈머 — 멱등성은 도메인 기록 테이블의 UNIQUE 제약으로 (조회 후 판단 금지)
2. 발행은 트랜잭션 커밋 후에만 — @TransactionalEventListener(AFTER_COMMIT) 경유
3. 잔액/재고 갱신은 조건부 원자 UPDATE 한 문장 (read-modify-write 금지)
4. manual ack + basicReject(requeue=false) → 자동 DLQ (Phase 4에서 배선, 예외 삼킴 금지)
5. 시크릿(브로커 비번, Slack 토큰, OAuth)은 git 미추적 application.yml에만.
   템플릿: src/main/resources/application-template.yml
6. 원본 paymentModel 3종은 절대 수정하지 않는다 (읽기 전용 참고)

## 현재까지 완료 (2026-07-14)
- Phase 0/1(네트워크·브로커), Phase 2(프로듀서+AFTER_COMMIT, JUnit 3건+e2e 검증)
- Phase 3 부분: notification-worker 구현·로컬 검증·bootJar까지. PC3 실행만 남음
- 재고 차감 컨슈머: 멱등+원자UPDATE+all-or-nothing, 동시성 테스트 5건+브로커 e2e 완료
- paymentModel 분석 완료: payment=core-spa의 원형(Outbox 3단 완비, 완결 수신부는 빈 클래스),
  wallet=동작(낙관락+JVM락, 취약), ledger=미완성(저장부 없음, 컴파일 불가)

## 상세 문서 (모두 core-spa/docs/)
progress-and-roadmap.md(허브: 완료/할일/기술부채), distributed-mq-plan.md(Phase 플랜+기록),
paymentmodel-analysis.md(Kafka→RabbitMQ 매핑표+M1~M4), codebase-state.md(코드 스냅샷)
```

---

## C. 작업별 지시 프롬프트 (공통 블록 뒤에 붙이기)

### C-1. M1 — Transactional Outbox 이식 (PC1에서 실행)
```
[작업: M1 — core-spa에 Transactional Outbox 이식]
원본 F:\Project\paymentModel\payment의 Outbox 3단 구성을 core-spa로 이식하라:
1. Outbox 엔티티/테이블 (idempotencyKey=orderId UNIQUE, status INIT/SENT/FAILURE,
   payload/metadata JSON). DDL은 ddl-auto=none이므로 수동 실행 (mysql.exe 경로:
   C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe, root/1234)
2. PaymentOutboxService.insertOutbox — SUCCESS 전이 트랜잭션 안에서 outbox 저장.
   기존 PaymentStatusUpdateRepository의 주석 seam(paymentOutboxService)이 그 자리
3. RabbitDispatchEventMessageAdapter에 발행 성공/실패 시 outbox 상태 마킹 추가
4. 릴레이 스케줄러 — @Scheduled(1s)로 pending(INIT/FAILURE) outbox 재발행
5. Kafka 전용 partitionKey는 이식하지 않는다
검증: 통합 테스트(성공 발행→SENT, 브로커 다운 시뮬→FAILURE→릴레이 재발행) + 컴파일+기존 테스트 회귀.
완료 후 커밋·푸시, docs/distributed-mq-plan.md 진행 기록 갱신.
```

### C-2. M2 — settlement-worker 신규 (PC1에서 개발, PC3 배포)
```
[작업: M2 — settlement-worker (wallet 재구현)]
F:\Project\movedFromC\core\settlement-worker 신규 프로젝트(Boot 3.4.x + amqp + JPA).
notification-worker 골격 참고. DB는 PC1 MySQL(core2_spa) 접속.
- wallet.settlement.queue ← payment.exchange/payment.confirmed 바인딩 (팬아웃 추가)
- 도메인: seller_wallet(seller_id UNIQUE, balance), wallet_transaction(order_id+seller_id UNIQUE=멱등키)
- 정산: payload.items[]를 판매자별 그룹핑 → 멱등 INSERT → 잔액은 조건부 원자 UPDATE
  (원본 wallet의 read-modify-write+낙관락 방식은 비교 실험용으로만 문서화)
- 처리 후 settlement.wallet.completed 발행 (이미 처리된 중복이어도 재발행 — 원본 설계 유지)
검증: 동시성 통합 테스트(같은 판매자 동시 정산 lost update 없음, 중복 전달 1회 처리) + 브로커 e2e.
```

### C-3. M3 — ledger-worker 신규
```
[작업: M3 — ledger-worker (ledger 완성+재구현)]
원본 ledger는 미완성(DoubleLedgerEntryRecordService에 저장부·return 없음, 컴파일 불가).
도메인(DoubleLedgerEntry의 차변=대변 불변식, Ledger.createDoubleLedgerEntry)은 그대로 이식하고
저장부를 완성하라: 생성된 엔트리들 → ledger_entry/ledger_transaction 영속화(멱등: order_id UNIQUE),
완료 후 settlement.ledger.completed 발행. core2_spa의 기존 ledger_* 테이블 스키마 재활용 검토.
```

### C-4. M4 — 완결 수신부 (PC1/core-spa)
```
[작업: M4 — 결제 완결 수신부]
core-spa에 settlement.wallet.completed / settlement.ledger.completed 구독 리스너 추가.
수신 시 해당 orderId의 payment_orders.is_wallet_updated / is_ledger_updated = true →
PaymentEvent.completeIfDone() → is_payment_done. (원본 payment에서도 빈 클래스였던 부분 —
WalletEventMessageHandler/LedgerEventMessageHandler를 우리가 처음 구현하는 것)
검증: 두 통지 모두 도착했을 때만 is_payment_done=true가 되는 통합 테스트.
```

### C-5. Phase 3 완결 (PC3에서 실행 — 메모리 없는 세션)
```
[작업: notification-worker 실행 및 소비 확인]
PC3에 이미 복사된 notification-worker-0.0.1-SNAPSHOT.jar와 application.yml로:
1. java -version으로 JDK 21 확인 → java -jar 실행
2. "Started NotificationWorkerApplication" 확인
3. payment.confirmed.queue에 대기 중인 합성 메시지 2건이 즉시 소비되는지 확인
   (콘솔에 "결제 확정 이벤트 수신" 로그. Slack은 토큰 placeholder라 invalid_auth 로그가 정상)
4. 관리 UI(http://192.168.0.6:15672, coreapp/1234, vhost core_vhost)에서
   payment.confirmed.queue의 consumers=1, messages=0 확인
```

### C-6. Phase 4 — 신뢰성 (M2/M3 완료 후)
```
[작업: Phase 4 — 신뢰성 배선]
대상: wallet.settlement.queue, ledger 큐, stock.deduction.queue (돈/재고 도메인 우선)
1. 컨슈머: acknowledge-mode manual, 성공 basicAck / 실패 basicReject(requeue=false)
2. 큐에 x-dead-letter-exchange=payment.dlx → 각 .dlq 큐 (자동 데드레터링 — 수동 DLX 발행 금지)
3. 프로듀서: publisher-confirm-type correlated + 콜백에서 outbox FAILURE 마킹
4. 재고 차감의 "재고부족 시 로그 후 ack(소실)" 정책을 reject→DLQ로 교체
DoD: 강제 예외 → DLQ 적재 확인 / 같은 메시지 2회 전달 → 부작용 1회.
```

---

## D. 프롬프트 작성 가이드라인 (새 템플릿을 만들 때)

1. **메모리 없는 세션 = 신뢰 가능한 사실만**: 경로·IP·계정·큐 이름은 전부 명시. "지난번처럼" 같은 표현 금지.
2. **DoD를 프롬프트에 포함**: 무엇이 확인되면 끝인지 한 줄로.
3. **검증 없는 완료 없음**: 컴파일→테스트→(가능하면) 브로커 e2e→커밋 순서를 지시에 포함.
4. **소비(consume)가 일어나는 작업은 경고 포함**: auto-ack 컨슈머 기동은 큐를 즉시 비운다. 남겨야 할 메시지가 있으면 프롬프트에 명시할 것.
5. **원본 수정 금지 조항 유지**: paymentModel 3종은 읽기 전용.
6. 완료 시 갱신할 문서(progress-and-roadmap.md, distributed-mq-plan.md 진행 기록)를 지시에 포함.
```
