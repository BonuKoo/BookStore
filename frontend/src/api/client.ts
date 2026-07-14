import axios from 'axios';

export const API_BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const TOKEN_KEY = 'accessToken';
export const USERNAME_KEY = 'username';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;
    const body = err.response?.data;
    // 백엔드는 미인증 요청에 Http403ForbiddenEntryPoint로 "빈 본문 403"을 반환한다.
    // 본문이 있는 403은 비즈니스 응답(예: 중복 주문)이므로 구분해서 처리한다.
    const isSecurityReject =
      status === 401 ||
      (status === 403 && !(body && (body.error !== undefined || body.data !== undefined)));
    if (isSecurityReject && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USERNAME_KEY);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  },
);

export default client;
