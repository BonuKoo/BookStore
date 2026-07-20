# core-frontend

core-spa 백엔드용 React SPA (Vite + React 18 + TypeScript).
docs/FRONTEND_STRATEGY.md의 "옵션 B — React" 로드맵 Phase 1~2 구현.

## 실행

```bash
npm install
npm run dev      # http://localhost:5173 (백엔드 CORS 허용 포트)
```

백엔드(core-spa, :8080)와 MySQL이 떠 있어야 API가 동작한다.

## 구조

```
src/
 ├─ api/          axios 인스턴스(Bearer 인터셉터) + 도메인별 API 함수
 ├─ auth/         AuthContext(토큰 상태·State/Dispatch 겸용), ProtectedRoute
 ├─ hooks/        useCart(장바구니 쿼리+뮤테이션), useRunOnce(마운트 1회 부수효과),
 │                usePageTitle(document.title 동기화)
 ├─ util/         format(formatWon·extractIsbn13), constants(PAGE_SIZE·SOCIAL_PROVIDERS)
 ├─ components/   Layout(헤더/네비), Button(variant), Message(피드백 문구),
 │                BookCard, SocialButtons, QuantityStepper
 ├─ pages/        Login, Signup, SocialLoginCallback,
 │                BookList, BookDetail, Cart, Checkout, PaymentSuccess/Fail
 └─ types/        백엔드 DTO 대응 타입 (ResponseDTO<T> 등)
```

REACT_GUIDELINE.md(onebite-react)의 4계층 분리를 따른다: **pages**(데이터 조립) /
**components**(props-only 표현+재사용) / **hooks**(use* 공유 로직) / **util**(React 무관 순수 함수).

### 재배치 노트 (가이드라인 적용)

- **hooks/·util/ 신설**: 페이지에 흩어져 있던 순수 계산(원화 표기, isbn13 추출)은 `util/`로,
  반복되던 로직(장바구니 쿼리/뮤테이션, StrictMode 1회 실행 가드, 타이틀 동기화)은 `hooks/`로 뺐다.
- **props-only 컴포넌트 분리**: `Button`(variant prop), `Message`(error/muted/success/notice),
  `BookCard`, `SocialButtons`를 추출해 페이지는 조립만 하도록 했다.
- **CSS는 단일 `index.css` 디자인 시스템 유지**(컴포넌트별 CSS 분리 대신). 컴포넌트는 `.btn`·`.error`
  같은 공용 클래스를 className으로 참조한다 — 가이드라인의 "컴포넌트별 CSS 동반"에서 의도적으로 벗어난 부분.
- 언어는 기존대로 **TypeScript 유지**(가이드라인 예시는 JSX지만 원칙은 언어 무관, TS를 JS로 낮추지 않음).

## 백엔드 계약 관련 주의사항 (코드에 주석으로도 표기)

- **미인증 응답이 403(빈 본문)** — `client.ts` 인터셉터가 "본문 없는 403"만 세션 만료로 간주해 로그인으로 보낸다. 본문 있는 403은 비즈니스 응답(중복 주문)이라 통과시킨다.
- **수량 변경은 덮어쓰기 시맨틱** — `/api/cartItem/increaseItem`이 amount를 가산이 아니라 대입하므로, 프론트에서 최종 수량을 계산해 보낸다 (`setCartItemAmount`).
- **장바구니 담기는 상세 페이지를 거쳐야 성공** — `GET /api/naver/bookDetail`이 Item을 DB에 저장하는 부수효과를 갖고 있어, 목록에서 바로 담기가 불가능하다.
- **주문 생성은 멱등** — 같은 장바구니 구성이면 200(SUCCESS) 또는 409(ALREADY_EXISTS)로 같은 orderId가 돌아오며, 둘 다 결제 진행 가능으로 처리한다 (`createCheckout`).
- **소셜 로그인** — `{API}/oauth2/authorization/{provider}?redirect_url={origin}` 진입 → 백엔드가 `/sociallogin?token=...`으로 복귀. 콜백 페이지가 토큰 저장 즉시 `history.replaceState`로 URL에서 토큰을 지운다.

## 환경 변수 (.env.development)

| 변수 | 기본값 | 설명 |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | core-spa 백엔드 |
| `VITE_TOSS_CLIENT_KEY` | test 키 | 토스 결제위젯 클라이언트 키 (백엔드 secretKey와 쌍) |

## 결제 테스트 흐름

1. 로그인 → 도서 검색 → 상세 → 장바구니 담기
2. 장바구니에서 "주문하기" → `/api/checkout` (멱등 주문 생성)
3. 결제 페이지에서 토스 위젯 → 테스트 카드로 결제
4. `/payment/success` → `/v1/toss/confirm` 승인 → 완료 화면
