import type { SocialProvider } from '../api/auth';

/** 도서 목록 페이지네이션 크기 */
export const PAGE_SIZE = 10;

/** 도서 검색 기본 질의어 */
export const DEFAULT_QUERY = 'spring';

/** 로그인 페이지에 노출할 소셜 로그인 제공자 목록 */
export const SOCIAL_PROVIDERS: ReadonlyArray<{ id: SocialProvider; label: string }> = [
  { id: 'google', label: 'Google' },
  { id: 'naver', label: '네이버' },
  { id: 'kakao', label: '카카오' },
  { id: 'github', label: 'GitHub' },
];
