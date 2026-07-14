import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { confirmPayment } from '../api/toss';
import type { PaymentConfirmationResult } from '../types/api';

type Phase = 'confirming' | 'done' | 'error';

/**
 * 토스 successUrl 콜백 — paymentKey/orderId/amount를 받아
 * 백엔드 /v1/toss/confirm 으로 최종 승인한다.
 */
export default function PaymentSuccessPage() {
  const [params] = useSearchParams();
  const [phase, setPhase] = useState<Phase>('confirming');
  const [result, setResult] = useState<PaymentConfirmationResult | null>(null);
  const [message, setMessage] = useState('');
  const requested = useRef(false);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (requested.current) return; // StrictMode 이중 실행 방지 (승인 요청은 1회만)
    requested.current = true;

    const paymentKey = params.get('paymentKey');
    const orderId = params.get('orderId');
    const amount = Number(params.get('amount'));

    if (!paymentKey || !orderId || !Number.isFinite(amount)) {
      setPhase('error');
      setMessage('결제 승인에 필요한 정보가 누락되었습니다.');
      return;
    }

    confirmPayment({ paymentKey, orderId, amount })
      .then((r) => {
        setResult(r);
        setPhase(r.status === 'SUCCESS' ? 'done' : 'error');
        setMessage(r.message);
        queryClient.invalidateQueries({ queryKey: ['cart'] });
      })
      .catch((e) => {
        setPhase('error');
        setMessage(e instanceof Error ? e.message : '결제 승인 중 오류가 발생했습니다.');
      });
  }, [params, queryClient]);

  if (phase === 'confirming') {
    return (
      <div className="center">
        <h1>결제 승인 중…</h1>
        <p className="muted">창을 닫지 말고 잠시만 기다려 주세요.</p>
      </div>
    );
  }

  if (phase === 'done') {
    return (
      <div className="center">
        <h1>✅ 결제가 완료되었습니다</h1>
        <p>{message}</p>
        <p className="muted">
          주문번호 <code>{params.get('orderId')}</code>
        </p>
        <Link to="/books" className="btn btn-primary">
          쇼핑 계속하기
        </Link>
      </div>
    );
  }

  return (
    <div className="center">
      <h1>⚠️ 결제 승인 실패</h1>
      <p className="error">{message}</p>
      {result?.failure && (
        <p className="muted">
          ({result.failure.errorCode}) {result.failure.message}
        </p>
      )}
      <Link to="/cart" className="btn">
        장바구니로 돌아가기
      </Link>
    </div>
  );
}
