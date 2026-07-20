import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCart, removeCartItem, setCartItemAmount } from '../api/cart';

/** 장바구니 관련 쿼리 무효화에 쓰는 공용 키 (여러 페이지가 참조) */
export const CART_QUERY_KEY = ['cart'] as const;

/**
 * 장바구니 조회 + 수량 변경/삭제 뮤테이션을 한 곳에 모은 훅.
 * 성공 시 cart 쿼리를 무효화해 목록을 최신화한다. (REACT_GUIDELINE §5)
 *
 * 수량 변경은 백엔드가 "덮어쓰기" 시맨틱이므로 최종 수량(absolute)을 그대로 넘긴다.
 */
export function useCart() {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY });

  const cartQuery = useQuery({ queryKey: CART_QUERY_KEY, queryFn: fetchCart });

  const amountMutation = useMutation({
    mutationFn: ({ isbn, amount }: { isbn: string; amount: number }) =>
      setCartItemAmount(isbn, amount),
    onSuccess: invalidate,
  });

  const removeMutation = useMutation({
    mutationFn: (isbn: string) => removeCartItem(isbn),
    onSuccess: invalidate,
  });

  return { cartQuery, amountMutation, removeMutation, invalidate };
}
