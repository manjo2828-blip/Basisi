import React from 'react';

export function SitterResultCard({
  sitter,
  rank,
  matchScore,
  reasons,
  onPickSitter,
  onViewDetail,
  showRank = false,
  showReasons = false
}) {
  const r = sitter || {};
  return (
    <div
      className="sitterResultCard"
      style={{
        border: '1px solid rgba(26,21,35,0.12)',
        borderRadius: 14,
        padding: 12,
        background: 'rgba(26,21,35,0.04)'
      }}
    >
      {showRank && rank != null ? (
        <div style={{ marginBottom: 8 }}>
          <span className="badge" style={{ marginRight: 8 }}>
            {rank}위
          </span>
          {matchScore != null ? <span className="badge">매칭 {matchScore}%</span> : null}
        </div>
      ) : null}

      {showReasons && Array.isArray(reasons) && reasons.length > 0 ? (
        <ul style={{ margin: '0 0 10px 0', paddingLeft: 18, fontSize: 12, color: 'rgba(26,21,35,0.72)' }}>
          {reasons.map((line, i) => (
            <li key={i}>{line}</li>
          ))}
        </ul>
      ) : null}

      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}>
        <div style={{ fontWeight: 800 }}>
          {r.name}{' '}
          <span className="badge">ID: {r.sitterProfileId}</span>
          <span className="badge" style={{ marginLeft: 8 }}>
            🔥 {r.flameScore ?? 0} ({r.flameGrade ?? 'NEW'})
          </span>
        </div>
        <div className="row" style={{ alignItems: 'center' }}>
          <span className="badge">성별 {r.gender === 'MALE' ? '남' : r.gender === 'FEMALE' ? '여' : '-'}</span>
          <span className="badge">나이 {r.age ?? '-'}</span>
          <span className="badge">경력 {r.yearsOfExperience ?? 0}년</span>
          <span className="badge">{r.hasCertificate ? '자격증 유' : '자격증 무'}</span>
          <span className="badge">{r.region ?? '지역 미입력'}</span>
        </div>
      </div>
      <div style={{ marginTop: 6, fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>{r.bio || '소개 없음'}</div>
      <div className="row" style={{ marginTop: 10 }}>
        <button type="button" className="btn accent" onClick={() => onPickSitter?.(r)}>
          이 시터로 예약하기
        </button>
        <button type="button" className="btn" onClick={() => onViewDetail?.(r.sitterProfileId)}>
          상세 보기
        </button>
      </div>
    </div>
  );
}
