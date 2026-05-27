import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Navigate, Route, Routes, Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';

import { AuthPanel } from './pages/AuthPanel.jsx';
import { AccountPanel } from './pages/AccountPanel.jsx';
import { ProfilePanel } from './pages/ProfilePanel.jsx';
import { SitterSearchPanel } from './pages/SitterSearchPanel.jsx';
import { AiSitterRecommendPanel } from './pages/AiSitterRecommendPanel.jsx';
import { ReservationPanel } from './pages/ReservationPanel.jsx';
import { HomePage } from './pages/HomePage.jsx';
import { SitterDetailPage } from './pages/SitterDetailPage.jsx';
import { ServiceIntroPage } from './pages/ServiceIntroPage.jsx';

import { Toast } from './components/Toast.jsx';
import { getApiBase } from './lib/api.js';
import {
  clearAuth,
  clearPickedSitterProfileId,
  clearSuggestedReservationWindow,
  getAccessToken,
  getDecodedTokenPayload,
  getEmail,
  getName,
  getPickedSitterProfileId,
  getRole,
  isAccessTokenValid,
  setPickedSitterProfileId,
} from './lib/storage.js';

function RequireAuth({ isAuthenticated, redirectTo = '/auth', children }) {
  if (!isAuthenticated) return <Navigate to={redirectTo} replace />;
  return children;
}

function PageCard({ children }) {
  return (
    <div className="content">
      <div className="card">{children}</div>
    </div>
  );
}

function AccountMenu({ displayName, onMyPage, onOpenAccount, onLogout }) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    const onDown = (e) => {
      if (!rootRef.current) return;
      if (rootRef.current.contains(e.target)) return;
      setOpen(false);
    };
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, [open]);

  return (
    <div className="accountMenu" ref={rootRef}>
      <button
        type="button"
        className="headerAuthLink accountMenuTrigger"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="menu"
        aria-expanded={open}
      >
        {displayName ? `${displayName}님` : '내 계정'}
      </button>

      {open ? (
        <div className="accountMenuPopover" role="menu">
          <div className="accountMenuHeader">
            <div className="accountMenuAvatar" aria-hidden />
            <div className="accountMenuName">{displayName ? `${displayName}님` : '내 계정'}</div>
          </div>
          <div className="accountMenuDivider" aria-hidden />
          <button
            type="button"
            className="accountMenuItem"
            onClick={() => {
              setOpen(false);
              onMyPage?.();
            }}
            role="menuitem"
          >
            마이페이지
          </button>
          <button
            type="button"
            className="accountMenuItem"
            onClick={() => {
              setOpen(false);
              onOpenAccount?.();
            }}
            role="menuitem"
          >
            회원 정보 수정
          </button>
          <button
            type="button"
            className="accountMenuItem"
            onClick={() => {
              setOpen(false);
              onLogout?.();
            }}
            role="menuitem"
          >
            로그아웃
          </button>
        </div>
      ) : null}
    </div>
  );
}

