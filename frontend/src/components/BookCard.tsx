import { Link } from 'react-router-dom';
import { extractIsbn13 } from '../util/format';
import type { NaverBook } from '../types/api';

interface Props {
  book: NaverBook;
}

/** 도서 목록 한 칸 — props만 받아 그리는 표현 컴포넌트 */
export default function BookCard({ book }: Props) {
  const isbn13 = extractIsbn13(book.isbn);

  return (
    <li className="book-card">
      <Link to={`/books/${isbn13}`}>
        {book.image ? (
          <img src={book.image} alt={book.title} loading="lazy" />
        ) : (
          <div className="book-noimage">이미지 없음</div>
        )}
        <div className="book-info">
          <strong dangerouslySetInnerHTML={{ __html: book.title }} />
          <span className="muted">{book.author}</span>
          <span className="price">{book.discountFormatted}원</span>
        </div>
      </Link>
    </li>
  );
}
