import client from './client';
import type { CartItemAdded, CartListResponse, ResponseDTO } from '../types/api';

export async function fetchCart(): Promise<CartListResponse> {
  const { data } = await client.get<ResponseDTO<CartListResponse>>('/api/cart/list');
  if (data.error) throw new Error(data.error);
  if (!data.data?.length) throw new Error('장바구니 응답이 비어 있습니다.');
  return data.data[0];
}

/** 담기 — 이미 담긴 ISBN이면 백엔드가 수량을 가산한다 */
export async function addToCart(isbn: string, amount: number): Promise<CartItemAdded> {
  const { data } = await client.post<ResponseDTO<CartItemAdded>>(
    '/api/cart/add',
    null,
    { params: { isbn, amount } },
  );
  if (data.error) throw new Error(data.error);
  return data.data![0];
}

/**
 * 수량 변경 — 백엔드 updateCartItemAmount는 "덮어쓰기" 시맨틱이므로
 * 증가/감소 모두 최종 수량(absolute)을 계산해 increaseItem 하나로 보낸다.
 */
export async function setCartItemAmount(itemIsbn: string, amount: number): Promise<void> {
  await client.post('/api/cartItem/increaseItem', { itemIsbn, amount });
}

export async function removeCartItem(itemIsbn: string): Promise<void> {
  await client.post('/api/cartItem/removeItem', null, { params: { itemIsbn } });
}
