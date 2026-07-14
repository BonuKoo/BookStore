# 🏗️ core 북스토어 — 아키텍처

> 코드 블록의 다이어그램은 노션에서 언어를 `Mermaid`로 지정하면 그림으로 렌더링됩니다.

---

## 1. 시스템 구성도

```mermaid
flowchart LR
    subgraph Client["클라이언트"]
        R["React SPA<br/>(Vite, :5173)"]
    end

    subgraph Server["Spring Boot :8080"]
        SEC["Security Filter Chain<br/>JWT 검증 · OAuth2 로그인"]
        API["REST Controllers"]
        SVC["Domain Services<br/>(Cart · Checkout · Payment)"]
        REPO["JPA + QueryDSL<br/>Repositories"]
    end

    subgraph External["외부 서비스"]
        OAUTH["Google · Naver<br/>Kakao · GitHub"]
        NAVER["네이버 도서 API"]
        TOSS["토스페이먼츠"]
    end

    DB[("MySQL 8<br/>core2_spa")]
    MON["Prometheus · Grafana<br/>p6spy · k6"]

    R -->|"Bearer JWT / JSON"| SEC --> API --> SVC --> REPO --> DB
    SEC <-->|"OAuth2 인증"| OAUTH
    SVC -->|"OpenFeign"| NAVER
    SVC -->|"OpenFeign<br/>Idempotency-Key"| TOSS
    Server -.->|"메트릭"| MON
```

**포인트**
- 백엔드는 완전한 STATELESS JSON API — 프론트(React SPA)와 CORS 기반으로 분리 배포
- 외부 호출은 모두 OpenFeign으로 통일 (인증 헤더는 RequestInterceptor로 주입)
- 관측 스택(Prometheus/Grafana/k6)은 docker-compose로 일괄 기동

---

## 2. 인증 플로우

```mermaid
sequenceDiagram
    participant U as React SPA
    participant S as Spring Security
    participant P as OAuth Provider
    participant DB as MySQL

    rect rgb(240, 246, 255)
    note over U,DB: 자체 로그인
    U->>S: POST /auth/signin (id/pw)
    S->>DB: 사용자 조회 + BCrypt 검증
    S-->>U: JWT (HS512, 1일)
    end

    rect rgb(240, 255, 244)
    note over U,DB: 소셜 로그인
    U->>S: /oauth2/authorization/{provider}?redirect_url=...
    note over S: redirect_url을 HttpOnly 쿠키로 저장 (커스텀 필터)
    S->>P: 인가 요청 → 사용자 정보 조회
    note over S: provider별 응답을 OAuthAttributes로 정규화<br/>(GitHub 이메일 비공개 시 추가 API 호출)
    S->>DB: 계정 조회/신규 저장
    S-->>U: {redirect_url}/sociallogin?token=JWT
    end

    U->>S: 이후 모든 요청 Authorization: Bearer
    note over S: JwtAuthenticationFilter가<br/>subject(userId)를 SecurityContext에 등록
```

---

## 3. 주문·결제 플로우 (멱등성 + 상태머신)

```mermaid
sequenceDiagram
    participant U as React SPA
    participant BE as Spring Boot
    participant DB as MySQL
    participant T as 토스페이먼츠

    U->>BE: POST /api/checkout (cartItemIds)
    note over BE: 멱등성 키 생성<br/>UUID(userId + 정렬된 cartItemIds)
    BE->>DB: INSERT payment_event (order_id UNIQUE)
    alt 신규 주문
        BE-->>U: 200 SUCCESS (orderId, amount)
    else 중복 주문 (Duplicate Key)
        BE->>DB: 기존 주문 조회 (REQUIRES_NEW + 프로젝션)
        BE-->>U: 409 ALREADY_EXISTS (동일 orderId)
        note over U: 오류가 아닌 "기존 주문 이어서 결제"로 처리
    end

    U->>T: 결제위젯 requestPayment(orderId, amount)
    T-->>U: successUrl?paymentKey&orderId&amount

    U->>BE: POST /v1/toss/confirm
    BE->>DB: 상태 EXECUTING 전이 + 이력 기록<br/>(이미 SUCCESS/FAILURE면 즉시 차단)
    BE->>DB: 주문 총액 ↔ 요청 금액 검증
    BE->>T: POST /v1/payments/confirm (Idempotency-Key)
    T-->>BE: 승인 결과 / 에러코드
    note over BE: 토스 에러 50여 종을<br/>성공·실패·UNKNOWN·재시도가능으로 분류
    BE->>DB: SUCCESS / FAILURE / UNKNOWN + 이력
    BE-->>U: 결제 결과
```

