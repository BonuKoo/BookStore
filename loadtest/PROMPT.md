# [실행 프롬프트] Phase 6 k6 부하 테스트 재개 — 포트폴리오 수치 확보

> 이 문서를 새 세션에 그대로 붙여 넣으면 컨텍스트 없이 이어서 수행할 수 있다.
> 목표: **HikariCP 튜닝 전→후 개선 서사 + 시스템 수용 한계 곡선 + 파이프라인 드레인 실측**을
> 구체적 수치로 확보해 포트폴리오에 기재한다.

---

## 0. 배경 (단일 진실원)

- core-spa 결제 확정 흐름: `POST /v1/toss/confirm` → 상태 전이 → Transactional Outbox(릴레이 1s 주기)
  → RabbitMQ(PC2) → 워커 3종(PC3: notification/settlement/ledger) → 완결 수신(is_payment_done).
- loadtest 프로파일: `LoadTestPaymentExecutorStub`(@Primary)이 Toss 실호출을 대체. 이후 경로는 전부 실코드.
- **1차 실행 결과 (2026-07-15, 이 수치가 "개선 전" baseline)**:
  - 1000건 시드, VU 20, shared-iterations
  - 성공 **2/602 (0.33%)**, **p95 = 60s** (타임아웃)
  - 원인: **HikariCP 기본 풀 10개 고갈** (`total=10, active=10, waiting=45` → CannotCreateTransactionException)
- 하니스: `loadtest/seed_orders.py`(시드) / `confirm_load.js`(부하) / `verify_pipeline.py`(드레인·정합성 검증)

## 토폴로지 / 실행 위치

