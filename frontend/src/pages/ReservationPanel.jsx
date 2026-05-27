// 예약(Phase2) UI를 제공하는 패널입니다.
import React, { useEffect, useMemo, useRef, useState } from 'react';
// 공통 입력 컴포넌트입니다.
import { TextInput } from '../components/TextInput.jsx';
// 예약 API 호출 함수입니다.
import {
  acceptReservation,
  cancelReservation,
  getMyReservations,
  rejectReservation,
  requestReservation
} from '../lib/reservationApi.js';
import { createReview } from '../lib/reviewApi.js';
// API 에러 타입입니다.
import { ApiError } from '../lib/api.js';
import { clearSuggestedReservationWindow, getSuggestedReservationWindow } from '../lib/storage.js';

import './ReservationPanel.css';

const STATUS_KOREAN = {
  REQUESTED: '신청됨',
  ACCEPTED: '수락됨',
  REJECTED: '거절됨',
  CANCELLED: '취소됨',
  CANCELED: '취소됨',
};

const STATUS_BADGE_CLASS = {
  REQUESTED: 'is-status-requested',
  ACCEPTED: 'is-status-accepted',
  REJECTED: 'is-status-rejected',
  CANCELLED: 'is-status-cancelled',
  CANCELED: 'is-status-cancelled',
};

