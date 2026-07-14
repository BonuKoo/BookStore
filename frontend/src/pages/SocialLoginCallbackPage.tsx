import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/**
 * 소셜 로그인 콜백: OAuthSuccessHandler가 /sociallogin?token=... 으로 리다이렉트한다.
 * 토큰은 URL 쿼리로 오므로, 저장 즉시 replace로 히스토리에서 제거한다.
 */
export default function SocialLoginCallbackPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token && token !== 'null') {
      login(token);
      // 토큰이 브라우저 히스토리에 남지 않도록 쿼리 제거 후 이동
      window.history.replaceState(null, '', '/sociallogin');
      navigate('/books', { replace: true });
    } else {
      navigate('/login', { replace: true });
    }
  }, [login, navigate]);

  return <p className="muted center">소셜 로그인 처리 중…</p>;
}
