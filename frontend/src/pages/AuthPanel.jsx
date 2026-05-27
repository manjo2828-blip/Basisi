// 회원가입/로그인 UI를 제공하는 패널입니다.
import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { login, signUp } from '../lib/authApi.js';
import { setAccessToken, setEmail, setName, setRole } from '../lib/storage.js';
import { ApiError } from '../lib/api.js';

import './AuthPanel.css';

export function AuthPanel({ onAuthChanged, onToast }) {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [mode, setMode] = useState('login');

  useEffect(() => {
    const nextMode = searchParams.get('mode');
    if (nextMode === 'signup') setMode('signup');
    else setMode('login');
    // URL 파라미터가 바뀌면 모드만 동기화합니다.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const [signUpEmail, setSignUpEmail] = useState('');
  const [signUpPassword, setSignUpPassword] = useState('');
  const [signUpName, setSignUpName] = useState('');
  const [signUpRole, setSignUpRole] = useState('PARENT');

  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const switchToLogin = () => {
    setMode('login');
    setSearchParams({});
    setError('');
    setSuccess('');
  };

  const switchToSignUp = () => {
    setMode('signup');
    setSearchParams({ mode: 'signup' });
    setError('');
    setSuccess('');
  };

  const onSignUp = async (e) => {
    e?.preventDefault?.();
    if (loading) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await signUp({
        email: signUpEmail,
        password: signUpPassword,
        name: signUpName,
        role: signUpRole,
      });
      setAccessToken(res.accessToken);
      setEmail(res.email);
      setRole(res.role);
      setName(res.name);
      setSuccess('회원가입 성공! 토큰이 저장되었습니다.');
      onToast?.({ type: 'success', title: '회원가입', message: '회원가입 및 자동 로그인에 성공했습니다.' });
      onAuthChanged?.();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '회원가입 중 알 수 없는 오류가 발생했습니다.';
      setError(message);
      onToast?.({ type: 'error', title: '회원가입 실패', message });
    } finally {
      setLoading(false);
    }
  };

  const onLogin = async (e) => {
    e?.preventDefault?.();
    if (loading) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await login({
        email: loginEmail,
        password: loginPassword,
      });
      setAccessToken(res.accessToken);
      setEmail(res.email);
      setRole(res.role);
      setName(res.name);
      setSuccess('로그인 성공! 토큰이 저장되었습니다.');
      onToast?.({ type: 'success', title: '로그인', message: '로그인에 성공했습니다.' });
      onAuthChanged?.();
      navigate('/', { replace: true });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : '로그인 중 알 수 없는 오류가 발생했습니다.';
      setError(message);
      onToast?.({ type: 'error', title: '로그인 실패', message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="basisi-auth">
      {mode === 'login' ? (
        <div className="ba-card" role="region" aria-label="로그인">
          <div className="ba-icon" aria-hidden>🔒</div>
          <h2 className="ba-title">로그인</h2>
          <p className="ba-subtitle">Basisi에 다시 오신 것을 환영해요</p>

          <form className="ba-form" onSubmit={onLogin} noValidate>
            <div className="ba-field">
              <label htmlFor="ba-login-email">이메일</label>
              <input
                id="ba-login-email"
                type="email"
                className="ba-input"
                placeholder="email@example.com"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                autoComplete="email"
                disabled={loading}
              />
            </div>

            <div className="ba-field">
              <label htmlFor="ba-login-password">비밀번호</label>
              <input
                id="ba-login-password"
                type="password"
                className="ba-input"
                placeholder="비밀번호를 입력하세요"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                autoComplete="current-password"
                disabled={loading}
              />
            </div>

            {error ? <div className="ba-message is-error" role="alert">{error}</div> : null}
            {success ? <div className="ba-message is-success" role="status">{success}</div> : null}

            <button type="submit" className="ba-submit" disabled={loading}>
              {loading ? '처리 중…' : '로그인 하기'}
            </button>
          </form>

          <p className="ba-footer">
            계정이 없으신가요?
            <button type="button" className="ba-link" onClick={switchToSignUp}>
              회원가입하기
            </button>
          </p>
        </div>
      ) : (
        <div className="ba-card" role="region" aria-label="회원가입">
          <div className="ba-icon" aria-hidden>✨</div>
          <h2 className="ba-title">회원가입</h2>
          <p className="ba-subtitle">Basisi의 회원이 되어 부모와 시터를 안전하게 연결해 보세요</p>

          <form className="ba-form" onSubmit={onSignUp} noValidate>
            <div className="ba-field">
              <label htmlFor="ba-signup-name">이름</label>
              <input
                id="ba-signup-name"
                type="text"
                className="ba-input"
                placeholder="홍길동"
                value={signUpName}
                onChange={(e) => setSignUpName(e.target.value)}
                autoComplete="name"
                disabled={loading}
              />
            </div>

            <div className="ba-field">
              <label htmlFor="ba-signup-email">이메일 (로그인 ID로 사용됩니다)</label>
              <input
                id="ba-signup-email"
                type="email"
                className="ba-input"
                placeholder="email@example.com"
                value={signUpEmail}
                onChange={(e) => setSignUpEmail(e.target.value)}
                autoComplete="email"
                disabled={loading}
              />
            </div>

            <div className="ba-field">
              <label htmlFor="ba-signup-password">비밀번호 (8자 이상)</label>
              <input
                id="ba-signup-password"
                type="password"
                className="ba-input"
                placeholder="비밀번호를 입력하세요"
                value={signUpPassword}
                onChange={(e) => setSignUpPassword(e.target.value)}
                autoComplete="new-password"
                disabled={loading}
              />
            </div>

            <div className="ba-field">
              <label htmlFor="ba-signup-role">역할</label>
              <select
                id="ba-signup-role"
                className="ba-select"
                value={signUpRole}
                onChange={(e) => setSignUpRole(e.target.value)}
                disabled={loading}
              >
                <option value="PARENT">부모 (PARENT)</option>
                <option value="SITTER">시터 (SITTER)</option>
              </select>
              <p className="ba-hint">역할에 따라 예약·프로필 기능이 달라집니다.</p>
            </div>

            {error ? <div className="ba-message is-error" role="alert">{error}</div> : null}
            {success ? <div className="ba-message is-success" role="status">{success}</div> : null}

            <button type="submit" className="ba-submit" disabled={loading}>
              {loading ? '처리 중…' : '가입 완료하기'}
            </button>
          </form>

          <p className="ba-footer">
            이미 계정이 있으신가요?
            <button type="button" className="ba-link" onClick={switchToLogin}>
              로그인으로 돌아가기
            </button>
          </p>
        </div>
      )}
    </div>
  );
}