export function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const authMode = searchParams.get('mode') === 'signup' ? 'signup' : 'login';
  const onAuthPath = location.pathname === '/auth';

  // 렌더링 트리거를 위한 상태입니다.
  const [tick, setTick] = useState(0);
  // 토스트 상태입니다.
  const [toast, setToast] = useState(null);
  // 예약 대상 시터 선택 상태입니다.
  const [pickedSitter, setPickedSitter] = useState(() => {
    const sitterProfileId = getPickedSitterProfileId();
    return sitterProfileId ? { sitterProfileId } : null;
  });

  // 저장된 인증 정보를 가져옵니다.
  const session = useMemo(() => {
    const email = getEmail();
    const role = getRole();
    const rawName = getName();
    const displayName = rawName && String(rawName).trim() ? String(rawName).trim() : null;
    const hasToken = Boolean(getAccessToken());
    const payload = getDecodedTokenPayload();
    const tokenExpMs = payload?.exp ? payload.exp * 1000 : null;
    const tokenExpired = tokenExpMs ? Date.now() >= tokenExpMs : null;
    return { email, role, displayName, hasToken, tokenExpMs, tokenExpired };
  }, [tick]);

  // exp 없음/파싱 실패는 비로그인과 동일(예약 POST 등에서 403 방지).
  const isAuthenticated = isAccessTokenValid();

  const onLogout = () => {
    clearAuth();
    clearPickedSitterProfileId();
    clearSuggestedReservationWindow();
    setPickedSitter(null);
    setToast({ type: 'success', title: '로그아웃', message: '로그아웃 되었습니다.' });
    setTick((v) => v + 1);
    navigate('/');
  };

  const onOpenAccount = () => {
    window.open('/account', '_blank', 'noopener,noreferrer');
  };

  const onAuthChanged = () => {
    setTick((v) => v + 1);
  };

  const notify = (nextToast) => {
    setToast(null);
    setTimeout(() => setToast(nextToast), 0);
  };

  useEffect(() => {
    if (!isAuthenticated) return;
    const token = getAccessToken();
    if (!token || !isAccessTokenValid()) return;

    const url = `${getApiBase()}/notifications/stream?access_token=${encodeURIComponent(token)}`;
    const es = new EventSource(url);

    const onReservation = (ev) => {
      try {
        const data = JSON.parse(ev.data);
        const action = data?.action;
        const reservationId = data?.reservationId;
        const labels = {
          REQUESTED: { title: '예약 알림', message: `새 예약 요청이 도착했습니다. (#${reservationId})` },
          ACCEPTED: { title: '예약 알림', message: `예약이 수락되었습니다. (#${reservationId})` },
          REJECTED: { title: '예약 알림', message: `예약이 거절되었습니다. (#${reservationId})` },
          CANCELLED: { title: '예약 알림', message: `예약이 취소되었습니다. (#${reservationId})` }
        };
        const next = labels[action];
        if (next) {
          setToast(null);
          setTimeout(() => setToast({ type: 'success', ...next }), 0);
        }
        window.dispatchEvent(new CustomEvent('basisi:reservations-changed', { detail: data }));
      } catch {
        /* malformed event */
      }
    };

    es.addEventListener('reservation', onReservation);
    es.onerror = () => {
      es.close();
    };

    return () => {
      es.removeEventListener('reservation', onReservation);
      es.close();
    };
  }, [isAuthenticated, tick]);

  const onPickSitterAndGoToReservation = (sitter) => {
    setPickedSitter(sitter);
    const sitterProfileId = sitter?.sitterProfileId;
    setPickedSitterProfileId(sitterProfileId);
    navigate('/reservations');
  };

  const navItem = (to, label) => {
    const active = location.pathname === to;
    return (
      <Link to={to} className={`navLinkBar ${active ? 'is-active' : ''}`}>
        {label}
      </Link>
    );
  };

  return (
    <div className="siteShell">
      <Toast toast={toast} onClose={() => setToast(null)} />

      <header className="siteHeader">
        <div className="content headerBar">
          <div className="headerBarMain">
            <Link to="/" className="siteBrand" aria-label="Basisi 홈">
              <span className="siteBrandIcon" aria-hidden>
                🐣
              </span>
              <span className="siteBrandWord" translate="no">
                <span className="siteBrandWordBase">BASIS</span>
                <span className="siteBrandWordAccent">I</span>
              </span>
            </Link>

            <nav className="headerNav" aria-label="주요 메뉴">
              {navItem('/', '홈')}
              {navItem('/about', '서비스 소개')}
              {navItem('/search', '시터 검색')}
            </nav>
          </div>

          <div className="headerBarAuth">
            {session.email ? (
              <div className="headerAuthUserRow">
                <AccountMenu
                  displayName={session.displayName}
                  onMyPage={() => navigate('/profile')}
                  onOpenAccount={onOpenAccount}
                  onLogout={onLogout}
                />
              </div>
            ) : (
              <div className="headerAuthGroup" aria-label="인증">
                <Link
                  to="/auth"
                  className={`headerAuthLink ${onAuthPath && authMode === 'login' ? 'is-active' : ''}`}
                >
                  로그인
                </Link>
                <Link
                  to="/auth?mode=signup"
                  className={`headerAuthLink ${onAuthPath && authMode === 'signup' ? 'is-active' : ''}`}
                >
                  회원가입
                </Link>
              </div>
            )}
          </div>
        </div>
      </header>

      {location.pathname !== '/auth' ? (
        <div className="quickDock" aria-label="빠른 이동">
          <Link
            to="/search"
            className={`quickDockItem ${location.pathname === '/search' ? 'is-active' : ''}`}
            aria-label="시터검색"
          >
            <span className="quickDockIcon" aria-hidden>
              🔎
            </span>
            <span className="quickDockLabel">시터검색</span>
          </Link>
        </div>
      ) : null}

      <main className="siteMain">
        <Routes>
          <Route
            path="/"
            element={<HomePage onGoSearch={() => navigate('/search')} />}
          />

          <Route
            path="/auth"
            element={<AuthPanel onAuthChanged={onAuthChanged} onToast={notify} />}
          />

          <Route
            path="/search"
            element={
              <RequireAuth isAuthenticated={isAuthenticated}>
                <SitterSearchPanel
                  session={session}
                  onToast={notify}
                  onPickSitter={onPickSitterAndGoToReservation}
                  onViewDetail={(id) => navigate(`/sitters/${id}`)}
                />
              </RequireAuth>
            }
          />

          <Route
            path="/sitters/:sitterProfileId"
            element={<PageCard><SitterDetailPage onPickSitter={onPickSitterAndGoToReservation} onToast={notify} /></PageCard>}
          />

          <Route
            path="/reservations"
            element={
              <RequireAuth isAuthenticated={isAuthenticated}>
                <ReservationPanel session={session} onToast={notify} pickedSitter={pickedSitter} />
              </RequireAuth>
            }
          />

          <Route
            path="/account"
            element={
              <RequireAuth isAuthenticated={isAuthenticated}>
                <AccountPanel onAuthChanged={onAuthChanged} onToast={notify} />
              </RequireAuth>
            }
          />

          <Route path="/about" element={<ServiceIntroPage />} />

          <Route
            path="/profile"
            element={
              <RequireAuth isAuthenticated={isAuthenticated}>
                <ProfilePanel session={session} onAuthChanged={onAuthChanged} onToast={notify} />
              </RequireAuth>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}

