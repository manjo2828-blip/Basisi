// Vite 설정 파일입니다.
import { defineConfig } from 'vite';
// React Fast Refresh 등을 활성화하는 플러그인입니다.
import react from '@vitejs/plugin-react';

// Vite 개발 서버 및 빌드 설정을 정의합니다.
export default defineConfig({
  // React 플러그인을 등록합니다.
  plugins: [react()],
  // 개발 서버 설정입니다.
  server: {
    // 기본 개발 서버 포트를 5173으로 고정합니다.
    port: 5173,
    // 백엔드로 API 프록시를 걸어서 CORS 이슈를 줄입니다.
    proxy: {
      // /api로 시작하는 요청은 백엔드로 전달합니다.
      // Windows + Node 18+에서 'localhost'가 IPv6(::1)로 먼저 풀려서 막히는 경우가 있어
      // 명시적으로 IPv4(127.0.0.1)를 지정합니다.
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: false,
        secure: false,
        ws: false,
        xfwd: true
      }
    }
  }
});

