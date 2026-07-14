import axios from 'axios';
import client from './client';
import type { CheckoutResult, ResponseDTO } from '../types/api';

/**
 * 주문 생성 (멱등).
 * - 200: 신규 주문 생성 (status=SUCCESS)
 * - 409: 동일 장바구니 구성으로 이미 생성된 주문 (status=ALREADY_EXISTS, 동일 orderId 반환)
 * 두 경우 모두 결제를 이어갈 수 있으므로 CheckoutResult로 정상 반환한다.
 */
export async function createCheckout(cartItemIds: number[]): Promise<CheckoutResult> {
  try {
    const { data } = await client.post<ResponseDTO<CheckoutResult>>('/api/checkout', {
      cartItemIds,
    });
    if (data.error) throw new Error(data.error);
    return data.data![0];
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.status === 409) {
      const body = e.response.data as ResponseDTO<CheckoutResult>;
      if (body.data?.length) return body.data[0];
    }
    if (axios.isAxiosError(e) && e.response?.data?.error) {
      throw new Error(String(e.response.data.error));
    }
    throw e;
  }
}
