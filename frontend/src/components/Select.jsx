// 공통 셀렉트 컴포넌트입니다.
import React from 'react';

// 셀렉트 UI를 렌더링합니다.
export function Select({ label, value, onChange, options }) {
  // 라벨과 셀렉트를 함께 렌더링합니다.
  return (
    <div className="field">
      <label>{label}</label>
      <select
        // 선택 값을 바인딩합니다.
        value={value}
        // 선택 변경 시 상위로 값을 전달합니다.
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}

