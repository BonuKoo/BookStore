/**
 * React 의존성 없는 순수 포맷/파싱 함수 모음.
 * 컴포넌트 안에서 반복되던 계산을 이곳으로 뺀다. (REACT_GUIDELINE §1)
 */

/** 원화 표기: 1234567 → "1,234,567원" */
export function formatWon(value: number): string {
  return `${value.toLocaleString()}원`;
}

/**
 * 네이버 도서 isbn은 "1041033729 9791041033720"처럼 10자리와 13자리가
 * 공백으로 이어진 형태일 수 있다. 라우팅·상세조회에는 마지막 토큰(13자리)을 쓴다.
 */
export function extractIsbn13(raw: string): string {
  return raw.trim().split(/\s+/).pop() ?? raw;
}
