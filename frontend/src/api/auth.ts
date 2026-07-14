import axios from 'axios';
import client, { API_BASE_URL } from './client';
import type { Account } from '../types/api';

/** 실패 시 백엔드가 400 + ResponseDTO{error}를 주므로 메시지를 뽑아 던진다 */
function extractError(e: unknown, fallback: string): Error {
  if (axios.isAxiosError(e) && e.response?.data?.error) {
    return new Error(String(e.response.data.error));
  }
  return new Error(fallback);
}

export async function signin(username: string, password: string): Promise<Account> {
  try {
    const { data } = await client.post<Account>('/auth/signin', { username, password });
    return data;
  } catch (e) {
    throw extractError(e, '로그인에 실패했습니다.');
  }
}

export async function signup(username: string, password: string): Promise<Account> {
  try {
    const { data } = await client.post<Account>('/auth/signup', { username, password });
    return data;
  } catch (e) {
    throw extractError(e, '회원가입에 실패했습니다.');
  }
}

export type SocialProvider = 'google' | 'naver' | 'kakao' | 'github';

/**
 * 소셜 로그인 시작 URL.
 * RedirectUrlCookieFilter가 redirect_url 파라미터를 쿠키로 저장하고,
 * OAuthSuccessHandler가 `${redirect_url}/sociallogin?token=...` 으로 되돌려준다.
 */
export function socialLoginUrl(provider: SocialProvider): string {
  const redirect = encodeURIComponent(window.location.origin);
  return `${API_BASE_URL}/oauth2/authorization/${provider}?redirect_url=${redirect}`;
}
