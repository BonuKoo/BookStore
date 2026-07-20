import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Button from './Button';

export default function Layout() {
  const { isAuthenticated, username, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="app">
      <header className="header">
        <Link to="/" className="brand">
          Core Bookstore
        </Link>
        <nav className="nav">
          <NavLink to="/books">도서 검색</NavLink>
          <NavLink to="/cart">장바구니</NavLink>
        </nav>
        <div className="header-right">
          {isAuthenticated ? (
            <>
              <span className="username">{username ?? '로그인됨'}</span>
              <Button variant="ghost" onClick={handleLogout}>
                로그아웃
              </Button>
            </>
          ) : (
            <Link to="/login" className="btn btn-ghost">
              로그인
            </Link>
          )}
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
      <footer className="footer">
        <div className="footer-inner">
          <span className="footer-brand">Core Bookstore</span>
          <p className="footer-note">읽을 책을 고르는 일이 곧 취향이 되는 곳.</p>
          <p className="footer-meta">© 2026 CORE BOOKSTORE</p>
        </div>
      </footer>
    </div>
  );
}