## 4. 결제 상태머신

```mermaid
stateDiagram-v2
    [*] --> NOT_STARTED: 주문 생성
    NOT_STARTED --> EXECUTING: confirm 시작<br/>(이력: PAYMENT_CONFIRMATION_START)
    EXECUTING --> SUCCESS: PSP 승인
    EXECUTING --> FAILURE: PSP 거절 (재시도 불가 에러)
    EXECUTING --> UNKNOWN: 타임아웃·네트워크 단절 등
    UNKNOWN --> EXECUTING: 복구 배치 재시도<br/>(failed_count < threshold)
    SUCCESS --> [*]
    FAILURE --> [*]
    note right of EXECUTING
        모든 전이는 payment_order_history에
        (이전 상태, 새 상태, 사유) 감사 기록
    end note
```

---

## 5. 결제 도메인 — 헥사고날(포트-어댑터) 구조

```mermaid
flowchart TB
    subgraph In["인바운드"]
        C1["RestCheckoutController"]
        C2["TossPaymentController"]
    end
    subgraph Core["도메인 (UseCase · Port)"]
        UC1["CheckoutUseCase"]
        UC2["PaymentConfirmUseCase"]
        P1["SavePaymentPort · LoadPaymentPort<br/>PaymentStatusUpdatePort · PaymentValidationPort<br/>LoadPendingPaymentPort · PaymentExecutorPort"]
    end
    subgraph Out["아웃바운드 어댑터"]
        A1["PaymentPersistentAdapter<br/>(JPA + QueryDSL)"]
        A2["TossPaymentExecutor<br/>(OpenFeign)"]
    end
    C1 --> UC1
    C2 --> UC2
    UC1 & UC2 --> P1
    P1 --> A1 --> DB[("MySQL")]
    P1 --> A2 --> TOSS["토스페이먼츠"]
```

**의도**: 결제 핵심 로직이 "DB가 JPA인지, PSP가 토스인지"를 모르게 격리 → PSP 교체·테스트 더블 주입이 포트 단위로 가능

## 6. ERD (핵심 테이블)

```mermaid
erDiagram
    ACCOUNT_ENTITY ||--o| CART : "1:1"
    CART ||--o{ CART_ITEM : "1:N"
    ITEM ||--o{ CART_ITEM : "1:N"
    ACCOUNT_ENTITY ||--o{ PAYMENT_EVENT : "구매자"
    PAYMENT_EVENT ||--o{ PAYMENT_ORDERS : "1:N"
    PAYMENT_ORDERS ||--o{ PAYMENT_ORDER_HISTORY : "상태 이력"

    ACCOUNT_ENTITY { bigint id PK "username UNIQUE" }
    ITEM { varchar isbn PK "가격·재고·@Version(낙관적 락)" }
    CART_ITEM { bigint id PK "cart_id FK, isbn FK, amount" }
    PAYMENT_EVENT { bigint id PK "order_id UNIQUE(멱등성 키)" }
    PAYMENT_ORDERS { bigint id PK "status, failed_count, threshold" }
    PAYMENT_ORDER_HISTORY { bigint id PK "prev→new 상태, reason" }
```

---

## 7. 패키지 구조

```
com.bookService.core
├─ common/          공통 응답 DTO · 전역 예외 핸들러 · 에러코드 · 멱등성 키 유틸
├─ config/          Security · JWT · Async 스레드풀 · Batch · p6spy
├─ security/        JWT 필터 · OAuth2 서비스 · 성공 핸들러 · redirect 쿠키 필터
├─ domain/
│   ├─ login/       계정 (회원가입 · 로그인)
│   ├─ item/        상품 (재고 · @Version)
│   ├─ cart/ cartitem/   장바구니 (QueryDSL 프로젝션 조회)
│   ├─ checkout/    주문 생성 (멱등성)
│   └─ payment/     결제 — entity · port · adapter · usecase · persistent(3계층 리포지토리)
├─ infra/
│   ├─ naver/       도서 검색 Feign 클라이언트
│   └─ toss/        결제 승인 Feign 클라이언트 · Executor · 에러 매핑
└─ facade/          재고 락 재시도 Facade (지수 백오프 · @Async)

frontend/ (React SPA)
├─ api/       axios 인스턴스(Bearer 인터셉터) + 도메인별 API 모듈
├─ auth/      AuthContext · ProtectedRoute
├─ pages/     로그인 · 도서 · 장바구니 · 결제 · 콜백 (9개)
└─ types/     백엔드 DTO 대응 TypeScript 타입
```