| PC | IP | 역할 | 부하 테스트 시 역할 |
|---|---|---|---|
| PC1 | 192.168.0.5 | core-spa + MySQL(127.0.0.1:3306, core2_spa) | SUT — loadtest 프로파일로 기동 |
| PC2 | 192.168.0.6 | RabbitMQ (coreapp:1234, vhost=core_vhost, 5672/15672) | SUT — 손대지 않음 |
| PC3 | 192.168.0.2 | 워커 3종 jar (/c/app/*.jar) | **k6 실행 위치** (`BASE_URL=http://192.168.0.5:8080`) |

k6를 PC3에서 돌리는 근거: PC2는 RAM 8GB로 RabbitMQ 메모리 워터마크(~3.2GB)에 여유가 없어
k6를 얹으면 memory alarm → 퍼블리셔 블로킹으로 측정이 오염된다. PC3는 free RAM ~8GB로 충분.
단, 테스트 중 PC3 CPU가 90%+ 포화되면 k6 자체가 병목 → 그 회차 결과는 불신하고 VU를 낮춘다.

---

## 1. 사전 점검 체크리스트 (매 실행 전 필수 — 전부 실전에서 발생했던 이슈)

1. **PC2 생존 확인**: ping 192.168.0.6 + 관리 API `GET http://192.168.0.6:15672/api/overview`
   (PC2는 절전으로 자동 다운되는 이력 반복 — 전원옵션 확인).
2. **PC1 앱 재기동**: 1차 부하 후 loadtest 인스턴스가 PC2 연결 끊김(getsockopt timeout) 상태로
   방치된 적 있음. 반드시 새로 기동: `./gradlew bootRun --args='--spring.profiles.active=loadtest'`
3. **PC3 좀비 프로세스 정리**: `Get-Process java`로 의도한 워커 3종 외 프로세스가 없는지 확인
   (Phase 5 실험 C에서 좀비가 몰래 소비해 결과를 오염시킨 실사례 있음).
4. **워커 연결 확인**: 관리 API로 전 메인 큐 `consumers=1`, 적체 0 확인.
5. **DLQ 초기화 상태 기록**: 시작 전 각 `.dlq` 건수를 기록해 두고 증가분만 판정.
6. **MySQL `max_connections` 확인**: HikariCP 풀을 키우기 전에
   `SHOW VARIABLES LIKE 'max_connections'` (기본 151) — 풀 크기가 이를 넘으면 안 됨.

## 2. 선행 조치: HikariCP 튜닝 (개선 조치 — 이게 "후" 수치를 만든다)

- `application-loadtest.yml`에만 적용 (운영 프로파일은 건드리지 않는다):
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 30      # 시작값. VU 20 + outbox 릴레이 + 완결 수신 동시성 감안
        connection-timeout: 5000   # 기본 30s는 대기열만 키움 — 빨리 실패시켜 병목을 드러낸다
  ```
- 30으로 시작해 VU 20이 깨끗이 통과하면 유지. p95가 여전히 나쁘면 50까지만 올려보고,
  그래도 나쁘면 풀이 아니라 다음 병목(행 잠금/CPU)이므로 풀을 더 키우지 말 것.

## 3. 사용자 시나리오 → k6 시나리오 매핑

| # | 사용자 시나리오 | k6 구현 | 목적 |
|---|---|---|---|
| S1 | 정상 결제 확정 (baseline) | 기존 `confirm_load.js` — shared-iterations, 주문당 정확히 1회, VU 20 | 전→후 비교의 "후" 수치. 1차와 **동일 조건**(1000건/VU20)이어야 비교가 성립 |
| S2 | 트래픽 급증 (한계 탐색) | `ramping-arrival-rate`: 5→10→20→40 iter/s 계단, 각 2분 | 수용 한계 곡선. p95<2s가 깨지는 지점 = 시스템 용량 |
| S3 | 사용자 더블클릭/재시도 (멱등) | 별도 스크립트: 동일 주문에 confirm 2연타 (전체의 ~10% 주문) | 멱등 가드 실증 — HTTP는 어떻게 응답하든 **DB 1행**이면 성공 |
| S4 | 비정상 요청 혼입 | S1에 5% 비율로 amount 불일치/없는 orderId 주입 | 오염 격리 실증 — 4xx로 거절되고 **정상 건 처리량에 영향 없음** + DLQ 증가 0 |
| S5 | (선택) 지속 부하 soak | constant-arrival-rate 10 iter/s × 15분 | 커넥션 누수/메모리 증가 추세 확인 |

**시나리오 설계 원칙**:
- S1은 closed model(shared-iterations)로 1차와 조건을 맞추고, **S2는 open model(arrival-rate)로 전환**한다.
  closed model은 서버가 느려지면 VU가 응답을 기다리며 부하도 같이 줄어 한계 측정이 왜곡된다
  (coordinated omission). 도착률 고정이 서버 한계 측정에 올바른 모델.
- 각 시나리오 사이에는 반드시 `verify_pipeline.py`로 **드레인 완결을 확인한 후** 다음을 시작한다
  (이전 시나리오의 잔여 메시지가 다음 측정을 오염시키지 않도록).

## 4. 더미 데이터 설계

- **시드**: `/c/Python313/python seed_orders.py <N>` — `lt-` 프리픽스 주문만 정리·재생성(실데이터 무영향),
  ID 1천만 대역(시퀀스 충돌 없음), 전용 상품 `loadtest-isbn-001` 재고를 N 이상으로 리셋.
- **시나리오별 N**:
  - S1: 1000건 (1차와 동일)
  - S2: 3000건 (램프 총량이 소진하고 남을 만큼 — 부족하면 iterations 고갈로 램프가 조기 종료됨)
  - S3: 500건 (그중 50건을 2연타 대상으로)
  - S4: 1000건 + 비정상 요청은 시드 없이 생성 (없는 orderId `lt-bogus-*`, amount+1 변조)
- **단일 상품 vs 다상품 (추가 비교축)**: 현재 시드는 상품 1개 → 재고 차감 조건부 UPDATE가
  같은 행에 몰려 **행 잠금 경합이 병목이 되는지 자체가 실험 포인트**. S1 통과 후
  시더를 확장해 상품 10개 분산 시드로 동일 부하를 재실행하면
  "핫 로우 경합이 p95에 미치는 영향"이라는 포트폴리오급 비교가 하나 더 나온다.

## 5. 측정·비교 수치 (포트폴리오 표의 열이 될 것들)

**A. 전→후 표 (핵심 서사)** — 동일 조건 1000건/VU20:

| 지표 | 개선 전 (실측 완료) | 개선 후 (측정할 것) |
|---|---|---|
| 성공률 | 0.33% (2/602) | ? (목표 100%) |
| p50 / p95 / p99 | p95=60s | ? |
| 처리량 (req/s) | 사실상 0 | ? |
| HikariCP | pool=10 고갈 (waiting=45) | pool=30, waiting=? |

**B. 수용 한계 곡선 (S2)** — 도착률별 p95/성공률 표 → "이 시스템은 X req/s까지 p95 2s 이내" 한 문장.

**C. 파이프라인 지표 (verify_pipeline.py)**:
- 드레인 시간: 마지막 confirm → 전건 `is_payment_done` (전체 소요 + msg/s 환산)
- 큐 최대 적체 깊이 (관리 API — 단, 통계는 5초 집계 지연이 있으니 추세만 신뢰)
- 정합성: stock/wallet/ledger 행 수 == N, 재고 감소량 == N, 분개 == 2N, **DLQ 증가 0**
- **HTTP p95와 e2e 드레인의 분리 서술**: "동기 응답은 Xms, 비동기 완결은 Y초 — 이 간극이
  MQ 도입으로 사용자 대기에서 분리해 낸 작업량"이라는 아키텍처 논거가 됨.

**D. 자원 지표 (수동 관찰, 각 회차당 1회 기록)**:
- PC1: CPU / HikariCP active·waiting (로그) / MySQL Threads_running
- PC2: 관리 UI 메모리 사용량 (워터마크 근접 여부)
- PC3: CPU (k6 병목 판정용)

## 6. 예외 상황 대비 (발생 시 대응 규칙)

| 예외 | 징후 | 대응 |
|---|---|---|
| HikariCP 재고갈 | CannotCreateTransactionException, waiting 급증 | 즉시 중단 → 풀 50으로 상향 1회만 재시도. 그래도 재발이면 풀이 아닌 다음 병목 — DB 프로세스리스트/행 잠금 조사로 전환 |
| RabbitMQ memory alarm | 발행 블로킹, 관리 UI 빨간 alarm | 테스트 무효 처리. PC2에 다른 프로세스 없는지 확인 후 재실행 |
| PC2 절전 다운 | mid-test 연결 끊김 | 결과 폐기. 전원옵션 조치 후 처음부터 |
| PC3 CPU 포화 | 90%+ 지속 | 해당 회차 지연 수치 불신. VU/도착률 낮춰 재실행 |
| outbox 적체 | confirm 성공인데 브로커 유입 없음 | 릴레이는 1s 주기 — 10s 유예까지는 정상. 그 이상이면 릴레이 로그 확인 |
| 시나리오 중단(Ctrl+C) | 잔여 메시지/미완 주문 | 방치 금지 — verify_pipeline.py로 드레인 확인 → 재시드 후 재시작 |
| 관리 API 수치 이상 | consumers=0으로 보임 등 | 통계 5초 집계 지연 착시 가능 — 5초 후 재조회로 재확인 |
| 워커 크래시 | 특정 큐만 적체 지속 | 큐는 durable — 유실 아님. PC3 재기동 후 드레인 확인 (Phase 5 실험 B에서 검증된 경로) |

## 7. 실행 순서 요약

1. 사전 점검 체크리스트 (§1) 전부 통과
2. HikariCP 튜닝 적용 (§2) + PC1 재기동
3. **S1 재실행** → 전→후 표 완성 ← 최우선. 여기까지만 해도 포트폴리오 핵심 서사 확보
4. S2 한계 탐색 (시나리오 간 드레인 확인 필수)
5. S3 멱등 / S4 비정상 혼입
6. (여력 시) 다상품 분산 비교, S5 soak
7. 결과를 `docs/load-test-results.md`로 문서화 — 모든 표에 각주:
   측정 환경(3-PC 사양·토폴로지, k6 위치=PC3), 반복 수, loadtest 스텁 사용 여부, 관리 API 통계 지연.
   1차 실패(0.33%)를 숨기지 말고 "부하 테스트로 용량 결함을 발견→진단→해소"의 기점으로 서술할 것.

## 참고 파일

- 하니스: `loadtest/seed_orders.py`, `loadtest/confirm_load.js`, `loadtest/verify_pipeline.py`, `loadtest/README.md`
- 전체 플랜: `docs/distributed-mq-plan.md` / 장애 실험: `docs/failure-experiments.md`
- Windows 주의: python은 `/c/Python313/python` (python3는 스토어 스텁), 시드 스크립트는 `PYTHONIOENCODING=utf-8`
