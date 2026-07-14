import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { fetchCart, removeCartItem, setCartItemAmount } from '../api/cart';
import { createCheckout } from '../api/checkout';
import QuantityStepper from '../components/QuantityStepper';

export default function CartPage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const { data: cart, isLoading, isError } = useQuery({
    queryKey: ['cart'],
    queryFn: fetchCart,
  });

  const invalidateCart = () => queryClient.invalidateQueries({ queryKey: ['cart'] });

  const amountMutation = useMutation({
    mutationFn: ({ isbn, amount }: { isbn: string; amount: number }) =>
      setCartItemAmount(isbn, amount),
    onSuccess: invalidateCart,
  });

  const removeMutation = useMutation({
    mutationFn: (isbn: string) => removeCartItem(isbn),
    onSuccess: invalidateCart,
  });

  const checkoutMutation = useMutation({
    mutationFn: (cartItemIds: number[]) => createCheckout(cartItemIds),
    onSuccess: (order) => {
      // SUCCESS(신규) / ALREADY_EXISTS(멱등 재요청) 모두 결제 진행 가능
      navigate('/checkout', { state: { order } });
    },
  });

  if (isLoading) return <p className="muted">장바구니 불러오는 중…</p>;
  if (isError) return <p className="error">장바구니를 불러오지 못했습니다.</p>;

  const lines = cart?.cartList ?? [];
  const total = cart?.totalPrice?.cartTotPrice ?? 0;
  const busy =
    amountMutation.isPending || removeMutation.isPending || checkoutMutation.isPending;

  if (lines.length === 0) {
    return (
      <div className="center">
        <h1>장바구니</h1>
        <p className="muted">장바구니가 비어 있습니다.</p>
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
              <td>{line.price.toLocaleString()}원</td>
              <td>
                <QuantityStepper
                  value={line.amount}
                  disabled={busy}
                  onChange={(next) =>
                    amountMutation.mutate({ isbn: line.isbn, amount: next })
                  }
                />
              </td>
              <td>{line.totPrice.toLocaleString()}원</td>
              <td>
                <button
                  className="btn btn-sm btn-danger"
                  disabled={busy}
                  onClick={() => removeMutation.mutate(line.isbn)}
                >
                  삭제
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="cart-footer">
        <strong className="price big">총 {total.toLocaleString()}원</strong>
        <button
          className="btn btn-primary btn-lg"
          disabled={busy}
          onClick={() => checkoutMutation.mutate(lines.map((l) => l.cartItemId))}
        >
          {checkoutMutation.isPending ? '주문 생성 중…' : '주문하기'}
        </button>
      </div>
      {checkoutMutation.isError && (
        <p className="error">
          {checkoutMutation.error instanceof Error
            ? checkoutMutation.error.message
            : '주문 생성에 실패했습니다.'}
        </p>
      )}
    </div>
  );
}
