import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ApiError } from '../lib/api.js';
import { getPublicSitterDetail } from '../lib/sitterApi.js';
import { getSitterReviews, getSitterReviewSummary } from '../lib/reviewApi.js';
import { getAccessToken } from '../lib/storage.js';

export function SitterDetailPage({ onPickSitter, onToast }) {
  const navigate = useNavigate();
  const { sitterProfileId } = useParams();

  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [reviewSummary, setReviewSummary] = useState(null);
  const [reviews, setReviews] = useState([]);

  const canReserve = Boolean(getAccessToken());

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      setDetail(null);
      setReviewSummary(null);
      setReviews([]);
      try {
        const [detailRes, summaryRes, reviewsRes] = await Promise.all([
          getPublicSitterDetail(Number(sitterProfileId)),
          getSitterReviewSummary(Number(sitterProfileId)),
          getSitterReviews(Number(sitterProfileId))
        ]);
        if (!cancelled) {
          setDetail(detailRes);
          setReviewSummary(summaryRes);
          setReviews(reviewsRes || []);
        }
      } catch (e) {
        if (cancelled) return;
        const msg = e instanceof ApiError ? e.message : '시터 상세 조회 중 오류가 발생했습니다.';
        setError(msg);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [sitterProfileId]);

  const onReserve = () => {
    if (!detail) return;
    onToast?.({
      type: 'success',
      title: '선택됨',
      message: `시터(ID ${detail.sitterProfileId})를 예약 대상으로 선택했습니다.`
    });
    // ReservationPanel에서 sitterProfileId만 쓰므로, detail 전체를 넘겨도 안전합니다.
    onPickSitter?.(detail);
  };

  return (
    <div>
      <div style={{ fontWeight: 900, marginBottom: 10 }}>시터 상세</div>

      <div className="row" style={{ alignItems: 'center', marginBottom: 12 }}>
        <button className="btn" onClick={() => navigate('/search')} disabled={loading}>
          ← 검색으로
        </button>

        <button className="btn accent" onClick={onReserve} disabled={loading || !detail || !canReserve}>
          {canReserve ? '예약하기' : '로그인 후 예약'}
        </button>
      </div>

      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div className="skeletonCard">
            <div className="skeletonLine" style={{ width: '55%', marginBottom: 10 }} />
            <div className="skeletonLine" style={{ width: '80%', marginBottom: 8 }} />
            <div className="skeletonLine" style={{ width: '70%', marginBottom: 8 }} />
            <div className="skeletonLine" style={{ width: '60%' }} />
          </div>
        </div>
      ) : null}
      {error ? <div className="error">{error}</div> : null}

      {detail ? (
        <>
          <div className="divider" />

          <div style={{ fontWeight: 1000, fontSize: 18, marginBottom: 8 }}>
            {detail.name} <span className="badge" style={{ marginLeft: 8 }}>ID: {detail.sitterProfileId}</span>
          </div>

          <div className="row" style={{ marginBottom: 10 }}>
            <span className="badge">성별 {detail.gender === 'MALE' ? '남' : detail.gender === 'FEMALE' ? '여' : '-'}</span>
            <span className="badge">나이 {detail.age ?? '-'}</span>
            <span className="badge">경력 {detail.yearsOfExperience ?? 0}년</span>
            <span className="badge">{detail.hasCertificate ? '자격증 유' : '자격증 무'}</span>
            <span className="badge">{detail.region ?? '지역 미입력'}</span>
            <span className="badge">평점 {reviewSummary?.averageRating?.toFixed?.(1) ?? '0.0'}점</span>
            <span className="badge">리뷰 {reviewSummary?.reviewCount ?? 0}건</span>
            <span className="badge" title="평점/완료예약/최근활동/응답속도 기반">
              🔥 {detail.flameScore ?? 0} ({detail.flameGrade ?? 'NEW'})
            </span>
          </div>

          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 10 }}>
            점수 근거: 완료예약 {detail.completedReservationCount ?? 0}건 · 평균평점 {Number(detail.averageRating ?? 0).toFixed(1)} ·
            최근활동 {detail.recentActivityCount ?? 0}회 · 평균응답 {detail.averageResponseMinutes != null ? `${Number(detail.averageResponseMinutes).toFixed(1)}분` : '기록 없음'} ·
            이번 주 {detail.weeklyDelta != null ? `${detail.weeklyDelta >= 0 ? '+' : ''}${detail.weeklyDelta}` : '+0'}
          </div>

          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 14 }}>
            {detail.bio || '소개 없음'}
          </div>

          <div className="divider" />
          <div style={{ fontWeight: 900, marginBottom: 8 }}>리뷰</div>
          {reviews.length === 0 ? (
            <div className="emptyState">아직 등록된 리뷰가 없습니다.</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {reviews.map((r) => (
                <div
                  key={r.reviewId}
                  style={{
                    border: '1px solid rgba(26,21,35,0.12)',
                    borderRadius: 12,
                    padding: 10,
                    background: 'rgba(26,21,35,0.03)'
                  }}
                >
                  <div className="row" style={{ alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ fontWeight: 800 }}>{r.parentName}</div>
                    <span className="badge">별점 {r.rating}/5</span>
                  </div>
                  <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginTop: 4 }}>
                    {r.comment || '코멘트 없음'}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      ) : null}
    </div>
  );
}

