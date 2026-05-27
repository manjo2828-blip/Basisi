import React from 'react';
import { getSitterProfileImageUrl } from '../lib/profileApi.js';
import { SITTER_AGE_OPTIONS } from '../data/sitterAgeOptions.js';
import { ProfilePhotoFrame } from './ProfilePhotoFrame.jsx';

const GENDER_LABEL = { MALE: '남', FEMALE: '여' };
const NATIONALITY_LABEL = { KOREAN: '내국인', FOREIGNER: '외국인' };

function formatWage(wage, negotiable) {
  if (negotiable && (!wage || Number(wage) < 1)) return '협의 가능';
  if (negotiable && wage) return `${Number(wage).toLocaleString('ko-KR')}원/시간 (협의 가능)`;
  if (wage) return `${Number(wage).toLocaleString('ko-KR')}원/시간`;
  return '미입력';
}

function formatRegions(regions) {
  if (!Array.isArray(regions) || regions.length === 0) return '미입력';
  return regions
    .filter((r) => (r.sido || '').trim() && (r.sigungu || '').trim() && (r.dong || '').trim())
    .map((r) => [r.sido, r.sigungu, r.dong].filter(Boolean).join(' '))
    .join(' · ');
}

function formatAgeGroups(ids) {
  if (!Array.isArray(ids) || ids.length === 0) return '미입력';
  const map = Object.fromEntries(SITTER_AGE_OPTIONS.map((a) => [a.id, a.title]));
  return ids.map((id) => map[id] || id).join(', ');
}

/**
 * 시터 본인·부모에게 보여줄 프로필 카드 미리보기입니다.
 */
export function SitterProfilePreviewCard({
  displayName,
  phone,
  age,
  gender,
  years,
  hasCert,
  region,
  bio,
  nationality,
  activities,
  wage,
  hourlyNegotiable,
  cctv,
  preferredRegions,
  ageGroups,
  photoIds,
  flameScore,
  flameGrade
}) {
  const photos = Array.isArray(photoIds) ? photoIds : [];
  const mainPhotoId = photos[0] || null;
  const name = (displayName || '').trim() || '시터';

  return (
    <div
      style={{
        border: '1px solid rgba(199, 61, 106, 0.22)',
        borderRadius: 16,
        overflow: 'hidden',
        background: 'linear-gradient(135deg, rgba(255, 246, 250, 0.95) 0%, rgba(255, 252, 246, 0.95) 100%)'
      }}
    >
      <div style={{ display: 'flex', gap: 16, padding: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <ProfilePhotoFrame src={mainPhotoId ? getSitterProfileImageUrl(mainPhotoId) : null} alt={`${name} 프로필`} size="hero" border="accent">
          <span style={{ fontSize: 12, color: 'rgba(26,21,35,0.45)', textAlign: 'center', padding: 8 }}>
            프로필 사진
            <br />
            없음
          </span>
        </ProfilePhotoFrame>

        <div style={{ flex: 1, minWidth: 200 }}>
          <div style={{ fontWeight: 900, fontSize: 20, marginBottom: 6 }}>{name}</div>
          <div className="row" style={{ flexWrap: 'wrap', gap: 6, marginBottom: 8 }}>
            {flameScore != null ? (
              <span className="badge">
                🔥 {flameScore} ({flameGrade || 'NEW'})
              </span>
            ) : null}
            <span className="badge">성별 {GENDER_LABEL[gender] || '-'}</span>
            <span className="badge">나이 {age || '-'}</span>
            <span className="badge">경력 {years ?? 0}년</span>
            <span className="badge">{hasCert === 'YES' || hasCert === true ? '자격증 유' : '자격증 무'}</span>
          </div>
          <div style={{ fontSize: 13, color: 'rgba(26,21,35,0.68)' }}>{region?.trim() || '거주 지역 미입력'}</div>
          {phone?.trim() ? (
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.52)', marginTop: 4 }}>연락처 {phone}</div>
          ) : null}
        </div>
      </div>

      {photos.length > 1 ? (
        <div style={{ padding: '0 16px 12px', display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          {photos.slice(1).map((id) => (
            <ProfilePhotoFrame key={id} src={getSitterProfileImageUrl(id)} alt="추가 사진" size="sm" />
          ))}
        </div>
      ) : null}

      <div style={{ padding: '0 16px 16px', display: 'grid', gap: 10 }}>
        <div style={{ fontSize: 13, lineHeight: 1.55, color: 'rgba(26,21,35,0.78)' }}>
          {bio?.trim() || '자기소개가 아직 없습니다. 「자기소개」 탭에서 작성해주세요.'}
        </div>

        <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.62)' }}>
          <div>
            <strong>국적</strong> {NATIONALITY_LABEL[nationality] || '미입력'}
          </div>
          <div style={{ marginTop: 4 }}>
            <strong>희망 시급</strong> {formatWage(wage, hourlyNegotiable)}
          </div>
          <div style={{ marginTop: 4 }}>
            <strong>CCTV</strong>{' '}
            {cctv === 'OK' ? '동의' : cctv === 'NO' ? '비동의' : '미입력'}
          </div>
          <div style={{ marginTop: 4 }}>
            <strong>활동 희망 지역</strong> {formatRegions(preferredRegions)}
          </div>
          <div style={{ marginTop: 4 }}>
            <strong>선호 연령</strong> {formatAgeGroups(ageGroups)}
          </div>
        </div>

        {Array.isArray(activities) && activities.length > 0 ? (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {activities.map((k) => (
              <span key={k} className="badge" style={{ fontSize: 11 }}>
                {k}
              </span>
            ))}
          </div>
        ) : (
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.48)' }}>가능 활동이 아직 선택되지 않았습니다.</div>
        )}
      </div>
    </div>
  );
}
