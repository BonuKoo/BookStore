import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 백엔드 CORS 설정(WebSecurityConfig)이 http://localhost:5173 을 허용하므로 포트 고정
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});
