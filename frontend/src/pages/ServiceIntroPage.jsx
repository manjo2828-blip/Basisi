import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

import './ServiceIntroPage.css';

const FEATURES = [
  {
    heading: '🔎 시터 탐색',
    desc: '지역/거리/시급/시간대/경력 조건으로 원하는 시터를 찾고 프로필을 비교할 수 있어요.',
  },
  {
    heading: '📅 예약 관리',
    desc: '부모는 신청/취소, 시터는 수락/거절로 예약을 관리합니다. 충돌 검증은 서버가 처리합니다.',
  },
  {
    heading: '📋 프로필 & 역할',
    desc: '로그인 역할(PARENT/SITTER)에 따라 프로필/예약 기능이 안전하게 분리됩니다.',
  },
  {
    heading: '🛡️ 안전한 흐름',
    desc: '인증 토큰 기반으로 권한을 확인하고, 필요한 기능만 접근할 수 있도록 구성했습니다.',
  },
];

export function ServiceIntroPage() {
  const navigate = useNavigate();

  return (
    <div className="basisi-about">
      <div className="bsa-card">
        <span className="bsa-tag">About</span>
        <h2 className="bsa-title">서비스 소개</h2>
        <p className="bsa-subtitle">
          Basisi는 부모와 베이비시터를 더 빠르고 안전하게 연결하는 매칭 서비스입니다.
        </p>

        <div className="bsa-grid">
          {FEATURES.map((f) => (
            <article className="bsa-feature" key={f.heading}>
              <div className="bsa-feature-heading">{f.heading}</div>
              <p className="bsa-feature-desc">{f.desc}</p>
            </article>
          ))}
        </div>

        <div className="bsa-quickstart">
          <div className="bsa-quickstart-icon" aria-hidden>⚡</div>
          <div className="bsa-quickstart-body">
            <h4>빠르게 시작하기</h4>
            <p>
              우측의 퀵 메뉴에서 <b>시터검색</b>과 <b>예약</b>으로 바로 이동할 수 있습니다.
            </p>
          </div>
        </div>

        <div className="bsa-cta-row">
          <button
            type="button"
            className="bsa-cta-main"
            onClick={() => navigate('/search')}
          >
            🔍 시터 검색하기
          </button>
          <Link to="/" className="bsa-cta-sec">
            메인으로 돌아가기 →
          </Link>
        </div>
      </div>
    </div>
  );
}
