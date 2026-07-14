import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

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
          📚 core 북스토어
        </Link>
        <nav className="nav">
          <NavLink to="/books">도서 검색</NavLink>
          <NavLink to="/cart">장바구니</NavLink>
        </nav>
        <div className="header-right">
          {isAuthenticated ? (
            <>
              <span className="username">{username ?? '로그인됨'}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>
                로그아웃
              </button>
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
    </div>
  );
}
