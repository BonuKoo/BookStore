import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchBookDetail } from '../api/naver';
import { addToCart } from '../api/cart';
import { useAuth } from '../auth/AuthContext';
import QuantityStepper from '../components/QuantityStepper';

/**
 * 도서 상세.
 * 백엔드가 상세 조회 시 Item을 DB에 저장하므로(saveItem),
 * "장바구니 담기"는 이 페이지를 거친 뒤에만 성공한다.
 */
export default function BookDetailPage() {
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
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });

  if (isLoading) return <p className="muted">불러오는 중…</p>;
  if (isError) return <p className="error">도서 정보를 불러오지 못했습니다.</p>;

  const item = data?.channel?.items?.[0];
  if (!item) return <p className="error">해당 ISBN의 도서를 찾을 수 없습니다.</p>;

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
        <p className="price big">{Number(item.discount).toLocaleString()}원</p>
        <p className="description" dangerouslySetInnerHTML={{ __html: item.description }} />

        <div className="detail-actions">
          <QuantityStepper value={amount} onChange={setAmount} />
          <button
            className="btn btn-primary"
            onClick={handleAdd}
            disabled={addMutation.isPending}
          >
            {addMutation.isPending ? '담는 중…' : '장바구니 담기'}
          </button>
          <button className="btn" onClick={() => navigate('/cart')}>
            장바구니 보기
          </button>
        </div>
        {addMutation.isSuccess && <p className="success">장바구니에 담았습니다.</p>}
        {addMutation.isError && (
          <p className="error">
            {addMutation.error instanceof Error
              ? addMutation.error.message
              : '담기에 실패했습니다.'}
          </p>
        )}
      </div>
    </div>
  );
}
