// 상단 토스트 메시지 컴포넌트입니다.
import React, { useEffect } from 'react';

// 토스트 UI를 렌더링합니다.
export function Toast({ toast, onClose }) {
  // 토스트가 없으면 렌더링하지 않습니다.
  if (!toast) return null;

  // 일정 시간이 지나면 자동으로 닫히게 합니다.
  useEffect(() => {
    // 자동 닫힘 타이머를 설정합니다.
    const timer = setTimeout(() => {
      // 토스트를 닫습니다.
      onClose?.();
    }, toast.durationMs ?? 2600);
    // 컴포넌트 언마운트/토스트 변경 시 타이머를 정리합니다.
    return () => clearTimeout(timer);
  }, [toast, onClose]);

  // 토스트 타입에 따른 클래스를 정합니다.
  const typeClass = toast.type === 'error' ? 'toast error' : 'toast success';

  return (
    <div className={typeClass} role="status" aria-live="polite">
      <div style={{ fontWeight: 800 }}>{toast.title || (toast.type === 'error' ? '오류' : '완료')}</div>
      <div style={{ marginTop: 4, fontSize: 12, opacity: 0.9 }}>{toast.message}</div>
      <button className="toastClose" onClick={onClose} aria-label="닫기">
        닫기
      </button>
    </div>
  );
}

