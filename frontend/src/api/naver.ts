import client from './client';
import type { NaverBook, NaverBookDetail } from '../types/api';

export async function searchBooks(
  query: string,
  display = 10,
  start = 1,
): Promise<NaverBook[]> {
  const { data } = await client.get<NaverBook[]>('/api/naver/search-books', {
    params: { query, display, start },
  });
  return data;
}

/**
 * 도서 상세 조회.
 * 주의: 백엔드가 이 호출에서 Item을 DB에 저장한다(saveItem 부수효과).
 * 장바구니 담기(/api/cart/add)는 Item이 DB에 있어야 성공하므로,
 * 담기는 반드시 상세 조회를 거친 뒤에 가능하다.
 */
export async function fetchBookDetail(isbn: string): Promise<NaverBookDetail> {
  const { data } = await client.get<NaverBookDetail>('/api/naver/bookDetail', {
    params: { isbn },
  });
  return data;
}
