# Phase 5 — 장애 주입 실험 결과

`docs/distributed-mq-plan.md` §4 Phase 5의 DoD(실험 3종 결과 기록)를 만족하기 위한 문서.

## 실험 A — 발행 중 PC2(브로커) 차단

**목적**: 프로듀서(PC1, core-spa)가 outbox 메시지를 발행하려는 시점에 브로커가 응답하지 않을 때, 메시지가 유실되지 않고 연결 복구 후 자동으로 재발행되는지 확인한다.

**방법**: PC2를 물리적으로 끄는 대신, PC1의 Windows 방화벽에서 `192.168.0.6:5672`(RabbitMQ)로 나가는 아웃바운드 트래픽을 차단해 "브로커 도달 불가" 상태를 재현했다. 프로듀서 관점에서는 실제 PC2 다운과 동일한 증상(연결 실패)이다.

```powershell
New-NetFirewallRule -DisplayName "core-mq-exp-A-block" -Direction Outbound -RemoteAddress 192.168.0.6 -RemotePort 5672 -Protocol TCP -Action Block
```

테스트 메시지는 `payment.failed`(단일 큐 `payment.failed.queue`에만 바인딩, 재고/정산/장부 팬아웃 없음)로 골라 부수효과 없이 순수 발행 경로만 검증했다. outbox 테이블에 `INIT` 상태 행을 직접 삽입해 릴레이(`PaymentEventMessageRelayService`, 1초 주기 / 10초 유예)가 재발행을 시도하도록 유도했다.

**진행 및 관찰**:
1. 방화벽 차단 적용 확인 (`Test-NetConnection 192.168.0.6:5672` → `False`).
2. `INIT` 상태 outbox 행 삽입 (`orderId=exp-a-pc2-block-1784097383`).
3. 10초 유예 경과 후 릴레이가 발행을 시도 → 브로커 연결 실패 → 예외를 catch하고 `FAILURE`로 마킹 (16초 후 확인, 상태 `FAILURE`).
   - `payment.failed.queue` 메시지 수: 0건 (유실도, 오발행도 없음).
4. 방화벽 규칙 제거, 연결 복구 확인 (`Test-NetConnection` → `True`).
5. 다음 릴레이 주기(약 3초 이내)에서 자동 재시도 → 발행 성공 → `SUCCESS`로 전환 (사람 개입 없음).
6. `payment.failed.queue.dlq` 메시지 수: 0건 — notification-worker가 정상 소비, 재전달/데드레터 발생 없음.

**결론**: at-least-once + Transactional Outbox 설계가 의도대로 동작함을 확인.
- 브로커 불능 구간에서도 메시지가 `INIT`/`FAILURE` 상태로 DB에 보존되어 유실되지 않는다.
- 릴레이가 예외를 삼키지 않고 항상 `FAILURE`로 명시 마킹하므로 다음 주기에 자동 재시도 대상에 계속 남는다(무한 방치 없음).
- 연결 복구 시 별도 트리거 없이 스케줄러가 자동으로 따라잡는다 — 운영자가 수동으로 "재발행 버튼"을 누를 필요가 없는 설계.

**한계**: 이번 재현은 PC1측 아웃바운드 차단이라 PC2 프로세스 자체는 살아있었다. RabbitMQ 서버 프로세스 자체가 죽는 경우(연결이 아예 리셋되는 경우)도 Spring AMQP의 연결 재시도 로직이 동일하게 동작할 것으로 예상되나, 별도 실증은 하지 않았다.

---

## 실험 B — 메시지 적재 후 PC3 다운 → 재기동 시 소비 재개

**상태**: ✅ 완료 (2026-07-15, 대상: ledger-worker / `ledger.recording.queue`).

**방법**: PC3에서 ledger-worker(+격리를 위해 settlement-worker도 함께) 정상 중지 → `ledger.recording.queue`에 컨슈머 없는 상태에서 정상 처리 가능한 유효 메시지 5건(`amount` 숫자, orderId=`exp-b-ledger-*`) 직접 발행 → PC3에서 ledger-worker 평소 방식대로 재기동.

**관찰**:
1. 워커 중지 직후: `ledger.recording.queue` consumers=0 확인.
2. 5건 발행 후: `messages_ready=5, consumers=0` — durable 큐라 컨슈머 없이도 메시지가 유실 없이 쌓임.
3. 재기동 명령 후 **약 7~10초 뒤**에 consumer가 붙음(Spring Boot 컨텍스트+DB+AMQP 연결 부팅 시간) — 이 타이밍 데이터가 실험 C의 kill 지연값 설계 근거가 됨(고정 300ms는 부팅 시간보다 짧아 무의미하다는 게 실측으로 확인됨).
4. consumer 연결과 동시에 5건 모두 즉시 소비, `messages_ready` 0으로 드레인.
5. DB 확인: `ledger_transactions` 5행(주문 5건 전부 매칭) + `ledger_entries` 10행(건당 차변/대변 정확히 2행) — 유실·누락 없이 정확히 반영.

