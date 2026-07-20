import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchBookDetail } from '../api/naver';
import { addToCart } from '../api/cart';
import { useAuth } from '../auth/AuthContext';
import { CART_QUERY_KEY } from '../hooks/useCart';
import { usePageTitle } from '../hooks/usePageTitle';
import { formatWon } from '../util/format';
import QuantityStepper from '../components/QuantityStepper';
import Button from '../components/Button';
import Message from '../components/Message';

/**
 * 도서 상세.
 * 백엔드가 상세 조회 시 Item을 DB에 저장하므로(saveItem),
 * "장바구니 담기"는 이 페이지를 거친 뒤에만 성공한다.
 */
export default function BookDetailPage() {
  usePageTitle('도서 상세');
  const { isbn = '' } = useParams();
  const [amount, setAmount] = useState(1);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['bookDetail', isbn],
    queryFn: () => fetchBookDetail(isbn),
    enabled: isbn.length > 0,
  });

  const addMutation = useMutation({
    mutationFn: () => addToCart(isbn, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY });
    },
  });

  if (isLoading) return <Message variant="muted">불러오는 중…</Message>;
  if (isError) return <Message variant="error">도서 정보를 불러오지 못했습니다.</Message>;

  const item = data?.channel?.items?.[0];
  if (!item) return <Message variant="error">해당 ISBN의 도서를 찾을 수 없습니다.</Message>;

  const handleAdd = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/books/${isbn}` } });
      return;
    }
    addMutation.mutate();
  };

  return (
    <div className="detail">
      <div className="detail-media">
        {item.image ? <img src={item.image} alt={item.title} /> : <div className="book-noimage">이미지 없음</div>}
      </div>
      <div className="detail-body">
        <h1 dangerouslySetInnerHTML={{ __html: item.title }} />
        <p className="muted">
          {item.author} · {item.publisher} · {item.pubdate}
        </p>
        <p className="price big">{formatWon(Number(item.discount))}</p>
        <p className="description" dangerouslySetInnerHTML={{ __html: item.description }} />

        <div className="detail-actions">
          <QuantityStepper value={amount} onChange={setAmount} />
          <Button variant="primary" onClick={handleAdd} disabled={addMutation.isPending}>
            {addMutation.isPending ? '담는 중…' : '장바구니 담기'}
          </Button>
          <Button onClick={() => navigate('/cart')}>장바구니 보기</Button>
        </div>
        {addMutation.isSuccess && <Message variant="success">장바구니에 담았습니다.</Message>}
        {addMutation.isError && (
          <Message variant="error">
            {addMutation.error instanceof Error ? addMutation.error.message : '담기에 실패했습니다.'}
          </Message>
        )}
      </div>
    </div>
  );
}
