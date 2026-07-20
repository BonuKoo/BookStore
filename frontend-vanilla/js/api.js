// 공용 fetch 래퍼. React 버전(api/client.ts)의 axios 인터셉터와 동일한 계약을 순수 fetch로 재현한다.
export const API_BASE_URL = 'http://localhost:8080';
export const TOKEN_KEY = 'accessToken';
export const USERNAME_KEY = 'username';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setSession(token, username) {
  localStorage.setItem(TOKEN_KEY, token);
  if (username) localStorage.setItem(USERNAME_KEY, username);
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USERNAME_KEY);
}

export function isAuthenticated() {
  return getToken() !== null;
}

/**
 * fetch 래퍼. Bearer 토큰을 자동 첨부하고, 백엔드의 "미인증 응답 = 빈 본문 403"만
 * 세션 만료로 간주해 로그인으로 보낸다. 본문이 있는 403(중복 주문 등 비즈니스 응답)은 통과시킨다.
 * (React client.ts 인터셉터와 동일 규칙 — REACT_GUIDELINE의 "외부 데이터는 신뢰하지 않는다"에 따라
 *  JSON 파싱 실패를 항상 방어한다.)
 */
export async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

  let body = null;
  const text = await res.text();
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
  }

  const isSecurityReject =
    res.status === 401 ||
    (res.status === 403 && !(body && (body.error !== undefined || body.data !== undefined)));

  if (isSecurityReject && token) {
    clearSession();
    if (!location.pathname.endsWith('login.html')) {
      location.href = 'login.html';
    }
  }

  return { ok: res.ok, status: res.status, body };
}

export function qs(params) {
  const usp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null) usp.set(k, v);
  }
  const s = usp.toString();
  return s ? `?${s}` : '';
}
