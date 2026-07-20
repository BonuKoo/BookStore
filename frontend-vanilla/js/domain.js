// 도메인별 API 함수. React 버전의 api/auth.ts, api/cart.ts, api/naver.ts, api/checkout.ts, api/toss.ts를
// 한 파일로 합친 것 — 바닐라 프로젝트는 파일 수를 늘릴수록 <script type="module"> 프리로드 그래프가
// 복잡해지므로, 여러 페이지가 공유하는 것들을 의도적으로 묶었다.
import { apiFetch, qs, API_BASE_URL } from './api.js';

export const SOCIAL_PROVIDERS = [
  { id: 'google', label: 'Google' },
  { id: 'naver', label: '네이버' },
  { id: 'kakao', label: '카카오' },
  { id: 'github', label: 'GitHub' },
];

export function socialLoginUrl(provider) {
  const redirect = encodeURIComponent(location.origin);
  return `${API_BASE_URL}/oauth2/authorization/${provider}?redirect_url=${redirect}`;
}

export async function signin(username, password) {
  const { ok, body } = await apiFetch('/auth/signin', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (!ok) throw new Error(body?.error || '로그인에 실패했습니다.');
  return body; // Account { id, username, token }
}

export async function signup(username, password) {
  const { ok, body } = await apiFetch('/auth/signup', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (!ok) throw new Error(body?.error || '회원가입에 실패했습니다.');
  return body;
}

export async function searchBooks(query, display = 10, start = 1) {
  const { ok, body } = await apiFetch(`/api/naver/search-books${qs({ query, display, start })}`);
  if (!ok) throw new Error('도서 검색에 실패했습니다.');
  return body; // NaverBook[]
}

/** 상세 조회는 백엔드가 Item을 DB에 저장하는 부수효과를 가진다 — 담기는 이 호출 이후에만 성공한다. */
export async function fetchBookDetail(isbn) {
  const { ok, body } = await apiFetch(`/api/naver/bookDetail${qs({ isbn })}`);
  if (!ok) throw new Error('도서 정보를 불러오지 못했습니다.');
  return body; // NaverBookDetail
}

export async function fetchCart() {
  const { ok, body } = await apiFetch('/api/cart/list');
  if (!ok || body?.error) throw new Error(body?.error || '장바구니를 불러오지 못했습니다.');
  if (!body?.data?.length) throw new Error('장바구니 응답이 비어 있습니다.');
  return body.data[0]; // CartListResponse
}

export async function addToCart(isbn, amount) {
  const { ok, body } = await apiFetch(`/api/cart/add${qs({ isbn, amount })}`, { method: 'POST' });
  if (!ok || body?.error) throw new Error(body?.error || '담기에 실패했습니다.');
  return body.data[0];
}

/** 덮어쓰기 시맨틱 — 호출부가 최종 수량을 계산해서 넘긴다. */
export async function setCartItemAmount(itemIsbn, amount) {
  const { ok, body } = await apiFetch('/api/cartItem/increaseItem', {
    method: 'POST',
    body: JSON.stringify({ itemIsbn, amount }),
  });
  if (!ok) throw new Error(body?.error || '수량 변경에 실패했습니다.');
}

export async function removeCartItem(itemIsbn) {
  const { ok, body } = await apiFetch(`/api/cartItem/removeItem${qs({ itemIsbn })}`, { method: 'POST' });
  if (!ok) throw new Error(body?.error || '삭제에 실패했습니다.');
}

/** 멱등 — 200(SUCCESS) 또는 409(ALREADY_EXISTS) 둘 다 결제 진행 가능한 정상 응답이다. */
export async function createCheckout(cartItemIds) {
  const { ok, status, body } = await apiFetch('/api/checkout', {
    method: 'POST',
    body: JSON.stringify({ cartItemIds }),
  });
  if (ok || status === 409) {
    if (body?.data?.length) return body.data[0];
  }
  throw new Error(body?.error || '주문 생성에 실패했습니다.');
}

export async function confirmPayment({ paymentKey, orderId, amount }) {
  const { body } = await apiFetch('/v1/toss/confirm', {
    method: 'POST',
    body: JSON.stringify({ paymentKey, orderId, amount }),
  });
  if (!body?.data) throw new Error(body?.message || '결제 승인 응답이 비어 있습니다.');
  return body.data; // PaymentConfirmationResult
}
