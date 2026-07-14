# core-spa 코드베이스 현황 & Phase 2 가이드라인

> 기준: master 브랜치 / 2026-07-14 점검. distributed-mq 통합 작업의 사전 점검 결과.
> 함께 볼 문서: [distributed-mq-plan.md](distributed-mq-plan.md)

---

## 1. 프로젝트 개요

- **BookStore** — Toss Payments 연계 전자상거래 백엔드
- **스택**: Spring Boot 3.4.8 / **Java 21** / Gradle
- **DB**: MySQL(`core2_spa`, `ddl-auto=none` — 스키마 수동 관리), MongoDB(장바구니 대체 구현), p6spy(SQL 로깅)
- **인증**: Spring Security + OAuth2(Google/Naver/Kakao/Github) + JWT
- **외부 연동**: Toss Payments(WebClient), Naver 책 검색 API(WebClient/Feign)
- **관측**: Actuator + Micrometer + Prometheus
- `compileJava` 통과, 워킹트리 clean.

## 2. 아키텍처 현황

도메인별 패키지(`domain/*`) + 인프라(`infra/*`) + 공통(`common/*`) + 보안(`security/*`).

⚠️ **결제 도메인은 두 스타일이 공존한다** (기술부채, 6절):
- **헥사고날**: `payment/port/*` 포트 + `PaymentPersistentAdapter` 어댑터. `PaymentEvent` 생성/조회 계열.
- **직접 호출**: 결제 확정 경로(`PaymentConfirmService`)는 포트를 안 거치고 `PaymentStatusUpdateRepository` 구체 클래스를 직접 주입해 사용.

또한 `@Repository`(`PaymentStatusUpdateRepository`)가 상태 전이·이력 적재·검증 등 **서비스 계층 책임**을 수행 중 → 원래 서비스로 올라가야 할 로직.

## 3. 구현된 기능 & 엔드포인트

| 도메인 | 엔드포인트(prefix) | 상태 |
|---|---|---|
| 인증 | `/auth/signup`, `/auth/signin` + OAuth2 로그인 | 구현 |
| 결제 확정(Toss) | `POST /v1/toss/confirm` | 구현(핵심) |
| 체크아웃(주문 생성) | `POST /api/checkout` | 구현(핵심) |
| 장바구니(MySQL) | `/api/cart`, `/api/cartItem/*` | 구현 |
| 장바구니(Mongo) | `/mongo/cart/*` | 대체 구현(실험) |
| 책 검색(Naver) | `/api/naver/search-books`, `/bookDetail` | 구현 |
| 상품 | `ItemService` | 구현 |
| Todo | `/todo` | 데모/학습용 |
| 배치 | `config/batch/*` (job.enabled=false) | 수동 실행용 |

## 4. 결제 핵심 흐름

**주문 생성** — `POST /api/checkout` → `CheckoutService.checkout()`
- 멱등키(orderId) = `IdempotencyCreator.create(request, userId)`
- `PaymentEvent`(+ N개 `PaymentOrder`, status `NOT_STARTED`) 저장
- 중복 orderId → `DataIntegrityViolationException` → `CheckoutResult.alreadyExists`

**결제 확정** — `POST /v1/toss/confirm` → `PaymentConfirmService.confirm()` (`@Transactional`)
1. `updatePaymentStatusToExecuting` — 이전 상태 검증(SUCCESS/FAILURE면 `PaymentAlreadyProcessedException`), 이력 적재, `EXECUTING` 전이, paymentKey 세팅
2. `paymentOrderRepository.isValid` — 금액 합계 검증
3. `tossPaymentExecutor.execute` — Toss 결제 승인 API 호출
4. `updatePaymentStatus(SUCCESS|FAILURE|UNKNOWN)` — 이력 + 상태 전이 (+ UNKNOWN 시 failed_count 증가)
5. `PSPConfirmationException` 시 실패 상태로 전이 후 결과 반환

**결제 상태머신**: `NOT_STARTED → EXECUTING → { SUCCESS | FAILURE | UNKNOWN }`

## 5. 최근 수정 (2026-07-14, Phase 2 착수 전 정리)

1. **`incrementFailedCount` 중복 증가 버그 수정** — 루프 제거, `incrementFailedCountByOrderId` 1회 호출(다중 항목 주문에서 failed_count N배 부풀림 해소)
2. **`@Transactional` 정리** — private 메서드의 무효 어노테이션 제거 + `jakarta.transaction.Transactional` → `org.springframework...Transactional` 통일(`PaymentConfirmService`, `JpaPaymentEventRepository`)
3. **컨트롤러 예외 위임** — `TossPaymentController`의 자체 `try/catch(Exception)` 제거 → `GlobalExceptionHandler`(@RestControllerAdvice)로 위임

## 6. 알려진 이슈 / 기술부채

- **예외 처리 미완**: `PaymentAlreadyProcessedException`이 `BusinessLogicException` 계열이 아니면 어드바이스 최종 `Exception` 핸들러에 걸려 여전히 500. 정확한 409/멱등 응답은 **Phase 4**에서 예외 계층 정비 시 마무리.
- **헥사고날 이중 구조** + `@Repository`의 과도한 책임(2절).
- **네이밍**: `CheckoutCommandForDev`가 운영 경로에서 실제 사용됨 → `CheckoutCommand`로 정리 필요(이번엔 보류).
- 소스 전반 TODO/"나중에" 주석 다수, 주석 처리된 outbox/mongo 코드 잔존.
- `CheckoutService`의 `exist` 변수(dead), `complete()` no-op 어댑터 등.

## 7. Phase 2 (프로듀서) 가이드라인

**발행 지점**: `PaymentStatusUpdateRepository.updatePaymentStatusToSuccess()`
— 현재 주석 블록(`// 메시지 이벤트 TODO`) 위치가 정확한 seam.

**이미 준비된 seam (구현체만 없음)**:
- `PaymentEventMessage`(messageType/payload/metadata) — 메시지 DTO
- `DispatchEventMessagePort` — 발행 포트(**AMQP 어댑터를 여기 붙임**)
- `LoadPendingPaymentEventMessagePort` — Outbox 조회용(Phase 6)
- `PaymentEventMessageType` enum

**설계 원칙 — 트랜잭션 커밋 후 발행 (플랜 요구)**:
현재 발행 지점은 `@Transactional` **내부**다. 여기서 바로 AMQP 전송하면 "롤백됐는데 메시지는 나감" 문제 발생. 따라서:
1. 발행 지점에서는 `ApplicationEventPublisher`로 **도메인 이벤트만** 발행
2. 실제 AMQP 전송은 `@TransactionalEventListener(phase = AFTER_COMMIT)` 리스너에서 `DispatchEventMessagePort` 구현체를 통해 수행
3. routing key: `payment.confirmed`(성공) / `payment.failed`(실패)
4. Phase 4에서 publisher confirms, Phase 6에서 Outbox로 완결

**의존성 활성화**: `build.gradle`의 `spring-boot-starter-amqp` / slack-api-client가 현재 주석 처리됨 → 주석 해제 필요.

**브로커**: PC2 `192.168.0.6:5672`(관리 UI `:15672`), 계정 `coreapp`.
