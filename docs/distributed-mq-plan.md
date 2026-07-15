# Core-spa 분산 메시징 전환 기획안 (core-mq)

작성일: 2026-07-14 / 작성: Claude (자율 기획안)

## 1. 배경과 목표

core-spa는 Toss 결제 흐름까지 구현된 북서비스 커머스 백엔드지만,
`PaymentConfirmService` 이후의 **outbox/메시지 발행, wallet/ledger 갱신이 미완(주석)** 상태다.
한편 projectRabbitMq(step0~11)에서 DLX/DLQ + Slack 알림 파이프라인을 학습했다.

이 프로젝트의 목표는 둘을 합치는 것:

> **결제 확정 이벤트를 RabbitMQ로 발행하고, 별도 PC의 워커가 소비하는
> 실제 멀티 인스턴스(2~3대) 분산 메시징 환경을 구축한다.**

부수 목표:
- step11에서 발견한 결함(컨슈머 예외 삼킴 → 자동 데드레터링 미동작)을 **반복하지 않고 교정**해서 적용
- 장애 주입 실험(브로커 다운, 컨슈머 다운, 네트워크 단절)으로 메시지 유실/중복 동작을 직접 관찰

## 2. 물리 배치

| 머신 | 역할 | 근거 |
|---|---|---|
| PC1 (고성능, 개발환경 완비) | core-spa + MySQL (프로듀서) | JVM 힙 + DB 버퍼가 가장 무거움 |
| PC2 (저사양, 미세팅) | RabbitMQ 브로커 전담 | 브로커는 유휴 RAM 100~200MB로 가벼움. 전담 배치로 장애 원인 분리 |
| PC3 (저사양) | notification-worker (컨슈머) | 워커는 이벤트 유입 시에만 부하. JDK+jar만으로 실행 |

원칙:
- PC2/PC3는 공유기 DHCP 예약으로 **고정 IP**
- PC2에 Docker Desktop 금지(저사양 Windows에서 WSL2가 브로커보다 무거움) → Erlang+RabbitMQ 네이티브 설치
- PC2/PC3 절전모드/자동 업데이트 재부팅 해제

## 3. 핵심 설계 결정

1. **첫 유스케이스**: `PaymentConfirmService`의 SUCCESS 전이 직후 `payment.confirmed` 이벤트 발행.
   (core-spa에 이미 "메시지 발행" 자리가 비어 있음 — 새 도메인을 만들지 않고 미완 흐름을 완성)
2. **전달 보장**: at-least-once + **멱등 컨슈머**.
   core-spa가 이미 멱등성 키(order_id UUID) 설계를 갖고 있으므로, 워커도 order_id 기준 중복 처리 방지 테이블로 일관성 유지.
3. **데드레터링은 자동으로**: 컨슈머는 예외를 삼키지 않는다.
   manual ack + `basicReject(requeue=false)` → 큐의 `x-dead-letter-exchange`로 자동 이동.
   (step11의 수동 DLX 발행 방식 폐기)
4. **토폴로지** (2026-07-14 Phase 2 구현 시 확정, 6절 진행 기록 참고):
   - vhost: `core_vhost` (전용, 기본 `/` 대신 분리)
   - exchange: `payment.exchange` (topic, durable)
   - routing key: `payment.confirmed`, `payment.failed`
   - queue: `payment.confirmed.queue`, `payment.failed.queue` (durable)
   - DLX(Phase 4에서 구현 예정): `payment.dlx` → `payment.confirmed.dlq`/`payment.failed.dlq` → Slack 경보
5. **보안**: guest 계정은 localhost 전용이므로 사용 불가. 전용 계정 `coreapp` 생성.
   접속 정보·Slack 토큰은 환경변수로만 주입, 커밋 금지.
6. **직렬화**: Jackson JSON (`Jackson2JsonMessageConverter`), 메시지에 `eventId`, `occurredAt`, `orderId` 필수 포함.

## 4. 단계별 마일스톤 (Phase 0~6)

각 Phase는 완료 기준(DoD)을 만족해야 다음으로 진행.

### Phase 0 — 네트워크 기반
- PC2/PC3 고정 IP(DHCP 예약), 방화벽 인바운드 5672/15672 허용(PC2)
- **DoD**: PC1에서 `Test-NetConnection <PC2_IP> -Port 5672` 성공

### Phase 1 — PC2 브로커 구축
- Erlang → RabbitMQ 네이티브 설치, `rabbitmq_management` 플러그인 활성화
- `coreapp` 계정 생성 + 권한/administrator 태그, Windows 서비스 자동 시작 확인
- 전용 vhost `core_vhost` 생성 + `coreapp` 권한 부여 (기본 `/` vhost 대신 분리, 2026-07-14 결정)
- **DoD**: PC1 브라우저에서 `http://<PC2_IP>:15672` 로그인 성공, `core_vhost` 노출 확인