**결론**: durable queue + persistent message 설정이 의도대로 동작. 컨슈머 다운 기간에도 메시지가 브로커에 안전하게 보존되고, 재기동 시 별도 개입 없이 즉시 캐치업 소비된다.

---

## 실험 C — 소비 도중 PC3 강제 종료(ack 전) → 재전달 + 멱등 확인

**상태**: ⚠️ 미확정 (2026-07-15, 3회 시도, 재전달 재현 실패). PC3 접근이 필요해 사용자가 직접 kill 스크립트를 실행하는 방식으로 진행.

**방법**: "관리 API로 consumer 연결 감지 → 짧은 지연 → `Stop-Process -Force`"로 ledger-worker를 처리 도중 강제 종료해, ack 전 미확인 메시지가 재큐잉·재전달되는지 관찰.

**시도 및 원인 분석**:
1. **1차(30건, 200ms 지연)**: `redeliver=0`. 원인 — RabbitMQ 관리 API의 `consumers`/`messages` 필드가 실시간이 아니라 서버 내부 통계 집계 주기(기본 5초)로만 갱신됨. 30건 처리가 1.3초(≈45ms/건) 만에 끝나버려, 폴링이 "연결됨"을 감지했을 땐 이미 처리가 전부 끝난 뒤였음. 게다가 `Stop-Process -Id $p.Id -Force`가 대상 PID를 죽이지 못한 것도 사후 확인(`Get-Process`, PC2 연결 목록에 동일 커넥션명이 계속 살아있음)됨.
2. **2차(500건으로 증량, 동일 지연)**: 처리 시간을 21초로 늘려 통계 지연을 압도하도록 설계했으나, 여전히 `redeliver=0`. 사후 확인 결과 PC3에 정리 안 된 좀비 프로세스(1차 시도의 PID와 별개로 이전 세션에서 남아있던 프로세스)가 미리 연결돼 있다가 발행 즉시 메시지를 전부 채감. **교훈: PC3 실험 전엔 `Get-Process java`로 잔여 프로세스가 정말 0개인지 반드시 확인.**
3. **3차(좀비 완전 정리 후 500건 재발행)**: PC3 java 프로세스 전부 종료 확인(`Get-Process java` 빈 결과) 후 재시도. 이번엔 신규 연결(새 포트)이 처음부터 끝까지 500건을 끊김 없이 처리 — 모니터링 중 8~12초 구간에 진행이 멈춘 것처럼 보였으나, 이는 위와 동일한 관리 API 통계 집계 지연(4초 간격 폴링과 5초 집계 주기가 우연히 겹침)에 의한 착시로 판단됨(`redeliver`는 시종 0, DB에 500/500 정확·중복 없음). **kill이 처리 완료 후에 걸렸거나 재차 무효화된 것으로 추정되나, 정확한 원인은 스크립트 자체 콘솔 로그(연결 감지/kill 시각)로 확정하지 못함.**

**결론**: 이 실험이 겨냥한 "재전달 자체"는 재현하지 못했다. 다만 관련된 신뢰성 메커니즘의 다른 축은 이미 별도로 검증되어 있다 — manual ack + `basicNack(requeue=false)` 기반 DLQ 이동은 Phase 4 e2e(x-death 헤더 확인)와 M1~M4 전 구간에서 실증됐고, 멱등 가드(UNIQUE 제약)는 M1~M3에서 반복 검증됨. 재전달 트리거(연결 강제 종료) 자체를 사람이 손으로 정밀하게 재현하는 난도가 이 환경의 한계로 판단해 이 실험은 여기서 보류한다.

**향후 재시도 시 고려사항**: (1) 관리 API 대신 애플리케이션 로그 파일을 실시간 tail해 "Started" 로그 직후 kill하는 방식으로 전환, (2) `Stop-Process -Force`가 왜 간헐적으로 무효화되는지 별도 재현·조사, (3) 또는 코드 레벨에서 리스너에 임시 `Thread.sleep`을 넣어 처리 시간을 인위적으로 늘리는 방식(가장 확실하지만 프로덕션 코드를 건드려야 함).

## 요약

| 실험 | 상태 | 핵심 결론 |
|---|---|---|
| A. 발행 중 브로커 차단 | ✅ 완료 (2026-07-15) | Outbox+릴레이로 유실 없이 자동 복구 |
| B. 적재 후 컨슈머 다운 | ⬜ 대기 (PC3 수동 작업) | - |
| C. ack 전 컨슈머 강제종료 | ⚠️ 보류 (3회 시도, 재전달 미재현) | 재전달 관측은 실패했으나 DLQ/멱등 메커니즘은 다른 실험에서 이미 검증됨 |
