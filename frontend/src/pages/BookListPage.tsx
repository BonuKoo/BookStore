import { useState } from 'react';
import type { FormEvent } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { searchBooks } from '../api/naver';
import { DEFAULT_QUERY, PAGE_SIZE } from '../util/constants';
import { usePageTitle } from '../hooks/usePageTitle';
import BookCard from '../components/BookCard';
import Button from '../components/Button';
import Message from '../components/Message';

export default function BookListPage() {
  usePageTitle('도서 검색');
  const [input, setInput] = useState('');
  const [query, setQuery] = useState(DEFAULT_QUERY);
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
        <Button variant="primary">검색</Button>
      </form>

      {isError && <Message variant="error">도서 검색에 실패했습니다. 잠시 후 다시 시도해 주세요.</Message>}
      {isFetching && <Message variant="muted">검색 중…</Message>}

      <ul className="book-grid">
        {books?.map((book) => (
          <BookCard key={`${book.isbn}-${book.title}`} book={book} />
        ))}
      </ul>

      <div className="pager">
        <Button disabled={page <= 1 || isFetching} onClick={() => setPage((p) => p - 1)}>
          이전
        </Button>
        <span className="muted">{page} 페이지</span>
        <Button
          disabled={isFetching || (books?.length ?? 0) < PAGE_SIZE}
          onClick={() => setPage((p) => p + 1)}
        >
          다음
        </Button>
      </div>
    </div>
  );
}
