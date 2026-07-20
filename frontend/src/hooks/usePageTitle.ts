import { useEffect } from 'react';

const BASE_TITLE = 'core 북스토어';

/**
 * 페이지별 document.title 동기화.
 * document 조작은 "외부 시스템과의 동기화"라 useEffect의 적정 용도다. (REACT_GUIDELINE §3)
 */
export function usePageTitle(title?: string): void {
  useEffect(() => {
    document.title = title ? `${title} · ${BASE_TITLE}` : BASE_TITLE;
  }, [title]);
}