// 예약 패널을 렌더링합니다.
export function ReservationPanel({ session, onToast, pickedSitter }) {
  // 로그인 상태를 확인합니다.
  const email = useMemo(() => session?.email || null, [session]);
  // 역할을 확인합니다.
  const role = useMemo(() => session?.role || null, [session]);

  // 예약 신청 폼 상태입니다.
  const [startAt, setStartAt] = useState('');
  const [endAt, setEndAt] = useState('');
  const [note, setNote] = useState('처음 예약입니다.');

  const selectedSitterName = pickedSitter?.name || null;
  const selectedSitterId = pickedSitter?.sitterProfileId ?? null;

  // 내 예약 목록 상태입니다.
  const [reservations, setReservations] = useState([]);
  // 로딩 상태입니다.
  const [loading, setLoading] = useState(false);
  // 에러 상태입니다.
  const [error, setError] = useState('');
  // 리뷰 작성 임시 상태입니다.
  const [reviewDrafts, setReviewDrafts] = useState({});

  const loadedReservationsRef = useRef({ email: null, role: null });

  const formatLocalDateTime = (d) => {
    const pad2 = (n) => String(n).padStart(2, '0');
    const yyyy = d.getFullYear();
    const mm = pad2(d.getMonth() + 1);
    const dd = pad2(d.getDate());
    const hh = pad2(d.getHours());
    const min = pad2(d.getMinutes());
    return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
  };

  const parseTimeToHHmm = (value) => {
    if (!value) return null;
    const s = String(value);
    const parts = s.split(':');
    if (parts.length < 2) return null;
    const hh = Number(parts[0]);
    const min = Number(parts[1]);
    if (Number.isNaN(hh) || Number.isNaN(min)) return null;
    return { hh, min };
  };

  // 선택된 시터가 바뀌면 예약 폼에 반영합니다.
  useEffect(() => {
    // 선택된 시터가 없으면 종료합니다.
    if (!pickedSitter) return;
  }, [pickedSitter]);

  // 시터의 가능 시간으로 예약 시작/종료를 추천합니다(폼이 비어있을 때만).
  useEffect(() => {
    if (role !== 'PARENT') return;
    if (!pickedSitter) return;
    if (startAt || endAt) return;

    const win = getSuggestedReservationWindow();
    if (!win?.startTime || !win?.endTime) return;

    const startHm = parseTimeToHHmm(win.startTime);
    const endHm = parseTimeToHHmm(win.endTime);
    if (!startHm || !endHm) return;

    const now = new Date();
    let start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), startHm.hh, startHm.min, 0, 0);
    let end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), endHm.hh, endHm.min, 0, 0);
    if (end <= start) {
      end = new Date(end);
      end.setDate(end.getDate() + 1);
    }

    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end <= start) return;

    setStartAt(formatLocalDateTime(start));
    setEndAt(formatLocalDateTime(end));
    clearSuggestedReservationWindow();
  }, [role, pickedSitter, startAt, endAt]);

  // 내 예약 목록을 불러옵니다.
  const loadMyReservations = async () => {
    // 메시지를 초기화합니다.
    setError('');
    // 로딩을 시작합니다.
    setLoading(true);
    try {
      // 내 예약 목록 API를 호출합니다.
      const res = await getMyReservations();
      // 결과를 저장합니다.
      setReservations(res || []);
      // 토스트를 표시합니다.
      onToast?.({ type: 'success', title: '조회 완료', message: `예약 ${(res || []).length}건을 불러왔습니다.` });
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '예약 목록 조회 중 오류가 발생했습니다.';
      setError(msg);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '조회 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  const parseLocalDateTime = (value) => {
    if (!value) return null;
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return null;
    return d;
  };

  const canWriteReview = (reservation) => {
    if (role !== 'PARENT') return false;
    if (reservation?.status !== 'ACCEPTED') return false;
    if (reservation?.reviewed) return false;
    const end = parseLocalDateTime(reservation?.endAt);
    if (!end) return false;
    return end.getTime() < Date.now();
  };

  const onReviewFieldChange = (reservationId, field, value) => {
    setReviewDrafts((prev) => ({
      ...prev,
      [reservationId]: {
        rating: prev?.[reservationId]?.rating ?? '5',
        comment: prev?.[reservationId]?.comment ?? '',
        [field]: value
      }
    }));
  };

  const onCreateReview = async (reservation) => {
    const draft = reviewDrafts?.[reservation.reservationId] || { rating: '5', comment: '' };
    const rating = Number(draft.rating || 0);
    if (Number.isNaN(rating) || rating < 1 || rating > 5) {
      setError('별점은 1~5 사이로 입력해주세요.');
      return;
    }

    setError('');
    setLoading(true);
    try {
      await createReview({
        reservationId: reservation.reservationId,
        rating,
        comment: draft.comment || null
      });
      onToast?.({ type: 'success', title: '리뷰 작성', message: `예약(ID ${reservation.reservationId}) 리뷰가 저장되었습니다.` });
      await loadMyReservations();
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '리뷰 작성 중 오류가 발생했습니다.';
      setError(msg);
      onToast?.({ type: 'error', title: '리뷰 작성 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const canRequest = useMemo(() => {
    if (role !== 'PARENT') return false;
    const sitterId = Number(selectedSitterId);
    if (selectedSitterId == null || Number.isNaN(sitterId) || sitterId <= 0) return false;
    const start = parseLocalDateTime(startAt);
    const end = parseLocalDateTime(endAt);
    if (!start || !end) return false;
    if (end <= start) return false;
    if (note && note.length > 500) return false;
    return true;
  }, [role, selectedSitterId, startAt, endAt, note]);

  // 로그인/역할이 바뀌면 내 예약 목록을 자동으로 불러옵니다.
  useEffect(() => {
    if (!email) return;
    const shouldReload = loadedReservationsRef.current.email !== email || loadedReservationsRef.current.role !== role;
    if (!shouldReload) return;

    loadedReservationsRef.current = { email, role };
    loadMyReservations();
    // pickedSitter는 목록 조회와 무관하므로 제외합니다.
  }, [email, role]);

  // SSE 등 실시간 알림 시 목록만 조용히 갱신합니다.
  useEffect(() => {
    if (!email) return;
    const onRemoteChange = async () => {
      try {
        const res = await getMyReservations();
        setReservations(res || []);
      } catch {
        /* 백그라운드 갱신 실패는 무시 */
      }
    };
    window.addEventListener('basisi:reservations-changed', onRemoteChange);
    return () => window.removeEventListener('basisi:reservations-changed', onRemoteChange);
  }, [email]);

  // 예약을 신청합니다. (부모 전용)
  const onRequest = async () => {
    setError('');

    // 입력값 검증(백엔드 검증 + UX 개선)
    if (!canRequest) {
      setError('시터를 먼저 선택해주세요.');
      return;
    }
    if (!startAt || !endAt) {
      setError('시작 시각과 종료 시각을 모두 입력해주세요.');
      return;
    }

    const start = new Date(startAt);
    const end = new Date(endAt);
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
      setError('날짜/시간 형식이 올바르지 않습니다.');
      return;
    }
    if (end <= start) {
      setError('종료 시각은 시작 시각보다 이후여야 합니다.');
      return;
    }

    if (note && note.length > 500) {
      setError('요청 메모는 500자 이하여야 합니다.');
      return;
    }

    // 로딩을 시작합니다.
    setLoading(true);
    try {
      const sitterId = Number(selectedSitterId);
      if (Number.isNaN(sitterId) || sitterId <= 0) {
        setError('선택된 시터 정보가 올바르지 않습니다. 다시 선택해주세요.');
        return;
      }
      // datetime-local 값은 "YYYY-MM-DDTHH:mm" 형태이므로 그대로 전송합니다.
      const payload = {
        sitterProfileId: sitterId,
        startAt: startAt,
        endAt: endAt,
        note: note
      };
      // 예약 신청 API를 호출합니다.
      const res = await requestReservation(payload);
      // 토스트를 표시합니다.
      onToast?.({ type: 'success', title: '예약 신청', message: `예약 신청이 완료되었습니다. (ID ${res.reservationId})` });
      // 목록을 다시 로드합니다.
      await loadMyReservations();
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '예약 신청 중 오류가 발생했습니다.';
      setError(msg);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '예약 신청 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  // 시터가 예약을 수락합니다.
  const onAccept = async (reservationId) => {
    if (!window.confirm(`예약(ID ${reservationId})을 수락할까요?`)) return;
    // 로딩을 시작합니다.
    setLoading(true);
    try {
      // 수락 API를 호출합니다.
      await acceptReservation(reservationId);
      // 토스트를 표시합니다.
      onToast?.({ type: 'success', title: '수락', message: `예약(ID ${reservationId})을 수락했습니다.` });
      // 목록을 다시 로드합니다.
      await loadMyReservations();
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '예약 수락 중 오류가 발생했습니다.';
      setError(msg);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '수락 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  // 시터가 예약을 거절합니다.
  const onReject = async (reservationId) => {
    if (!window.confirm(`예약(ID ${reservationId})을 거절할까요?`)) return;
    // 로딩을 시작합니다.
    setLoading(true);
    try {
      // 거절 API를 호출합니다.
      await rejectReservation(reservationId);
      // 토스트를 표시합니다.
      onToast?.({ type: 'success', title: '거절', message: `예약(ID ${reservationId})을 거절했습니다.` });
      // 목록을 다시 로드합니다.
      await loadMyReservations();
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '예약 거절 중 오류가 발생했습니다.';
      setError(msg);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '거절 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  // 부모가 예약을 취소합니다.
  const onCancel = async (reservationId) => {
    if (!window.confirm(`예약(ID ${reservationId})을 취소할까요?`)) return;
    // 로딩을 시작합니다.
    setLoading(true);
    try {
      // 취소 API를 호출합니다.
      await cancelReservation(reservationId);
      // 토스트를 표시합니다.
      onToast?.({ type: 'success', title: '취소', message: `예약(ID ${reservationId})을 취소했습니다.` });
      // 목록을 다시 로드합니다.
      await loadMyReservations();
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '예약 취소 중 오류가 발생했습니다.';
      setError(msg);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '취소 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  const fmt = (s) => (s ? String(s).replace('T', ' ') : '-');

  return (
    <div className="basisi-reserve">
      <div className="br-shell">
        <div className="br-header">
          <h1 className="br-title">예약</h1>
          <p className="br-subtitle">
            부모는 예약 신청·취소, 시터는 수락·거절을 수행합니다.
            <br />시간 충돌 검증은 백엔드에서 처리됩니다.
          </p>
        </div>

        {!email ? (
          <div className="emptyState">로그인 후 이용할 수 있습니다.</div>
        ) : (
          <>
            <div className="br-info-row">
              <span className="badge is-info">{email}</span>
              <span className="badge">{role || 'ROLE 미지정'}</span>
              <button
                type="button"
                className="btn"
                onClick={loadMyReservations}
                disabled={loading}
              >
                {loading ? '불러오는 중...' : '내 예약 목록 새로고침'}
              </button>
            </div>

            {role === 'PARENT' ? (
              <>
                <div className="divider" />
                <h2 className="br-section-title">예약 신청 (부모)</h2>

                {!pickedSitter ? (
                  <div className="br-hint">
                    시터를 먼저 선택해주세요. <code>시터검색</code> →{' '}
                    <code>상세 보기</code> → <code>예약하기</code>
                  </div>
                ) : (
                  <div className="br-picked">
                    <span aria-hidden>💝</span>
                    선택 시터: <b>{selectedSitterName || '이름 없음'}</b>
                    {selectedSitterId != null ? (
                      <span className="badge">시터 ID {selectedSitterId}</span>
                    ) : null}
                  </div>
                )}

                <div className="br-form">
                  <div className="br-form-row">
                    <div className="field">
                      <label>시작 시각</label>
                      <input
                        type="datetime-local"
                        value={startAt}
                        onChange={(e) => setStartAt(e.target.value)}
                      />
                    </div>
                    <div className="field">
                      <label>종료 시각</label>
                      <input
                        type="datetime-local"
                        value={endAt}
                        onChange={(e) => setEndAt(e.target.value)}
                      />
                    </div>
                  </div>

                  <TextInput
                    label="요청 메모"
                    value={note}
                    onChange={setNote}
                    placeholder="요청사항을 적어주세요"
                  />

                  <div className="br-form-cta">
                    <button
                      type="button"
                      className="btn primary lg"
                      onClick={onRequest}
                      disabled={loading || !canRequest}
                    >
                      {loading ? '신청중...' : '🗓️ 예약 신청'}
                    </button>
                  </div>
                </div>
              </>
            ) : null}

            {error ? <div className="error">{error}</div> : null}

            <div className="divider" />
            <h2 className="br-section-title">내 예약 목록</h2>

            {loading && reservations.length === 0 ? (
              <div className="br-list">
                <div className="skeletonCard">
                  <div className="skeletonLine" style={{ width: '45%', marginBottom: 10 }} />
                  <div className="skeletonLine" style={{ width: '70%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '85%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '55%' }} />
                </div>
                <div className="skeletonCard">
                  <div className="skeletonLine" style={{ width: '55%', marginBottom: 10 }} />
                  <div className="skeletonLine" style={{ width: '80%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '60%' }} />
                </div>
              </div>
            ) : reservations.length === 0 ? (
              <div className="emptyState">
                아직 예약이 없습니다. 먼저 시터를 선택하고 <b>예약 신청</b>을 진행해보세요.
              </div>
            ) : (
              <div className="br-list">
                {reservations.map((r) => {
                  const statusClass = STATUS_BADGE_CLASS[r.status] || '';
                  const statusKr = STATUS_KOREAN[r.status] || r.status;
                  return (
                    <div key={r.reservationId} className="br-card">
                      <div className="br-card-head">
                        <div className="br-card-title">
                          예약 ID: {r.reservationId}
                          <span className={`badge ${statusClass}`}>{statusKr}</span>
                          {r.reviewed ? (
                            <span className="badge is-info">리뷰 작성완료</span>
                          ) : null}
                        </div>
                        <div className="row">
                          <span className="badge">시터 ID {r.sitterProfileId}</span>
                          <span className="badge">
                            {fmt(r.startAt)} ~ {fmt(r.endAt)}
                          </span>
                        </div>
                      </div>

                      <div className="br-card-meta">
                        부모: <b>{r.parentName}</b> · 시터: <b>{r.sitterName}</b>
                      </div>

                      {r.note ? (
                        <div className="br-card-note">메모: {r.note}</div>
                      ) : null}

                      <div className="br-card-actions">
                        {role === 'SITTER' && r.status === 'REQUESTED' ? (
                          <>
                            <button
                              type="button"
                              className="btn primary"
                              onClick={() => onAccept(r.reservationId)}
                              disabled={loading}
                            >
                              {loading ? '처리중...' : '수락'}
                            </button>
                            <button
                              type="button"
                              className="btn danger"
                              onClick={() => onReject(r.reservationId)}
                              disabled={loading}
                            >
                              {loading ? '처리중...' : '거절'}
                            </button>
                          </>
                        ) : null}
                        {role === 'PARENT' &&
                        (r.status === 'REQUESTED' || r.status === 'ACCEPTED') ? (
                          <button
                            type="button"
                            className="btn danger"
                            onClick={() => onCancel(r.reservationId)}
                            disabled={loading}
                          >
                            {loading ? '처리중...' : '취소'}
                          </button>
                        ) : null}
                      </div>

                      {canWriteReview(r) ? (
                        <div className="br-review">
                          <h3 className="br-review-title">리뷰 작성</h3>
                          <div className="br-review-row">
                            <div className="field br-review-rating">
                              <label>별점 (1~5)</label>
                              <input
                                type="number"
                                min="1"
                                max="5"
                                step="1"
                                value={
                                  reviewDrafts?.[r.reservationId]?.rating ?? '5'
                                }
                                onChange={(e) =>
                                  onReviewFieldChange(
                                    r.reservationId,
                                    'rating',
                                    e.target.value,
                                  )
                                }
                              />
                            </div>
                            <div className="field br-review-comment">
                              <label>코멘트 (선택)</label>
                              <input
                                type="text"
                                maxLength="1000"
                                value={
                                  reviewDrafts?.[r.reservationId]?.comment ?? ''
                                }
                                onChange={(e) =>
                                  onReviewFieldChange(
                                    r.reservationId,
                                    'comment',
                                    e.target.value,
                                  )
                                }
                                placeholder="시터 이용 후기를 작성해주세요."
                              />
                            </div>
                          </div>
                          <div className="br-review-cta">
                            <button
                              type="button"
                              className="btn primary"
                              onClick={() => onCreateReview(r)}
                              disabled={loading}
                            >
                              {loading ? '저장중...' : '리뷰 저장'}
                            </button>
                          </div>
                        </div>
                      ) : null}
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

