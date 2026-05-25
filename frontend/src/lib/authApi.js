// 인증 관련 API 호출 모음입니다.

// 공통 API fetch 함수를 import합니다.
import { apiFetch } from './api.js';

// 회원가입 API를 호출합니다.
export async function signUp(payload) {
  // /auth/signup 엔드포인트에 POST 요청을 보냅니다.
  return await apiFetch('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(payload),
    // 이전 세션의 만료 토큰을 보내지 않음(로그인/가입 요청이 막히지 않게)
    skipAuth: true
  });
}

// 로그인 API를 호출합니다.
export async function login(payload) {
  // /auth/login 엔드포인트에 POST 요청을 보냅니다.
  return await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
    skipAuth: true
  });
}

