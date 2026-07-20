import { useEffect, useRef } from 'react';

/**
 * effect를 컴포넌트 생애 최초 1회만 실행한다.
 *
 * React 18 StrictMode의 개발 모드 이중 마운트에도 콜백이 한 번만 돌아야 하는
 * 부수효과(결제 승인 요청, 소셜 토큰 처리 등)에 쓴다. 기존 페이지들이 각자
 * `useRef(false)` 가드를 반복하던 패턴을 하나로 모은 것. (REACT_GUIDELINE §5)
 */
export function useRunOnce(effect: () => void): void {
  const ran = useRef(false);

  useEffect(() => {
    if (ran.current) return;
    ran.current = true;
    effect();
    // 최초 1회 실행이 목적이므로 effect를 deps에서 의도적으로 제외한다.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
