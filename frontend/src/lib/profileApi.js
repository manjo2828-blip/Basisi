// 프로필 관련 API 호출 모음입니다.

// 공통 API fetch 함수를 import합니다.
import { apiFetch, getApiBase } from './api.js';

// 내 부모 프로필을 조회합니다.
export async function getMyParentProfile(options = {}) {
  return await apiFetch('/profile/parent/me', {
    method: 'GET',
    ...options
  });
}

// 내 부모 프로필을 생성/수정합니다.
export async function upsertMyParentProfile(payload, options = {}) {
  return await apiFetch('/profile/parent/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
    ...options
  });
}

// 내 부모 프로필을 삭제합니다.
export async function deleteMyParentProfile() {
  // 부모 프로필 삭제 엔드포인트를 호출합니다.
  return await apiFetch('/profile/parent/me', {
    // HTTP 메서드를 지정합니다.
    method: 'DELETE'
  });
}

// 내 시터 프로필을 조회합니다.
export async function getMySitterProfile() {
  // 시터 프로필 조회 엔드포인트를 호출합니다.
  return await apiFetch('/profile/sitter/me', {
    // HTTP 메서드를 지정합니다.
    method: 'GET'
  });
}

// 내 시터 프로필을 생성/수정합니다.
export async function upsertMySitterProfile(payload) {
  // 시터 프로필 upsert 엔드포인트를 호출합니다.
  return await apiFetch('/profile/sitter/me', {
    // HTTP 메서드를 지정합니다.
    method: 'PUT',
    // 요청 바디를 JSON 문자열로 전송합니다.
    body: JSON.stringify(payload)
  });
}

// 내 시터 프로필을 삭제합니다.
export async function deleteMySitterProfile() {
  // 시터 프로필 삭제 엔드포인트를 호출합니다.
  return await apiFetch('/profile/sitter/me', {
    // HTTP 메서드를 지정합니다.
    method: 'DELETE'
  });
}

/** 공개 URL — 시터 프로필 이미지 표시용 */
export function getSitterProfileImageUrl(imageId) {
  return `${getApiBase()}/public/sitter-images/${imageId}`;
}

/** 시터 프로필 이미지 1장 업로드 (최대 5장, 서버 정책) */
export async function uploadSitterProfileImage(file) {
  const body = new FormData();
  body.append('file', file);
  return await apiFetch('/profile/sitter/me/images', {
    method: 'POST',
    body
  });
}

export async function deleteSitterProfileImage(id) {
  return await apiFetch(`/profile/sitter/me/images/${encodeURIComponent(id)}`, {
    method: 'DELETE'
  });
}

