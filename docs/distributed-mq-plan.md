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
4. **토폴로지**:
   - exchange: `payment.events` (topic, durable)
   - routing key: `payment.confirmed`, `payment.failed`
   - queue: `q.notification.payment` (durable, x-dead-letter-exchange=`dlx.payment`)
   - DLX: `dlx.payment` → `q.dead.payment` → Slack 경보
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
- **DoD**: PC1 브라우저에서 `http://<PC2_IP>:15672` 로그인 성공

### Phase 2 — core-spa 프로듀서 (PC1)
- `spring-boot-starter-amqp` 추가, 토폴로지 선언(@Configuration), 접속정보 환경변수화
- `PaymentConfirmService` SUCCESS 전이 후 `payment.confirmed` 발행 (트랜잭션 커밋 후 발행 시점 주의 — `@TransactionalEventListener(AFTER_COMMIT)` 경유 권장)
- **DoD**: 결제 confirm 호출 → 관리 UI에서 `q.notification.payment`에 메시지 1건 적재 확인

### Phase 3 — PC3 워커 구축
- 신규 프로젝트 `notification-worker` (Boot 3.x, amqp + slack), PC1에서 개발 → `bootJar` 산출물만 PC3로 배포
- PC3에는 JDK만 설치. 소비 → Slack 알림 (step11 SlackNotifier 패턴 재사용, `.block()` 대신 동기 클라이언트 or 타임아웃 명시)
- **DoD**: PC1 결제 → PC2 경유 → PC3 Slack 알림 end-to-end 성공

### Phase 4 — 신뢰성 강화
- publisher confirms + returns 콜백, 컨슈머 manual ack, 자동 DLQ 배선 검증
- 워커에 `processed_event(event_id unique)` 테이블로 멱등 처리
- **DoD**: 워커에서 강제 예외 발생 시 재시도 후 `q.dead.payment` 적재 + Slack 경보. 같은 메시지 2회 전달 시 부수효과 1회만 발생

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
