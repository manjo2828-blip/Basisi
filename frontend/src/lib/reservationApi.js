// 예약 관련 API 호출 모음입니다.

// 공통 API fetch 함수를 import합니다.
import { apiFetch } from './api.js';

// 예약 신청 API를 호출합니다.
export async function requestReservation(payload) {
  // 예약 신청 엔드포인트로 POST 요청을 보냅니다.
  return await apiFetch('/reservations', {
    // HTTP 메서드를 지정합니다.
    method: 'POST',
    // 요청 바디를 JSON 문자열로 전송합니다.
    body: JSON.stringify(payload)
  });
}

// 내 예약 목록을 조회합니다.
export async function getMyReservations() {
  // 내 예약 목록 엔드포인트로 GET 요청을 보냅니다.
  return await apiFetch('/reservations/me', {
    // HTTP 메서드를 지정합니다.
    method: 'GET'
  });
}

// 예약 수락 API를 호출합니다.
export async function acceptReservation(reservationId) {
  // POST + JSON 본문: PATCH 빈 본문 조합에서 프록시/CORS와 Authorization이 어긋나 403이 나는 환경을 피합니다.
  return await apiFetch(`/reservations/${reservationId}/accept`, {
    method: 'POST',
    body: JSON.stringify({})
  });
}

// 예약 거절 API를 호출합니다.
export async function rejectReservation(reservationId) {
  return await apiFetch(`/reservations/${reservationId}/reject`, {
    method: 'POST',
    body: JSON.stringify({})
  });
}

// 예약 취소 API를 호출합니다.
export async function cancelReservation(reservationId) {
  return await apiFetch(`/reservations/${reservationId}/cancel`, {
    method: 'POST',
    body: JSON.stringify({})
  });
}

