import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const out = path.join(__dirname, '../src/pages/AiSitterRecommendPanel.jsx');

const content = `// AI 맞춤 시터 추천 패널
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { SitterResultCard } from '../components/SitterResultCard.jsx';
import { ApiError } from '../lib/api.js';
import { getMyParentProfile } from '../lib/profileApi.js';
import { postSitterRecommend } from '../lib/recommendApi.js';

const QUICK_CHIPS = ['주말만 가능', '영어 놀이 원해요', 'CCTV 동의', '경력 3년 이상', '여성 시터 선호'];

const WORK_LABEL = {
  DUAL_INCOME: '맞벌이',
  HOMEMAKER: '전업 주부·주부'
};

const SCHEDULE_LABEL = {
  REGULAR: '정기적인 일정',
  SPECIFIC: '특정일 일정',
  UNDECIDED: '일정 미정'
};

function formatRegion(profile) {
  if (!profile) return null;
  if (profile.region && String(profile.region).trim()) return String(profile.region).trim();
  const parts = [profile.regionSido, profile.regionSigungu, profile.regionDong].filter((x) => x && String(x).trim());
  return parts.length ? parts.join(' ') : null;
}

function formatChildLine(child) {
  if (!child) return '';
  const g = child.gender === 'MALE' ? '남자' : child.gender === 'FEMALE' ? '여자' : '';
  const bd = child.birthDate ? String(child.birthDate).trim() : '';
  if (!bd) return '';
  return g ? \`\${bd} · \${g} 아이\` : bd;
}

function profileCompleteness(profile) {
  if (!profile) {
    return { percent: 0, missing: ['프로필 없음'], isEmpty: true };
  }
  const checks = [
    { key: 'region', ok: !!formatRegion(profile), label: '방문 지역' },
    {
      key: 'children',
      ok: Array.isArray(profile.children) && profile.children.some((c) => (c.birthDate || '').trim()),
      label: '아이 정보'
    },
    {
      key: 'keywords',
      ok: Array.isArray(profile.expectationKeywords) && profile.expectationKeywords.length > 0,
      label: '맘시터 활동 키워드'
    },
    { key: 'schedule', ok: !!profile.scheduleType, label: '필요 일정' },
    { key: 'work', ok: !!profile.parentWorkType, label: '부모 가구 형태' }
  ];
  const done = checks.filter((c) => c.ok).length;
  const missing = checks.filter((c) => !c.ok).map((c) => c.label);
  return {
    percent: Math.round((done / checks.length) * 100),
    missing,
    isEmpty: done === 0
  };
}

export function AiSitterRecommendPanel({ session, onToast, onPickSitter, onViewDetail }) {
  const email = useMemo(() => session?.email || null, [session]);
  const role = useMemo(() => session?.role || null, [session]);

  const [useMyProfile, setUseMyProfile] = useState(true);
  const [profile, setProfile] = useState(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState('');

  const [additionalRequest, setAdditionalRequest] = useState('');
  const [limit, setLimit] = useState(5);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  const completeness = useMemo(() => profileCompleteness(profile), [profile]);
  const regionText = useMemo(() => formatRegion(profile), [profile]);
  const childLines = useMemo(() => {
    const list = Array.isArray(profile?.children) ? profile.children : [];
    return list.map(formatChildLine).filter(Boolean);
  }, [profile]);

  const loadProfile = useCallback(async () => {
    setProfileError('');
    setProfileLoading(true);
    try {
      const res = await getMyParentProfile();
      setProfile(res || null);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '부모 프로필을 불러오지 못했습니다.';
      setProfile(null);
      setProfileError(msg);
    } finally {
      setProfileLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!email || role !== 'PARENT') return;
    loadProfile();
  }, [email, role, loadProfile]);

  const appendChip = (text) => {
    setAdditionalRequest((prev) => {
      const trimmed = prev.trim();
      const next = trimmed ? \`\${trimmed} \${text}\` : text;
      return next.length > 500 ? next.slice(0, 500) : next;
    });
  };

  const canRecommend =
    !loading &&
    (useMyProfile || additionalRequest.trim().length > 0 || (!completeness.isEmpty && completeness.percent > 0));

  const onRecommend = async () => {
    setError('');
    setLoading(true);
    setResult(null);
    try {
      const res = await postSitterRecommend({
        useMyProfile,
        additionalRequest: additionalRequest.trim() || undefined,
        limit: Number(limit) || 5
      });
      setResult(res || null);
      const count = (res?.items || []).length;
      onToast?.({
        type: 'success',
        title: '추천 완료',
        message: count > 0 ? \`\${count}명의 시터를 추천했습니다.\` : '조건에 맞는 시터가 없습니다.'
      });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : 'AI 추천 중 오류가 발생했습니다.';
      setError(msg);
      onToast?.({ type: 'error', title: '추천 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const items = result?.items || [];

  return (
    <div>
      <motionless>PLACEHOLDER_ROOT</motionless>
    </div>
  );
}
`;

// The template literal above is incomplete - write full file directly
fs.writeFileSync(out, content.replace('<motionless>PLACEHOLDER_ROOT</motionless>', 'INCOMPLETE'), 'utf8');
