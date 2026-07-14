/** 백엔드 공통 응답 포맷 (com.bookService.core.common.dto.ResponseDTO) */
export interface ResponseDTO<T> {
  error: string | null;
  data: T[] | null;
}

/** /auth/signin, /auth/signup 응답 (AccountDTO) */
export interface Account {
  id: number;
  username: string;
  token?: string;
}

/** 장바구니 한 줄 (CartListDTOForQueryProjection) */
export interface CartLine {
  name: string;
  price: number;
  amount: number;
  totPrice: number;
  isbn: string;
  cartItemId: number;
}

/** 장바구니 응답 (CartListResponseDTO) */
export interface CartListResponse {
  cartList: CartLine[];
  totalPrice: { cartTotPrice: number };
}

/** 담기 응답 (CartItemDTO) */
export interface CartItemAdded {
  cartItemId: number;
  itemIsbn: string;
  amount: number;
}

export type CheckoutStatus = 'SUCCESS' | 'ALREADY_EXISTS' | 'FAILED';

/** 주문 생성 응답 (CheckoutResult) */
export interface CheckoutResult {
  amount: number;
  orderId: string;
  orderName: string;
  status: CheckoutStatus;
}

/** 네이버 도서 검색 결과 (NaverBookController 내부 NaverBook) */
export interface NaverBook {
  title: string;
  isbn: string;
  image: string;
  author: string;
  discountFormatted: string;
}

/** 네이버 도서 상세 (NaverBookDetailViewResponseDto) */
export interface NaverBookDetail {
  channel: {
    title: string;
    total: number;
    items: Array<{
      title: string;
      link: string;
      image: string;
      author: string;
      discount: string;
      publisher: string;
      pubdate: string;
      isbn: string;
      description: string;
    }> | null;
  } | null;
}

/** 토스 confirm 응답 envelope (infra.toss.dto.ApiResponse) */
export interface TossApiResponse<T> {
  status: number;
  message: string;
  data: T | null;
}

/** 결제 승인 결과 (PaymentConfirmationResult) */
export interface PaymentConfirmationResult {
  status: 'SUCCESS' | 'FAILURE' | 'UNKNOWN';
  failure: { errorCode: string; message: string } | null;
  message: string;
}