### Phase 2 — core-spa 프로듀서 (PC1)
- `spring-boot-starter-amqp` 추가, 토폴로지 선언(@Configuration), 접속정보 환경변수화
- `PaymentConfirmService` SUCCESS 전이 후 `payment.confirmed` 발행 (트랜잭션 커밋 후 발행 시점 주의 — `@TransactionalEventListener(AFTER_COMMIT)` 경유 권장)
- **DoD**: 결제 confirm 호출 → 관리 UI(`core_vhost`)에서 `payment.confirmed.queue`에 메시지 1건 적재 확인

### Phase 3 — PC3 워커 구축
- 신규 프로젝트 `notification-worker` (Boot 3.x, amqp + slack), PC1에서 개발 → `bootJar` 산출물만 PC3로 배포
- PC3에는 JDK만 설치. 소비 → Slack 알림 (step11 SlackNotifier 패턴 재사용, `.block()` 대신 동기 클라이언트 or 타임아웃 명시)
- **DoD**: PC1 결제 → PC2 경유 → PC3 Slack 알림 end-to-end 성공

### Phase 4 — 신뢰성 강화
- publisher confirms + returns 콜백, 컨슈머 manual ack, 자동 DLQ 배선 검증
- 워커에 `processed_event(event_id unique)` 테이블로 멱등 처리
- **DoD**: 워커에서 강제 예외 발생 시 재시도 후 `payment.confirmed.dlq` 적재 + Slack 경보. 같은 메시지 2회 전달 시 부수효과 1회만 발생

### Phase 5 — 장애 주입 실험 (본 프로젝트의 학습 핵심)
- 실험 A: 발행 중 PC2 전원 차단 → confirms 실패 처리 관찰
- 실험 B: 메시지 적재 후 PC3 다운 → 재기동 시 소비 재개(durable+persistent) 확인
- 실험 C: 소비 도중 PC3 강제 종료(ack 전) → 재전달 + 멱등 처리 확인
- **DoD**: 실험 3종 결과를 `docs/failure-experiments.md`에 기록

### Phase 6 (선택 확장)
- Transactional Outbox 패턴 (발행 유실 원천 차단)
- 기존 Prometheus/Grafana 스택에 rabbitmq_prometheus 연동, k6로 발행 부하 실험
- wallet/ledger 갱신 컨슈머 추가 (두 번째 워커 = PC3 또는 PC2 여유분)

## 5. 리스크 및 대응

| 리스크 | 대응 |
|---|---|
| guest 계정 원격 접속 불가 | Phase 1에서 전용 계정 생성 (기본 규칙) |
| Windows 방화벽이 5672 차단 | Phase 0 DoD로 선검증 |
| PC2 절전/자동 재부팅으로 브로커 소실 | 전원 옵션 변경 + RabbitMQ 서비스 자동시작 |
| 공유기 재부팅 시 IP 변동 | DHCP 예약(고정 IP) |
| Slack 토큰/브로커 비밀번호 커밋 | 환경변수 주입, yml에는 placeholder만 (SocialLoginSpa 교훈) |
| 트랜잭션 롤백됐는데 메시지는 발행됨 | AFTER_COMMIT 발행, Phase 6에서 Outbox로 완결 |
| 컨슈머 예외 삼킴으로 DLQ 미동작 | manual ack + reject(requeue=false) 강제, Phase 4 DoD로 검증 |

## 6. 진행 기록

