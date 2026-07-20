# core-frontend-vanilla

core-spa 백엔드용 순수 HTML/CSS/JS 프론트. `frontend/`(React 18 + Vite + TS)와 **동일한 핵심 기능**을
프레임워크·빌드 도구 없이 구현해, 아키텍처 선택(SPA+번들러 vs MPA+바닐라)의 실제 무게 차이를 비교하기 위한 프로젝트다.

## 실행

빌드 스텝이 없으므로 정적 파일 서버만 있으면 된다. **반드시 포트 3000**으로 띄운다 —
백엔드 `WebSecurityConfig`의 CORS 허용 오리진이 `localhost:3000`/`localhost:5173`로 하드코딩되어 있다
(5173은 React dev 서버가 이미 쓰므로 이쪽은 3000).

```bash
python -m http.server 3000
# 또는: npx serve -l 3000
```

백엔드(core-spa, :8080)와 MySQL이 떠 있어야 API가 동작한다.

## 구조

```
frontend-vanilla/
 ├─ js/
 │   ├─ api.js        fetch 래퍼(Bearer 토큰, 403-빈본문 세션만료 판별) — React의 api/client.ts에 대응
 │   ├─ domain.js      도메인 API 함수 전부(auth/cart/naver/checkout/toss) — React의 api/*.ts 전부를 병합
 │   └─ layout.js      공용 헤더 렌더링 + requireAuth 가드 + 포맷 헬퍼
 ├─ css/style.css      단일 스타일시트 (React index.css와 유사한 톤)
 ├─ books.html, book-detail.html, cart.html, checkout.html,
 │  login.html, signup.html, payment-success.html, payment-fail.html
 ├─ sociallogin/index.html   OAuth 콜백 전용 (아래 "라우팅 차이" 참고)
 └─ index.html          books.html로 즉시 리다이렉트
```

## React 버전과의 구조적 차이 (의도적)

- **SPA 클라이언트 라우팅이 없다.** 페이지 전환은 진짜 `<a href>`/`location.href`이고, 매번 문서가
  새로 로드된다. `react-router-dom`이 하던 일(중첩 라우트, `<Navigate>`, `useParams`)을 대신할 게
  애초에 없다 — 파일 자체가 라우트다.
- **`location.state`가 없다.** React의 `CartPage → navigate('/checkout', { state: { order } })`처럼
  메모리로 다음 페이지에 데이터를 못 넘긴다. 대신 `sessionStorage`로 주문 객체를 넘긴다
  (`cart.html`이 쓰고 `checkout.html`이 읽고 지운다).
- **소셜 로그인 콜백 경로 문제.** 백엔드(`OAuthSuccessHandler`)는 `{origin}/sociallogin?token=...`로
  리다이렉트한다 — 확장자 없는 경로다. 정적 파일 서버는 `/sociallogin`을 그대로 찾지 못하므로,
  `sociallogin.html`이 아니라 **`sociallogin/index.html`**(디렉터리 라우트)로 둬서 대부분의 정적
  서버가 지원하는 "디렉터리 요청 → index.html" 규칙에 태웠다.
- **StrictMode 이중 마운트 문제 자체가 없다.** React판 `PaymentSuccessPage`는 승인 요청이 2번 나가지
  않도록 `useRunOnce` 가드가 필요했다. 바닐라는 페이지당 스크립트가 정확히 한 번만 실행되므로 이
  문제가 애초에 존재하지 않는다 — 프레임워크가 만든 문제를 프레임워크 없는 쪽이 풀 필요가 없었던 사례.
- **Toss SDK는 CDN 스크립트 태그**(`https://js.tosspayments.com/v2/standard`, 전역 `window.TossPayments`)로
  로드한다. npm 패키지(`@tosspayments/tosspayments-sdk`)의 소스를 확인해 CDN URL과 전역 함수명,
  `ANONYMOUS = '@@ANONYMOUS'` 상수를 그대로 가져와 재현했다.

## 백엔드 계약 (React README와 동일 — 프론트 구현체가 달라도 계약은 같다)

- 미인증 응답이 403(빈 본문) — 본문 있는 403(중복 주문 등)과 구분해서 처리.
- 수량 변경은 덮어쓰기 시맨틱.
- 장바구니 담기는 상세 페이지를 거쳐야 성공(`saveItem` 부수효과).
- 주문 생성은 멱등 (200 SUCCESS / 409 ALREADY_EXISTS 모두 결제 진행 가능).

## 성능 비교 메모

| | React (frontend/) | Vanilla (frontend-vanilla/) |
|---|---|---|
| 빌드 | `tsc -b && vite build` 필요 | 없음 — 파일을 그대로 서빙 |
| 프로덕션 JS | 275.2 kB (gzip 90.7 kB), 단일 번들 | 프레임워크 런타임 0 kB. 페이지당 실제 로드하는 모듈만(예: books.html ≈ api.js+domain.js+layout.js, 수 kB대) |
| 페이지 전환 | 클라이언트 라우팅, 문서 재로드 없음 | 매번 풀 페이지 로드 (브라우저 캐시로 정적 자산은 재사용) |
| 상태 공유 | Context, react-query 캐시 | 없음 — 매 페이지가 API를 다시 호출하거나 sessionStorage 사용 |
| 개발 경험 | HMR, 타입체크가 리팩터링 안전망 | 타입체크 없음, 리네이밍 시 문자열 검색에 의존 |

React 쪽이 초기 진입 시 275 kB를 한 번에 내려받는 대신 이후 페이지 전환이 가볍고, 바닐라는 매 요청이
가볍지만 페이지 이동마다 풀 리로드 비용을 문다 — 트래픽 패턴(체류 페이지 수)에 따라 유불리가 갈리는
전형적인 SPA vs MPA 트레이드오프를 이 프로젝트 규모에서 그대로 재현한다.
