import { useState } from 'react';
import type { FormEvent } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { searchBooks } from '../api/naver';

const PAGE_SIZE = 10;

export default function BookListPage() {
  const [input, setInput] = useState('');
  const [query, setQuery] = useState('spring');
  const [page, setPage] = useState(1);

  const { data: books, isFetching, isError } = useQuery({
    queryKey: ['books', query, page],
    queryFn: () => searchBooks(query, PAGE_SIZE, (page - 1) * PAGE_SIZE + 1),
    placeholderData: keepPreviousData,
  });

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;
    setQuery(input.trim());
    setPage(1);
  };

  return (
    <div>
      <h1>도서 검색</h1>
      <form onSubmit={handleSearch} className="search-bar">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="검색어를 입력하세요"
        />
        <button className="btn btn-primary">검색</button>
      </form>

      {isError && <p className="error">도서 검색에 실패했습니다. 잠시 후 다시 시도해 주세요.</p>}
      {isFetching && <p className="muted">검색 중…</p>}

      <ul className="book-grid">
        {books?.map((book) => {
          // 네이버 isbn은 "10자리 13자리" 형태일 수 있어 마지막 토큰(13자리) 사용
          const isbn13 = book.isbn.trim().split(/\s+/).pop() ?? book.isbn;
          return (
            <li key={`${isbn13}-${book.title}`} className="book-card">
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
        })}
      </ul>

      <div className="pager">
        <button
          className="btn"
          disabled={page <= 1 || isFetching}
          onClick={() => setPage((p) => p - 1)}
        >
          이전
        </button>
        <span className="muted">{page} 페이지</span>
        <button
          className="btn"
          disabled={isFetching || (books?.length ?? 0) < PAGE_SIZE}
          onClick={() => setPage((p) => p + 1)}
        >
          다음
        </button>
      </div>
    </div>
  );
}
