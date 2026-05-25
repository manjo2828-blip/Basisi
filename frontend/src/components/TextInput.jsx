// 공통 텍스트 입력 컴포넌트입니다.
import React from 'react';

// 텍스트 입력 UI를 렌더링합니다.
export function TextInput({ label, value, onChange, placeholder, type = 'text' }) {
  // 라벨과 인풋을 함께 렌더링합니다.
  return (
    <div className="field">
      <label>{label}</label>
      <input
        // 입력 타입을 지정합니다.
        type={type}
        // 입력값을 바인딩합니다.
        value={value}
        // 입력 변경 시 상위로 값을 전달합니다.
        onChange={(e) => onChange(e.target.value)}
        // 플레이스홀더를 설정합니다.
        placeholder={placeholder}
      />
    </div>
  );
}

