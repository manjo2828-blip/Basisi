import React from 'react';

import './SitterResultCard.css';

function genderLabel(g) {
  if (g === 'MALE') return '남';
  if (g === 'FEMALE') return '여';
  return '-';
}

export function SitterResultCard({
  sitter,
  rank,
  matchScore,
  reasons,
  onPickSitter,
  onViewDetail,
  showRank = false,
  showReasons = false,
}) {
  const r = sitter || {};
  const hasRank = showRank && rank != null;
  const hasMatch = matchScore != null;
  const hasReasons = showReasons && Array.isArray(reasons) && reasons.length > 0;
  const bio = (r.bio || '').trim();

  return (
    <article className="basisi-result-card">
      {hasRank || hasMatch ? (
        <div className="brc-rank-row">
          {hasRank ? <span className="brc-rank-badge">{rank}위</span> : null}
          {hasMatch ? <span className="brc-match-badge">매칭 {matchScore}%</span> : null}
        </div>
      ) : null}

      {hasReasons ? (
        <ul className="brc-reasons">
          {reasons.map((line, i) => (
            <li key={i}>{line}</li>
          ))}
        </ul>
      ) : null}

      <div className="brc-head">
        <div className="brc-name">
          <span>{r.name || '이름 없음'}</span>
          <span className="brc-badge is-id">ID: {r.sitterProfileId ?? '-'}</span>
          <span className="brc-badge is-score">🔥 {r.flameScore ?? 0} ({r.flameGrade ?? 'NEW'})</span>
        </div>

        <div className="brc-meta">
          <span className="brc-badge">성별 {genderLabel(r.gender)}</span>
          <span className="brc-badge">나이 {r.age ?? '-'}</span>
          <span className="brc-badge">경력 {r.yearsOfExperience ?? 0}년</span>
          <span className="brc-badge">{r.hasCertificate ? '자격증 유' : '자격증 무'}</span>
          <span className="brc-badge">{r.region || '지역 미입력'}</span>
        </div>
      </div>

      <p className={`brc-bio ${bio ? '' : 'is-empty'}`}>{bio || '소개 없음'}</p>

      <div className="brc-actions">
        <button
          type="button"
          className="brc-btn is-primary"
          onClick={() => onPickSitter?.(r)}
        >
          이 시터로 예약하기
        </button>
        <button
          type="button"
          className="brc-btn"
          onClick={() => onViewDetail?.(r.sitterProfileId)}
        >
          상세 보기
        </button>
      </div>
    </article>
  );
}
