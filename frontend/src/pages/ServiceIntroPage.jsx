import React from 'react';

export function ServiceIntroPage() {
  return (
    <div className="aboutPage">
      <div className="aboutHead">
        <div className="bbEyebrow">ABOUT</div>
        <h2 className="bbH2">서비스 소개</h2>
        <p className="bbSub">Basisi는 부모와 베이비시터를 더 빠르고 안전하게 연결하는 매칭 서비스입니다.</p>
      </div>

      <div className="grid">
        <div className="card aboutCard">
          <div className="aboutCardTitle">🔎 시터 탐색</div>
          <div className="aboutCardText">지역/거리/시급/시간대/경력 조건으로 원하는 시터를 찾고 프로필을 비교할 수 있어요.</div>
        </div>
        <div className="card aboutCard">
          <div className="aboutCardTitle">📅 예약 관리</div>
          <div className="aboutCardText">부모는 신청/취소, 시터는 수락/거절로 예약을 관리합니다. 충돌 검증은 서버가 처리합니다.</div>
        </div>
        <div className="card aboutCard">
          <div className="aboutCardTitle">🧾 프로필 &amp; 역할</div>
          <div className="aboutCardText">로그인 역할(PARENT/SITTER)에 따라 프로필/예약 기능이 안전하게 분리됩니다.</div>
        </div>
        <div className="card aboutCard">
          <div className="aboutCardTitle">🛡️ 안전한 흐름</div>
          <div className="aboutCardText">인증 토큰 기반으로 권한을 확인하고, 필요한 기능만 접근할 수 있도록 구성했습니다.</div>
        </div>
      </div>

      <div className="divider" style={{ marginTop: 18 }} />

      <div className="aboutFoot">
        <div className="aboutFootTitle">빠르게 시작하기</div>
        <div className="aboutFootText">
          우측의 퀵 메뉴에서 <b>시터검색</b>과 <b>예약</b>으로 바로 이동할 수 있습니다.
        </div>
      </div>
    </div>
  );
}

