import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { createCheckout } from '../api/checkout';
import { useCart } from '../hooks/useCart';
import { usePageTitle } from '../hooks/usePageTitle';
import { formatWon } from '../util/format';
import QuantityStepper from '../components/QuantityStepper';
import Button from '../components/Button';
import Message from '../components/Message';

export default function CartPage() {
  usePageTitle('장바구니');
  const navigate = useNavigate();
  const { cartQuery, amountMutation, removeMutation } = useCart();
  const { data: cart, isLoading, isError } = cartQuery;

  const checkoutMutation = useMutation({
    mutationFn: (cartItemIds: number[]) => createCheckout(cartItemIds),
    onSuccess: (order) => {
      // SUCCESS(신규) / ALREADY_EXISTS(멱등 재요청) 모두 결제 진행 가능
      navigate('/checkout', { state: { order } });
    },
  });

  if (isLoading) return <Message variant="muted">장바구니 불러오는 중…</Message>;
  if (isError) return <Message variant="error">장바구니를 불러오지 못했습니다.</Message>;

  const lines = cart?.cartList ?? [];
  const total = cart?.totalPrice?.cartTotPrice ?? 0;
  const busy =
    amountMutation.isPending || removeMutation.isPending || checkoutMutation.isPending;

  if (lines.length === 0) {
    return (
      <div className="center">
        <h1>장바구니</h1>
        <Message variant="muted">장바구니가 비어 있습니다.</Message>
        <Link to="/books" className="btn btn-primary">
          도서 보러 가기
        </Link>
      </div>
    );
  }

  return (
    <div>
      <h1>장바구니</h1>
      <table className="cart-table">
        <thead>
          <tr>
            <th>도서</th>
            <th>단가</th>
            <th>수량</th>
            <th>합계</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {lines.map((line) => (
            <tr key={line.cartItemId}>
              <td>
                <Link to={`/books/${line.isbn}`} dangerouslySetInnerHTML={{ __html: line.name }} />
              </td>
              <td>{formatWon(line.price)}</td>
              <td>
                <QuantityStepper
                  value={line.amount}
                  disabled={busy}
                  onChange={(next) => amountMutation.mutate({ isbn: line.isbn, amount: next })}
                />
              </td>
              <td>{formatWon(line.totPrice)}</td>
              <td>
                <Button
                  variant="danger"
                  size="sm"
                  disabled={busy}
                  onClick={() => removeMutation.mutate(line.isbn)}
                >
                  삭제
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="cart-footer">
        <strong className="price big">총 {formatWon(total)}</strong>
        <Button
          variant="primary"
          size="lg"
          disabled={busy}
          onClick={() => checkoutMutation.mutate(lines.map((l) => l.cartItemId))}
        >
          {checkoutMutation.isPending ? '주문 생성 중…' : '주문하기'}
        </Button>
      </div>
      {checkoutMutation.isError && (
        <Message variant="error">
          {checkoutMutation.error instanceof Error
            ? checkoutMutation.error.message
            : '주문 생성에 실패했습니다.'}
        </Message>
      )}
    </div>
  );
}
