import { Link, useSearchParams } from 'react-router-dom';

/** 토스 failUrl 콜백 — code/message 쿼리 파라미터로 실패 사유가 전달된다 */
export default function PaymentFailPage() {
  const [params] = useSearchParams();
  const code = params.get('code');
  const message = params.get('message');

  return (
    <div className="center">
      <h1>결제에 실패했습니다</h1>
      <p className="error">{message ?? '알 수 없는 오류가 발생했습니다.'}</p>
      {code && <p className="muted">오류 코드: {code}</p>}
      <Link to="/cart" className="btn btn-primary">
        장바구니로 돌아가기
      </Link>
    </div>
  );
}
