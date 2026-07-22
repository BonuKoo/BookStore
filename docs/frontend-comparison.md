# React SPA vs 바닐라 JS 프론트엔드 성능 비교

동일한 core-spa REST API를 소비하는 두 프론트엔드 구현을 같은 조건에서 비교했다.
- **React**: React 18 + TypeScript + Vite + TanStack Query + react-router-dom + axios (SPA, 프로덕션 빌드)
- **바닐라**: 프레임워크 없는 HTML/CSS/ES모듈 (MPA, 페이지별 정적 파일)
- 두 구현은 의도적으로 동일한 CSS 클래스 체계(14islands 디자인 시스템)를 공유.

측정일: 2026-07-20 / 하드웨어: Intel i7-6700(4C/8T), Windows 10 / Chrome headless + Lighthouse 12.8.2

---

## 1. 번들 크기 (정적 분석 — 서버·네트워크 무관)

| | JS (raw / gzip) | CSS (raw / gzip) | 합계 (raw / gzip) |
|---|---|---|---|
| **React** (SPA 첫 로드 전체) | 276.9 / 90.5 KB | 8.3 / 2.3 KB | 285.6 / **93.0 KB** |
| **바닐라** (공통 자산) | 8.8 / 4.0 KB | 11.4 / 3.5 KB | 20.2 / **7.6 KB** |
| **비율** | ~23× | — | **약 12×** |

React 번들의 90.5 KB(gzip)는 React+ReactDOM+Router+TanStack Query+axios 런타임이다.
바닐라는 앱 코드가 8.8 KB(js 3파일)에 불과하고, 오히려 CSS(11.4 KB)가 더 크다.

## 2. 렌더링 성능 (Lighthouse, 데스크톱·스로틀 없음, 5회 중앙값)

| 지표 | react-login | vanilla-login | react-books | vanilla-books |
|---|---:|---:|---:|---:|
| Perf 점수 | 100 | 100 | 90 | 90 |
| FCP (ms) | 327 | **309** | 329 | **320** |
| LCP (ms) | 327 | **309** | 516 | **439** |
| Speed Index (ms) | 334 | 317 | 481 | 430 |
| TBT (ms) | 0 | 0 | 0 | 0 |
| CLS | 0.001 | 0.001 | 0.213 | 0.203 |
| 전송량 (KB, 폰트·이미지 포함) | 303 | 234 | 796 | 728 |

- **로그인(정적, API 없음)**: 바닐라가 FCP 18ms 빠름. 둘 다 Perf 100점 — 체감 차이는 사실상 없음.
- **도서목록(API+이미지)**: 바닐라가 LCP 77ms 빠름. CLS는 양쪽 다 0.2대(도서 표지 이미지 로딩 시 레이아웃 이동 — 공통 개선 포인트, `aspect-ratio` 지정으로 완화 가능).
- **TBT=0 (양쪽 모두)**: 이 하드웨어+번들 규모에서는 React 275 KB 파싱조차 50ms 미만이라 메인스레드 롱태스크가 안 잡힌다 → **JS 실행이 사용자 체감 병목이 아니다.**

## 3. 결론 — 정직한 해석

**이 규모(소형 앱)·이 환경(고속 데스크톱)에서 프레임워크 오버헤드는 사용자 체감으로 거의 드러나지 않는다.**
렌더 지표 차이는 로그인 18ms / 도서목록 77ms로 미미하고, 두 구현 모두 Perf 90~100.

차이가 명확한 유일한 축은 **번들 크기(네트워크 비용)** — React가 gzip 12배(85 KB 더 큼).
이 비용은 고속 환경에선 렌더로 전이되지 않지만, **저사양 기기·느린 네트워크에선 이 85 KB가
다운로드+파싱 지연으로 전이**된다(→ §4의 측정 함정이 이를 우연히 드러냈다).

즉 "React가 느리다"가 아니라: **소형 앱에서 React의 비용은 성능이 아니라 전송량으로 지불되며,
그 전송량이 체감 성능이 되는지는 사용자의 네트워크·기기에 달렸다.** 바닐라는 그 비용이
근본적으로 없지만, 상태관리·라우팅·데이터 페칭을 직접 구현하는 개발 비용과 맞바꾼 것이다.

## 4. 측정 방법론 & 함정 (정직성 노트)

- **1차 측정(모바일 slow-4G 스로틀)은 폐기했다.** FCP/LCP/TTI가 React·바닐라 모두 ~4초로
  수렴하고 프레임워크 차이가 사라졌는데, 원인은 **두 구현이 공유하는 Google Fonts CDN `@import`**
  였다. 느린 네트워크 스로틀에서 외부 폰트 요청이 렌더를 블로킹해 FCP를 지배했고,
  프레임워크 차이(수십 ms)가 폰트 왕복(수천 ms)에 완전히 묻혔다.
  → **역설적 교훈: 이 앱에서 FCP를 좌우하는 건 프레임워크 선택이 아니라 폰트 로딩 전략이다**
  (폰트를 로컬 번들링하거나 `font-display:swap` + `preconnect`로 개선하는 게 프레임워크 교체보다 효과 큼).
- 그래서 §2는 **데스크톱 폼팩터 + 스로틀 없음**(`--preset=desktop --throttling-method=provided`)으로
  재측정 — 로컬 실제 조건에서 프레임워크 부팅 비용을 드러냈다.
- React는 **프로덕션 빌드**(`vite build`)를 preview 서버로 서빙했다(dev 서버는 HMR·비압축이라 불공정).
  두 서버 모두 gzip 미적용 정적 서빙이라 전송량 조건 동일.
- 각 지표 5회 반복 중앙값. 도서목록은 네이버 이미지·API 응답이 변수로 섞이므로,
  프레임워크 순수 비교는 로그인 페이지가 더 신뢰도 높다.

## 재현

```bash
# 번들 크기
cd core-spa/frontend && npm run build   # dist/ 크기 측정
# 렌더 성능 (React preview 5173, vanilla 3000 기동 후)
npx lighthouse http://localhost:5173/login --preset=desktop --throttling-method=provided \
  --only-categories=performance --output=json --output-path=out.json
```
