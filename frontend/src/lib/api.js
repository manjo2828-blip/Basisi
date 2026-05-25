// 백엔드 API 호출을 위한 fetch 래퍼입니다.

// 인증 토큰을 가져오는 함수입니다.
import { getAccessToken } from './storage.js';

// VITE_API_BASE 를 지정하면 항상 그 값을 씁니다. (예: https://api.example.com/api)
// 로컬 개발·preview(5173/4173)에서는 동일 출처 `/api` → Vite 프록시 → localhost:8080 으로 보냅니다.
// (브라우저에서 8080으로 직접 호출하면 CORS·Authorization 누락으로 403이 나는 경우가 많습니다.)
export function getApiBase() {
  const explicit = import.meta.env.VITE_API_BASE?.trim();
  if (explicit) return explicit.replace(/\/$/, '');

  if (typeof window !== 'undefined') {
    const port = window.location.port;
    if (import.meta.env.DEV || port === '5173' || port === '4173') {
      return '/api';
    }
  }
  return '/api';
}

// API 요청 중 에러를 표현하는 커스텀 에러 클래스입니다.
export class ApiError extends Error {
  // 생성자에서 메시지와 상태코드를 받습니다.
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

// JSON 응답을 안전하게 파싱합니다.
async function safeJson(response) {
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) return null;
  return await response.json();
}

// 공통 API 요청 함수입니다.
// options.public: 공개 GET — Authorization 미전송
// options.skipAuth: 로그인/회원가입 등 — Authorization 미전송(만료 토큰이 로그인을 막지 않게)
export async function apiFetch(path, rawOptions = {}) {
  const {
    public: publicRead = false,
    skipAuth = false,
    timeoutMs = 30000,
    ...rest
  } = rawOptions;

  const method = (rest.method || 'GET').toUpperCase();
  const token = publicRead || skipAuth ? null : getAccessToken();

  const headers = {
    ...(rest.headers || {})
  };

  const hasBody = rest.body !== undefined && rest.body !== null && rest.body !== '';
  const isFormData = typeof FormData !== 'undefined' && rest.body instanceof FormData;
  // 본문이 있을 때만 JSON 타입 지정(빈 PATCH 등에서 불필요한 Content-Type·프리플라이트 부담 감소)
  if (hasBody && !isFormData && !headers['Content-Type'] && !headers['content-type']) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const controller = new AbortController();
  const timeoutId =
    timeoutMs > 0 && !rest.signal
      ? setTimeout(() => controller.abort(), timeoutMs)
      : null;

  const fetchOptions = {
    ...rest,
    headers,
    signal: rest.signal ?? controller.signal
  };

  const apiBase = getApiBase();

  console.info('[Basisi API Request]', {
    method,
    path: `${apiBase}${path}`,
    hasToken: Boolean(token),
    publicRead,
    skipAuth,
    tokenPreview: token ? `${token.slice(0, 16)}...` : null,
    origin: window.location.origin
  });

  let response;
  try {
    response = await fetch(`${apiBase}${path}`, fetchOptions);
  } catch (e) {
    if (e?.name === 'AbortError') {
      throw new ApiError(
        '서버 응답이 지연되거나 없습니다. 백엔드가 실행 중인지(포트 8080) 확인한 뒤 다시 시도해 주세요.',
        0
      );
    }
    throw e;
  } finally {
    if (timeoutId) clearTimeout(timeoutId);
  }

  const body = await safeJson(response);

  console.info('[Basisi API Response]', {
    method,
    path: `${apiBase}${path}`,
    status: response.status,
    ok: response.ok
  });

  if (response.ok) return body;

  let message = body?.message || `요청에 실패했습니다. (HTTP ${response.status})`;
  if (response.status === 401) {
    message = token
      ? '로그인이 만료되었습니다. 다시 로그인해 주세요.'
      : '로그인이 필요합니다. 로그인 후 다시 시도해 주세요.';
  } else if (response.status === 403) {
    message = token
      ? '접근이 거부되었습니다(403). 백엔드를 최신 코드로 재시작했는지 확인하고, 그래도 같으면 로그아웃 후 다시 로그인해 주세요.'
      : '로그인이 필요합니다. 로그인 후 다시 시도해 주세요.';
  }
  throw new ApiError(message, response.status);
}
