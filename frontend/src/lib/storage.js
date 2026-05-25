// 로컬 스토리지 기반 인증 정보 저장 유틸입니다.

// Access Token을 저장하는 키입니다.
const TOKEN_KEY = 'basisi.accessToken';
// 사용자 역할을 저장하는 키입니다.
const ROLE_KEY = 'basisi.role';
// 사용자 이메일을 저장하는 키입니다.
const EMAIL_KEY = 'basisi.email';
// 사용자 표시 이름을 저장하는 키입니다.
const NAME_KEY = 'basisi.name';

// 선택된 시터 프로필 ID를 저장하는 키입니다.
// 새로고침 시에도 예약 폼에 반영되도록 sessionStorage를 사용합니다.
const PICKED_SITTER_PROFILE_ID_KEY = 'basisi.pickedSitterProfileId';

// 예약 시작/종료 시간 추천(시터 상세 기반)을 저장하는 키입니다.
// detail.availableStartTime/availableEndTime을 그대로 문자열로 저장합니다.
const SUGGESTED_RESERVATION_START_TIME_KEY = 'basisi.suggestedReservationStartTime';
const SUGGESTED_RESERVATION_END_TIME_KEY = 'basisi.suggestedReservationEndTime';

// Access Token을 저장합니다.
export function setAccessToken(token) {
  // 토큰이 없으면 키를 제거합니다.
  if (!token) {
    localStorage.removeItem(TOKEN_KEY);
    return;
  }
  // 토큰을 로컬 스토리지에 저장합니다.
  localStorage.setItem(TOKEN_KEY, token);
}

// Access Token을 읽어옵니다.
export function getAccessToken() {
  // 로컬 스토리지에서 토큰을 읽어 반환합니다.
  return localStorage.getItem(TOKEN_KEY);
}

// 토큰의 payload를 디코딩해 반환합니다. (디버깅/만료 확인용)
export function getDecodedTokenPayload() {
  // 저장된 토큰을 읽어옵니다.
  const token = getAccessToken();
  // 토큰이 없으면 null을 반환합니다.
  if (!token) return null;
  try {
    // JWT의 payload 부분(2번째 세그먼트)을 분리합니다.
    const payloadBase64Url = token.split('.')[1];
    // URL-safe Base64를 일반 Base64로 치환합니다.
    const payloadBase64 = payloadBase64Url.replace(/-/g, '+').replace(/_/g, '/');
    // Base64 디코딩 후 JSON 문자열을 얻습니다.
    const json = decodeURIComponent(
      atob(payloadBase64)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
    // JSON 객체로 파싱해 반환합니다.
    return JSON.parse(json);
  } catch (e) {
    // 파싱 실패 시 null을 반환합니다.
    return null;
  }
}

// JWT가 디코딩 가능하고 만료 전인지(라우트 가드·API와 동일 기준).
export function isAccessTokenValid() {
  const token = getAccessToken();
  if (!token || !token.trim()) return false;
  const payload = getDecodedTokenPayload();
  if (!payload || typeof payload.exp !== 'number') return false;
  return Date.now() < payload.exp * 1000;
}

// 사용자 역할을 저장합니다.
export function setRole(role) {
  // 역할이 없으면 키를 제거합니다.
  if (!role) {
    localStorage.removeItem(ROLE_KEY);
    return;
  }
  // 역할 문자열을 저장합니다.
  localStorage.setItem(ROLE_KEY, role);
}

// 사용자 역할을 읽어옵니다.
export function getRole() {
  // 로컬 스토리지에서 역할 값을 읽어 반환합니다.
  return localStorage.getItem(ROLE_KEY);
}

// 사용자 이메일을 저장합니다.
export function setEmail(email) {
  // 이메일이 없으면 키를 제거합니다.
  if (!email) {
    localStorage.removeItem(EMAIL_KEY);
    return;
  }
  // 이메일을 저장합니다.
  localStorage.setItem(EMAIL_KEY, email);
}

// 사용자 이메일을 읽어옵니다.
export function getEmail() {
  // 로컬 스토리지에서 이메일 값을 읽어 반환합니다.
  return localStorage.getItem(EMAIL_KEY);
}

// 사용자 표시 이름을 저장합니다.
export function setName(name) {
  if (!name) {
    localStorage.removeItem(NAME_KEY);
    return;
  }
  localStorage.setItem(NAME_KEY, name);
}

// 사용자 표시 이름을 읽어옵니다.
export function getName() {
  return localStorage.getItem(NAME_KEY);
}

// 로그아웃 처리를 위해 저장된 인증 정보를 모두 삭제합니다.
export function clearAuth() {
  // 토큰을 삭제합니다.
  localStorage.removeItem(TOKEN_KEY);
  // 역할을 삭제합니다.
  localStorage.removeItem(ROLE_KEY);
  // 이메일을 삭제합니다.
  localStorage.removeItem(EMAIL_KEY);
  // 표시 이름을 삭제합니다.
  localStorage.removeItem(NAME_KEY);
}

// 선택된 시터 프로필 ID 저장합니다.
export function setPickedSitterProfileId(sitterProfileId) {
  if (sitterProfileId === null || sitterProfileId === undefined || sitterProfileId === '' || Number.isNaN(Number(sitterProfileId))) {
    sessionStorage.removeItem(PICKED_SITTER_PROFILE_ID_KEY);
    return;
  }
  sessionStorage.setItem(PICKED_SITTER_PROFILE_ID_KEY, String(Number(sitterProfileId)));
}

// 선택된 시터 프로필 ID를 읽습니다.
export function getPickedSitterProfileId() {
  const raw = sessionStorage.getItem(PICKED_SITTER_PROFILE_ID_KEY);
  if (raw === null) return null;
  const n = Number(raw);
  return Number.isNaN(n) ? null : n;
}

// 선택된 시터 프로필 ID를 삭제합니다.
export function clearPickedSitterProfileId() {
  sessionStorage.removeItem(PICKED_SITTER_PROFILE_ID_KEY);
}

// 예약 추천 시간 저장합니다.
export function setSuggestedReservationWindow(startTime, endTime) {
  const s = startTime == null ? '' : String(startTime);
  const e = endTime == null ? '' : String(endTime);

  if (!s && !e) {
    sessionStorage.removeItem(SUGGESTED_RESERVATION_START_TIME_KEY);
    sessionStorage.removeItem(SUGGESTED_RESERVATION_END_TIME_KEY);
    return;
  }

  if (s) sessionStorage.setItem(SUGGESTED_RESERVATION_START_TIME_KEY, s);
  else sessionStorage.removeItem(SUGGESTED_RESERVATION_START_TIME_KEY);

  if (e) sessionStorage.setItem(SUGGESTED_RESERVATION_END_TIME_KEY, e);
  else sessionStorage.removeItem(SUGGESTED_RESERVATION_END_TIME_KEY);
}

// 예약 추천 시간 조회합니다.
export function getSuggestedReservationWindow() {
  const rawStart = sessionStorage.getItem(SUGGESTED_RESERVATION_START_TIME_KEY);
  const rawEnd = sessionStorage.getItem(SUGGESTED_RESERVATION_END_TIME_KEY);

  if (!rawStart && !rawEnd) return null;
  return { startTime: rawStart || null, endTime: rawEnd || null };
}

// 예약 추천 시간 삭제합니다.
export function clearSuggestedReservationWindow() {
  sessionStorage.removeItem(SUGGESTED_RESERVATION_START_TIME_KEY);
  sessionStorage.removeItem(SUGGESTED_RESERVATION_END_TIME_KEY);
}

