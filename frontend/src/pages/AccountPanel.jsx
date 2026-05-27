import React, { useEffect, useMemo, useState } from 'react';
import { ApiError } from '../lib/api.js';
import { TextInput } from '../components/TextInput.jsx';
import {
  getAccountMe,
  updateAccountEmail,
  updateAccountName,
  updateAccountPassword,
} from '../lib/accountApi.js';
import { setAccessToken, setEmail, setName } from '../lib/storage.js';

import './AccountPanel.css';

export function AccountPanel({ onAuthChanged, onToast }) {
  const [tab, setTab] = useState('BASIC'); // BASIC | PASSWORD | EMAIL
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [me, setMe] = useState(null);

  const [name, setNameState] = useState('');
  const [email, setEmailState] = useState('');
  const [nextEmail, setNextEmail] = useState('');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newPassword2, setNewPassword2] = useState('');

  const displayEmail = useMemo(() => me?.email || email || '', [me, email]);

  const loadMe = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getAccountMe();
      setMe(res);
      setNameState(res?.name ?? '');
      setEmailState(res?.email ?? '');
      setNextEmail(res?.email ?? '');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '회원 정보 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const commitName = async () => {
    const next = (name || '').trim();
    if (!next) {
      setError('이름을 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await updateAccountName(next);
      setMe(res);
      setName(res?.name ?? next);
      onAuthChanged?.();
      onToast?.({ type: 'success', title: '저장', message: '이름이 변경되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '이름 변경 중 오류가 발생했습니다.';
      setError(msg);
      onToast?.({ type: 'error', title: '실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const commitPassword = async () => {
    if (!currentPassword) {
      setError('현재 비밀번호를 입력해주세요.');
      return;
    }
    if (!newPassword || newPassword.length < 6) {
      setError('새 비밀번호는 6자 이상이어야 합니다.');
      return;
    }
    if (newPassword !== newPassword2) {
      setError('새 비밀번호 확인이 일치하지 않습니다.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await updateAccountPassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      setNewPassword2('');
      onToast?.({ type: 'success', title: '변경', message: '비밀번호가 변경되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '비밀번호 변경 중 오류가 발생했습니다.';
      setError(msg);
      onToast?.({ type: 'error', title: '실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const commitEmail = async () => {
    const next = (nextEmail || '').trim();
    if (!next) {
      setError('이메일을 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await updateAccountEmail(next);
      setMe(res);
      setEmail(res?.email ?? next);
      setNextEmail(res?.email ?? next);
      if (res?.accessToken) {
        setAccessToken(res.accessToken);
      }
      onAuthChanged?.();
      onToast?.({ type: 'success', title: '변경', message: '이메일이 변경되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '이메일 변경 중 오류가 발생했습니다.';
      setError(msg);
      onToast?.({ type: 'error', title: '실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const onTabChange = (next) => {
    setTab(next);
    setError('');
  };

  return (
    <div className="basisi-account">
      <div className="bac-shell">
        <div className="bac-header">
          <h1 className="bac-title">회원 정보 수정</h1>
          <p className="bac-subtitle">기본 정보·비밀번호·이메일을 안전하게 변경할 수 있어요.</p>
        </div>

        <div className="bac-tab-row">
          <button
            type="button"
            className={`btn ${tab === 'BASIC' ? 'primary' : ''}`}
            onClick={() => onTabChange('BASIC')}
          >
            기본 정보 수정
          </button>
          <button
            type="button"
            className={`btn ${tab === 'PASSWORD' ? 'primary' : ''}`}
            onClick={() => onTabChange('PASSWORD')}
          >
            비밀번호 변경
          </button>
          <button
            type="button"
            className={`btn ${tab === 'EMAIL' ? 'primary' : ''}`}
            onClick={() => onTabChange('EMAIL')}
          >
            이메일 변경
          </button>
          <button type="button" className="btn" onClick={loadMe} disabled={loading}>
            {loading ? '불러오는 중...' : '새로고침'}
          </button>
        </div>

        <div className="divider" />

        {error ? <div className="error">{error}</div> : null}

        {tab === 'BASIC' ? (
          <div>
            <div className="bac-info">
              <span className="bac-info-label">아이디(이메일)</span>
              <span className="badge">{displayEmail || '-'}</span>
            </div>
            <TextInput label="이름" value={name} onChange={setNameState} placeholder="이름" />
            <div className="bac-cta-row">
              <button
                type="button"
                className="btn primary lg"
                onClick={commitName}
                disabled={loading}
              >
                {loading ? '저장중...' : '저장'}
              </button>
            </div>
          </div>
        ) : null}

        {tab === 'PASSWORD' ? (
          <div>
            <TextInput
              label="현재 비밀번호"
              value={currentPassword}
              onChange={setCurrentPassword}
              placeholder="현재 비밀번호"
              type="password"
            />
            <TextInput
              label="새 비밀번호 (6자 이상)"
              value={newPassword}
              onChange={setNewPassword}
              placeholder="새 비밀번호"
              type="password"
            />
            <TextInput
              label="새 비밀번호 확인"
              value={newPassword2}
              onChange={setNewPassword2}
              placeholder="새 비밀번호 확인"
              type="password"
            />
            <div className="bac-cta-row">
              <button
                type="button"
                className="btn primary lg"
                onClick={commitPassword}
                disabled={loading}
              >
                {loading ? '변경중...' : '변경'}
              </button>
            </div>
          </div>
        ) : null}

        {tab === 'EMAIL' ? (
          <div>
            <div className="bac-info">
              <span className="bac-info-label">현재 이메일</span>
              <span className="badge">{displayEmail || '-'}</span>
            </div>
            <TextInput
              label="변경할 이메일"
              value={nextEmail}
              onChange={setNextEmail}
              placeholder="example@domain.com"
            />
            <p className="bac-help">인증 요청 없이 바로 변경됩니다.</p>
            <div className="bac-cta-row">
              <button
                type="button"
                className="btn primary lg"
                onClick={commitEmail}
                disabled={loading}
              >
                {loading ? '변경중...' : '이메일 변경'}
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
