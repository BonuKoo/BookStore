import type { ReactNode } from 'react';

type Variant = 'error' | 'muted' | 'success' | 'notice';

interface Props {
  variant: Variant;
  children: ReactNode;
  center?: boolean;
}

/**
 * 피드백 문구 (에러/안내/성공/일반). 페이지마다 흩어져 있던
 * `<p className="error">` 류를 하나의 props-only 컴포넌트로 모은 것.
 * className은 index.css의 동명 클래스와 연결된다.
 */
export default function Message({ variant, children, center = false }: Props) {
  return <p className={center ? `${variant} center` : variant}>{children}</p>;
}
