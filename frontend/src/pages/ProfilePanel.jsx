// 프로필 CRUD UI를 제공하는 패널입니다.
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMySitterScore } from '../lib/scoreApi.js';
import { ParentProfileEditor } from './ParentProfileEditor.jsx';
import { SitterProfileEditor } from './SitterProfileEditor.jsx';

// 프로필 패널을 렌더링합니다.
export function ProfilePanel({ session, onAuthChanged, onToast }) {
  const navigate = useNavigate();
  // 현재 세션에서 역할 정보를 읽어옵니다.
  const role = useMemo(() => session?.role || null, [session]);
  // 현재 세션에서 이메일 정보를 읽어옵니다.
  const email = useMemo(() => session?.email || null, [session]);

  const [activeTab, setActiveTab] = useState(() => role || 'PARENT');
  const [mySitterScore, setMySitterScore] = useState(null);
  const [sitterSaveNonce, setSitterSaveNonce] = useState(0);

  useEffect(() => {
    if (!role) return;
    setActiveTab(role);
  }, [role]);

  useEffect(() => {
    if (!email || role !== 'SITTER') return;
    const loadMyScore = async () => {
      try {
        const res = await getMySitterScore();
        setMySitterScore(res || null);
      } catch (e) {
        setMySitterScore(null);
      }
    };
    loadMyScore();
  }, [email, role, sitterSaveNonce]);

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 10 }}>마이 프로필</div>

      <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
        - 역할(Role)에 따라 부모/시터 프로필이 자동으로 분기됩니다.
      </div>

      <div className="row" style={{ marginBottom: 12 }}>
        <button
          className={`btn ${activeTab === 'PARENT' ? 'primary' : ''}`}
          onClick={() => setActiveTab('PARENT')}
          disabled={role !== 'PARENT'}
        >
          부모 프로필
        </button>
        <button
          className={`btn ${activeTab === 'SITTER' ? 'primary' : ''}`}
          onClick={() => setActiveTab('SITTER')}
          disabled={role !== 'SITTER'}
        >
          시터 프로필
        </button>
        <button className="btn accent" onClick={() => navigate('/reservations')} disabled={!email}>
          내 예약/요청
        </button>
      </div>

      <div className="divider" />

      {!email ? (
        <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>
          아직 로그인 상태가 아닙니다. 왼쪽에서 로그인/회원가입 후 다시 시도해주세요.
        </div>
      ) : role === 'PARENT' ? (
        <ParentProfileEditor onToast={onToast} onAuthChanged={onAuthChanged} />
      ) : role === 'SITTER' ? (
        <>
          <div
            style={{
              border: '1px solid rgba(199, 61, 106, 0.22)',
              background: 'rgba(255, 112, 166, 0.06)',
              borderRadius: 12,
              padding: 12,
              marginBottom: 12
            }}
          >
            <div style={{ fontWeight: 900, marginBottom: 6 }}>내 불꽃 점수</div>
            <div className="row" style={{ alignItems: 'center', marginBottom: 6 }}>
              <span className="badge">🔥 {mySitterScore?.score ?? 0}</span>
              <span className="badge">등급 {mySitterScore?.grade ?? 'NEW'}</span>
              <span className="badge">이번 주 {mySitterScore?.weeklyDelta != null ? `${mySitterScore.weeklyDelta >= 0 ? '+' : ''}${mySitterScore.weeklyDelta}` : '+0'}</span>
            </div>
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>
              완료예약 {mySitterScore?.completedReservationCount ?? 0} · 평균평점 {Number(mySitterScore?.averageRating ?? 0).toFixed(1)} ·
              최근활동 {mySitterScore?.recentActivityCount ?? 0} · 응답점수 {mySitterScore?.responseScore ?? 0}
            </div>
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginTop: 4 }}>
              점수 올리는 법: 예약 완료 누적 + 리뷰 평점 관리 + 최근 30일 활동 + 빠른 요청 응답
            </div>
          </div>
          <SitterProfileEditor
            onToast={onToast}
            onAuthChanged={onAuthChanged}
            onSaved={() => setSitterSaveNonce((n) => n + 1)}
          />
        </>
      ) : (
        <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>
          역할(Role)이 확인되지 않습니다. 다시 로그인해 주세요.
        </div>
      )}

      {role ? (
        <div style={{ marginTop: 12, fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>
          현재 역할(Role): <span className="badge">{role}</span>
        </div>
      ) : null}

      <div className="divider" />
    </div>
  );
}