- 2026-07-14: 기획안 작성. Phase 0 착수 전.
- 2026-07-14: Phase 0/1 완료. Phase 2 착수 전 core-spa master 코드 점검·정리(3건 수정, `docs/codebase-state.md` 작성).
- 2026-07-14: Phase 2 구현. vhost는 기본 `/` 대신 전용 `core_vhost`로, 토폴로지 네이밍은 `payment.events`/`q.notification.payment` 대신 `payment.exchange`/`payment.confirmed.queue`·`payment.failed.queue`로 확정(원안 대비 변경, 3.4절 반영). PC2에는 vhost·권한 설정만 추가로 필요.
- 2026-07-14: Phase 2 연결/토폴로지 검증 완료. PC1에서 core-spa 기동 → PC2(`core_vhost`)에 `payment.exchange`, `payment.confirmed.queue`, `payment.failed.queue` 자동 선언 확인(관리 API로 직접 조회). 단, RabbitAdmin의 선언은 **앱 기동 시 즉시가 아니라 첫 실제 AMQP 연결 시점(lazy)** 에 일어남 — 즉 실제로는 첫 결제 confirm(또는 admin 호출) 시 처음 나타남. 실제 결제 confirm 흐름(OAuth 로그인 → 장바구니 → checkout → confirm)을 통한 end-to-end 메시지 도착 확인은 아직 미실시(수동 테스트 필요).
- 2026-07-14: (환경 메모, 코드 변경 아님) 로컬 MySQL 8 연결 시 `Public Key Retrieval is not allowed` 에러 발생 — `application.yml`(git 미추적) datasource url에 `allowPublicKeyRetrieval=true` 추가로 해결. 이 파일은 커밋되지 않으므로 PC1을 새로 세팅할 경우 동일 조치 필요.
- 2026-07-14: Phase 2 경로 A 검증 완료 — 가짜 paymentKey confirm → FAILURE 전이 → `payment.failed.queue` 적재 + 페이로드 정합(UTF-8 바이트 검증) 확인. Phase 3 착수: notification-worker 신규 프로젝트(별도 git repo) 구현·기동, `payment.failed` 3건 실소비 및 Slack 호출 시도 확인(토큰 placeholder라 invalid_auth — 정상 실패 처리). Slack 토큰 발급·PC3 배포는 사용자 작업으로 대기.
- 2026-07-14: **재고 차감 컨슈머 구현** (RabbitMQ 실용 도메인 1호) — `stock.deduction.queue`를 `payment.confirmed`에 팬아웃 바인딩, core-spa 자신이 소비. 멱등성(stock_deduction UNIQUE)·조건부 원자 UPDATE(lost update/음수 차단)·주문 단위 all-or-nothing. 동시성 통합 테스트 5건 + 브로커 경유 e2e(합성 발행 → 재고 50→48, 중복 발행 → 48 유지) 검증 완료. DDL 2건 수동 적용(payment_orders.quantity, stock_deduction 테이블).
- 다음 계획: paymentModel 3종(payment/wallet/ledger, Kafka 기반) 분석 → RabbitMQ 재구현. payment은 core-spa 이식, wallet/ledger는 PC2/PC3 배포 예정.
- 2026-07-15: M1~M4 로드맵 전체 완결(코드+테스트+브로커e2e) — Outbox 이식, settlement-worker/ledger-worker 신규, core-spa 완결 수신부. settlement-worker/ledger-worker git 초기화·GitHub 푸시 완료.
- 2026-07-15: **Phase 4 신뢰성 강화** — core-spa/settlement-worker/ledger-worker/notification-worker 4개 프로젝트 전부에 동일 패턴 적용: (1) 공유 DLX `payment.dlx`(direct exchange) + 큐마다 `<큐이름>.dlq`, 각 큐에 `x-dead-letter-exchange`/`x-dead-letter-routing-key` 인자 부여. 같은 큐를 여러 프로젝트가 선언하는 경우(payment.confirmed/failed.queue ↔ core-spa+notification-worker, settlement/ledger completed.queue ↔ worker+core-spa) 인자를 한 글자도 다르지 않게 맞춤(안 그러면 406 PRECONDITION_FAILED). (2) 모든 `@RabbitListener`가 `AcknowledgeMode.MANUAL` 컨테이너 팩토리 사용, 성공 시 `channel.basicAck`, 예외 시 `channel.basicNack(tag, false, false)`로 즉시 DLQ 적재. **notification-worker의 과거 무한 재전달 루프(Slack 토큰 오류 시 5만+회)의 근본 원인**이 바로 이 auto-ack+예외 미처리였음 — 이번 수정으로 해결. (3) 각 프로듀서(core-spa/settlement-worker/ledger-worker) RabbitTemplate에 `setMandatory(true)`+confirm/returns 콜백(로그) 배선, `application.yml`에 `publisher-confirm-type: correlated`/`publisher-returns: true`. processed_event 테이블은 스킵(기존 order_id/seller_id UNIQUE 멱등 가드로 충분하다고 판단). Slack DLQ 경보는 이번 범위 밖(후속). 4개 프로젝트 전부 컴파일+테스트(gradle test) 통과 확인. **주의**: SettlementService.settle()/LedgerRecordingService.record()는 판매자·항목 단위로 예외를 내부에서 swallow하고 항상 완결 메시지를 반환하는 기존 설계라, 부분 실패가 리스너까지 전파되지 않아 이번에 추가한 DLQ가 그 경로에서는 발동하지 않는다(전체 처리 자체가 실패하는 경우에만 DLQ행). 별도 검토 필요.
- 잔여(Phase 4): 실브로커에서 강제 예외 → DLQ 적재 확인(PC2 필요), Phase 5 장애 주입 실험, Phase 6(선택).
