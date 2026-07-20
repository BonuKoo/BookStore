import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ANONYMOUS, loadTossPayments } from '@tosspayments/tosspayments-sdk';
import type { CheckoutResult } from '../types/api';
import { usePageTitle } from '../hooks/usePageTitle';
import { formatWon } from '../util/format';
import Button from '../components/Button';
import Message from '../components/Message';

const CLIENT_KEY: string = import.meta.env.VITE_TOSS_CLIENT_KEY ?? '';

/** 토스 SDK v2 위젯 객체에서 이 페이지가 사용하는 부분만 정의 (메서드 시그니처는 구조적 타이핑으로 호환) */
interface PaymentWidgets {
  setAmount(amount: { currency: 'KRW'; value: number }): Promise<void>;
  renderPaymentMethods(options: { selector: string }): Promise<unknown>;
  renderAgreement(options: { selector: string }): Promise<unknown>;
  requestPayment(options: {
    orderId: string;
    orderName: string;
    successUrl: string;
    failUrl: string;
  }): Promise<void>;
}

/**
 * 결제 페이지 — CartPage에서 생성한 주문(CheckoutResult)을 받아
 * 토스 결제위젯을 렌더링하고 결제를 요청한다.
 * 성공 시 /payment/success?paymentKey&orderId&amount 로 리다이렉트되어
 * 백엔드 /v1/toss/confirm 승인으로 이어진다.
 */
export default function CheckoutPage() {
  usePageTitle('결제');
  const location = useLocation();
  const navigate = useNavigate();
  const order = (location.state as { order?: CheckoutResult } | null)?.order;

  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [paying, setPaying] = useState(false);
  const widgetsRef = useRef<PaymentWidgets | null>(null);

  useEffect(() => {
    if (!order) return;
    let cancelled = false;

    (async () => {
      try {
        const tossPayments = await loadTossPayments(CLIENT_KEY);
        const widgets = tossPayments.widgets({ customerKey: ANONYMOUS });
        if (cancelled) return;
        widgetsRef.current = widgets;

        await widgets.setAmount({ currency: 'KRW', value: order.amount });
        await Promise.all([
          widgets.renderPaymentMethods({ selector: '#payment-method' }),
          widgets.renderAgreement({ selector: '#agreement' }),
        ]);
        if (!cancelled) setReady(true);
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : '결제위젯 로딩에 실패했습니다.');
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [order]);

  if (!order) {
    // 새로고침 등으로 주문 정보가 없으면 장바구니로 되돌린다 (멱등키 덕분에 재주문해도 동일 주문)
    return (
      <div className="center">
        <Message variant="muted">주문 정보가 없습니다. 장바구니에서 다시 시도해 주세요.</Message>
        <Button variant="primary" onClick={() => navigate('/cart')}>
          장바구니로
        </Button>
      </div>
    );
  }

  const handlePay = async () => {
    if (!widgetsRef.current) return;
    setPaying(true);
    setError(null);
    try {
      await widgetsRef.current.requestPayment({
        orderId: order.orderId,
        orderName: order.orderName.slice(0, 100) || '도서 주문',
        successUrl: `${window.location.origin}/payment/success`,
        failUrl: `${window.location.origin}/payment/fail`,
      });
    } catch (e) {
      // 사용자가 결제창을 닫은 경우 등
      setError(e instanceof Error ? e.message : '결제 요청이 중단되었습니다.');
      setPaying(false);
    }
  };

  return (
    <div className="checkout">
      <h1>결제</h1>
      <div className="card order-summary">
        <p>
          <span className="muted">주문명</span> {order.orderName}
        </p>
        <p>
          <span className="muted">주문번호</span> <code>{order.orderId}</code>
        </p>
        {order.status === 'ALREADY_EXISTS' && (
          <Message variant="notice">이미 생성된 주문입니다. 이어서 결제를 진행합니다.</Message>
        )}
        <p className="price big">{formatWon(order.amount)}</p>
      </div>

      <div id="payment-method" />
      <div id="agreement" />

      {error && <Message variant="error">{error}</Message>}
      <Button variant="primary" size="lg" full disabled={!ready || paying} onClick={handlePay}>
        {paying ? '결제창 여는 중…' : `${formatWon(order.amount)} 결제하기`}
      </Button>
    </div>
  );
}
