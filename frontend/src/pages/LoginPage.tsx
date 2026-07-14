import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { signin, socialLoginUrl } from '../api/auth';
import type { SocialProvider } from '../api/auth';
import { useAuth } from '../auth/AuthContext';

const PROVIDERS: Array<{ id: SocialProvider; label: string }> = [
  { id: 'google', label: 'Google' },
  { id: 'naver', label: '네이버' },
  { id: 'kakao', label: '카카오' },
  { id: 'github', label: 'GitHub' },
];

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/books';

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const account = await signin(username, password);
      if (!account.token) throw new Error('토큰이 응답에 없습니다.');
      login(account.token, account.username);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : '로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card auth-card">
      <h1>로그인</h1>
      <form onSubmit={handleSubmit} className="form">
        <label>
          아이디
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" disabled={loading}>
          {loading ? '로그인 중…' : '로그인'}
        </button>
      </form>

      <div className="divider">또는 소셜 계정으로</div>
      <div className="social-buttons">
        {PROVIDERS.map((p) => (
          <a key={p.id} className={`btn btn-social ${p.id}`} href={socialLoginUrl(p.id)}>
            {p.label}
          </a>
        ))}
      </div>

      <p className="muted">
        계정이 없으신가요? <Link to="/signup">회원가입</Link>
      </p>
    </div>
  );
}
