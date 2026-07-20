// 공용 헤더/네비 — React의 Layout.tsx에 대응. 각 페이지가 로드 시 한 번 호출한다.
import { isAuthenticated, clearSession, getToken } from './api.js';

export function mountHeader(activePath) {
  const root = document.getElementById('app-header');
  if (!root) return;

  const authed = isAuthenticated();
  const username = localStorage.getItem('username');

  root.innerHTML = `
    <a href="books.html" class="brand">Core Bookstore · V</a>
    <nav class="nav">
      <a href="books.html" class="${activePath === 'books' ? 'active' : ''}">도서 검색</a>
      <a href="cart.html" class="${activePath === 'cart' ? 'active' : ''}">장바구니</a>
    </nav>
    <div class="header-right">
      ${
        authed
          ? `<span class="username">${username ?? '로그인됨'}</span>
             <button class="btn btn-ghost" id="logout-btn">로그아웃</button>`
          : `<a href="login.html" class="btn btn-ghost">로그인</a>`
      }
    </div>
  `;

  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      clearSession();
      location.href = 'books.html';
    });
  }

  mountFooter();
}

/** 잉크 인버전 푸터 — React의 Layout.tsx 푸터에 대응. mountHeader가 함께 붙인다. */
export function mountFooter() {
  if (document.querySelector('.footer')) return;
  const footer = document.createElement('footer');
  footer.className = 'footer';
  footer.innerHTML = `
    <div class="footer-inner">
      <span class="footer-brand">Core Bookstore</span>
      <p class="footer-note">읽을 책을 고르는 일이 곧 취향이 되는 곳.</p>
      <p class="footer-meta">© 2026 CORE BOOKSTORE</p>
    </div>
  `;
  document.body.appendChild(footer);
}

/** 로그인 필요 페이지 상단에서 호출 — 미로그인 시 로그인으로 보내고 복귀 경로를 남긴다. */
export function requireAuth(returnTo) {
  if (!getToken()) {
    const from = returnTo ?? location.pathname.split('/').pop();
    location.href = `login.html?from=${encodeURIComponent(from)}`;
    return false;
  }
  return true;
}

export function formatWon(value) {
  return `${Number(value).toLocaleString()}원`;
}

/** 네이버 isbn "10자리 13자리" 형태에서 마지막 토큰(13자리)만 취한다. */
export function extractIsbn13(raw) {
  const parts = String(raw).trim().split(/\s+/);
  return parts[parts.length - 1] || raw;
}

export function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}
