// 회원가입/로그인 UI를 제공하는 패널입니다.
import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { TextInput } from '../components/TextInput.jsx';
import { Select } from '../components/Select.jsx';
import { login, signUp } from '../lib/authApi.js';
import { setAccessToken, setEmail, setName, setRole } from '../lib/storage.js';
import { ApiError } from '../lib/api.js';

// 인증 패널을 렌더링합니다.
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

  const onSignUp = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await signUp({
        email: signUpEmail,
        password: signUpPassword,
        name: signUpName,
        role: signUpRole
      });
      setAccessToken(res.accessToken);
      setEmail(res.email);
      setRole(res.role);
      setName(res.name);
      setSuccess('회원가입 성공! 토큰이 저장되었습니다.');
      onToast?.({ type: 'success', title: '회원가입', message: '회원가입 및 자동 로그인에 성공했습니다.' });
      onAuthChanged?.();
    } catch (e) {
      if (e instanceof ApiError) setError(e.message);
      else setError('회원가입 중 알 수 없는 오류가 발생했습니다.');
      onToast?.({ type: 'error', title: '회원가입 실패', message: e instanceof ApiError ? e.message : '오류가 발생했습니다.' });
    } finally {
      setLoading(false);
    }
  };

  const onLogin = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const res = await login({
        email: loginEmail,
        password: loginPassword
      });
      setAccessToken(res.accessToken);
      setEmail(res.email);
      setRole(res.role);
      setName(res.name);
      setSuccess('로그인 성공! 토큰이 저장되었습니다.');
      onToast?.({ type: 'success', title: '로그인', message: '로그인에 성공했습니다.' });
      onAuthChanged?.();
      navigate('/', { replace: true });
    } catch (e) {
      if (e instanceof ApiError) setError(e.message);
      else setError('로그인 중 알 수 없는 오류가 발생했습니다.');
      onToast?.({ type: 'error', title: '로그인 실패', message: e instanceof ApiError ? e.message : '오류가 발생했습니다.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="authPanel">
      {mode === 'login' ? (
        <div className="authCard">
          <div className="authCardHeader">
            <span className="authCardIcon" aria-hidden>
              🔒
            </span>
            <h1 className="authCardTitle">로그인</h1>
            <p className="authCardSubtitle">Basisi에 다시 오신 것을 환영해요</p>
          </div>

          <div className="authForm">
            <TextInput label="이메일" value={loginEmail} onChange={setLoginEmail} placeholder="email@example.com" />
            <TextInput
              label="비밀번호"
              type="password"
              value={loginPassword}
              onChange={setLoginPassword}
              placeholder="비밀번호를 입력하세요"
            />
            <button type="button" className="btn authSubmit" onClick={onLogin} disabled={loading}>
              {loading ? '처리 중…' : '로그인 하기'}
            </button>
          </div>

          {error ? <div className="error authMessage">{error}</div> : null}
          {success ? <div className="success authMessage">{success}</div> : null}

          <p className="authFooterNote">
            <button type="button" className="authLinkButton" onClick={switchToSignUp}>
              계정이 없으신가요? <span className="authLinkEm">회원가입하기</span>
            </button>
          </p>
        </div>
      ) : (
        <div className="authCard">
          <div className="authCardHeader">
            <span className="authCardIcon" aria-hidden>
              ✨
            </span>
            <h1 className="authCardTitle">회원가입</h1>
            <p className="authCardSubtitle">Basisi의 회원이 되어 부모와 시터를 안전하게 연결해 보세요</p>
          </div>

          <div className="authForm">
            <TextInput label="이름" value={signUpName} onChange={setSignUpName} placeholder="홍길동" />
            <TextInput
              label="이메일 (로그인 ID로 사용됩니다)"
              value={signUpEmail}
              onChange={setSignUpEmail}
              placeholder="email@example.com"
            />
            <TextInput
              label="비밀번호 (8자 이상)"
              type="password"
              value={signUpPassword}
              onChange={setSignUpPassword}
              placeholder="비밀번호를 입력하세요"
            />
            <Select
              label="역할"
              value={signUpRole}
              onChange={setSignUpRole}
              options={[
                { value: 'PARENT', label: '부모 (PARENT)' },
                { value: 'SITTER', label: '시터 (SITTER)' }
              ]}
            />
            <button type="button" className="btn authSubmit authSubmitAlt" onClick={onSignUp} disabled={loading}>
              {loading ? '처리 중…' : '가입 완료하기'}
            </button>
          </div>

          {error ? <div className="error authMessage">{error}</div> : null}
          {success ? <div className="success authMessage">{success}</div> : null}

          <p className="authRoleHint">역할에 따라 예약·프로필 기능이 달라집니다.</p>

          <p className="authFooterNote">
            <button type="button" className="authLinkButton" onClick={switchToLogin}>
              이미 계정이 있으신가요? <span className="authLinkEm">로그인으로 돌아가기</span>
            </button>
          </p>
        </div>
      )}
    </div>
  );
}
