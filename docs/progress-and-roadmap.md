# core-mq 진행 현황 & 로드맵 (마스터 문서)

> 기준일: 2026-07-14. 이 문서가 전체 현황의 허브다. 각론은 아래 문서 참조:
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
| **M1 (다음)** | core-spa에 **Transactional Outbox 이식** — Outbox 엔티티/테이블, insertOutbox(전이 트랜잭션 내), Sender sent/failure 마킹, 1s 릴레이 스케줄러 | 기존 주석 seam이 정확히 이 자리. partitionKey는 제거 |
| M2 | **settlement-worker** 신규(PC3) — 판매자 지갑 정산. 원자 UPDATE+UNIQUE 멱등 기본, 원본(낙관락) 방식은 비교군. 처리 후 `settlement.wallet.completed` 발행 | Phase 4 배선 대상 |
| M3 | **ledger-worker** 신규 — 복식부기 도메인 이식 + 원본 미완성 저장부 완성. `settlement.ledger.completed` 발행 | DB의 기존 ledger_* 테이블 재활용 검토 |
| M4 | core-spa가 completed 2종 구독 → `is_wallet_updated`/`is_ledger_updated` → `completeIfDone()` | 원본에서도 빈 클래스였던 최종 완결 |

### B. 기존 Phase 마무리 (병행 가능)
- **Phase 3 완결**: PC3에서 `java -jar` 실행 → PC1 결제 → PC3 소비 확인 (payment.confirmed.queue에 합성 메시지 2건 대기 중 — 첫 소비 테스트용). Slack 토큰은 여유 될 때
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
