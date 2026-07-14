import axios from 'axios';
import client from './client';
import type { PaymentConfirmationResult, TossApiResponse } from '../types/api';

/** 결제 승인 — 백엔드가 토스 API를 호출하고 결제 상태를 확정한다 */
export async function confirmPayment(params: {
  paymentKey: string;
  orderId: string;
  amount: number;
}): Promise<PaymentConfirmationResult> {
  try {
    const { data } = await client.post<TossApiResponse<PaymentConfirmationResult>>(
      '/v1/toss/confirm',
      params,
    );
    if (!data.data) {
      throw new Error(data.message || '결제 승인 응답이 비어 있습니다.');
    }
    return data.data;
  } catch (e) {
    // 이미 처리된 결제 등은 500 + ApiResponse{message}로 내려온다
    if (axios.isAxiosError(e) && e.response?.data?.message) {
      throw new Error(String(e.response.data.message));
    }
    throw e;
  }
}
