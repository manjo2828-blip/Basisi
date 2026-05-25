import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';

export function HomePage({ onGoSearch }) {
  const [openFaqIdx, setOpenFaqIdx] = useState(0);
  const [howStep, setHowStep] = useState(0);

  const howSteps = useMemo(
    () => [
      {
        title: '🔍 조건 설정 & 검색',
        desc:
          '지역/거리/시급/시간대/경력을 설정해 원하는 시터를 빠르게 찾습니다. 조건을 넓히면 더 많은 후보를, 좁히면 더 정확한 후보를 추천받을 수 있어요.',
        bullets: ['원하는 시간대/예산으로 필터', '거리 기반으로 가까운 시터 우선', '경력/자격 등 핵심 정보 한눈에']
      },
      {
        title: '🧾 프로필 확인',
        desc:
          '프로필에서 소개, 가능 시간, 시급, 경력 정보를 비교하고 우리 가족에게 맞는 시터를 선택합니다. 중요한 포인트만 골라 빠르게 비교해보세요.',
        bullets: ['가능 시간·시급·경력 비교', '소개글로 돌봄 스타일 확인', '필요하면 상세 페이지에서 더 확인']
      },
      {
        title: '🤝 예약 확정',
        desc:
          '부모는 신청/취소, 시터는 수락/거절로 예약이 진행됩니다. 충돌 검증은 서버가 처리해, 같은 시간대에 중복 예약이 되지 않도록 안전하게 관리됩니다.',
        bullets: ['신청 → 수락으로 확정', '예약 변경/취소 흐름 제공', '서버에서 시간 충돌 검증']
      }
    ],
    []
  );

  const totalHow = howSteps.length;
  const goPrevHow = () => setHowStep((v) => (v - 1 + totalHow) % totalHow);
  const goNextHow = () => setHowStep((v) => (v + 1) % totalHow);

  const featuredSitters = useMemo(
    () => [
      { name: '김지현', meta: '서울 마포구 · 경력 5년', rate: '₩18,000/시간', emoji: '👩‍🍼', tag: '인기' },
      { name: '이수진', meta: '서울 강남구 · 경력 3년', rate: '₩15,000/시간', emoji: '👩🏻‍🏫', tag: '신규' },
      { name: '박민준', meta: '경기 성남시 · 경력 4년', rate: '₩16,000/시간', emoji: '🧑‍🍼', tag: '추천' },
      { name: '최예린', meta: '서울 송파구 · 경력 7년', rate: '₩20,000/시간', emoji: '👩‍🎨', tag: '최고평점' }
    ],
    []
  );

  const faq = [
    { q: '시터 신원 검증은 어떻게 이루어지나요?', a: '신원 확인, 이력/자격 검증, 활동 모니터링 등 단계적 검증을 거칩니다.' },
    { q: '예약 취소/환불은 어떻게 되나요?', a: '취소 시점에 따라 환불 규정이 달라지며, 예약 화면에서 안내됩니다.' },
    { q: '돌봄 중 문제가 생기면 어떻게 하나요?', a: '문의/지원 채널로 즉시 연결되며, 예약 건 기준으로 필요한 조치를 진행합니다.' }
  ];

  return (
    <div>
      <section className="bbHero">
        <div className="content">
          <div className="bbHeroGrid">
            <div>
              <h1 className="bbH1">
                우리 가족을 위한
                <br />
                <em>프로페셔널 돌봄</em>을
                <br />
                더 빠르고 안전하게.
              </h1>
              <p className="bbLead">
                검증된 시터 탐색, 프로필 비교, 예약 신청/수락까지.
                <br />
                Basisi에서 한 번에 진행하세요.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="bbMarqueeFullBleed" aria-label="Basisi 핵심 기능">
        <div className="bbMarquee">
          <div className="content">
            <div className="bbMarqueeTrack" aria-hidden="true">
              {Array.from({ length: 2 }).map((_, k) => (
                <React.Fragment key={k}>
                  <div className="bbMarqueeItem">⭐ 평점/후기</div>
                  <div className="bbMarqueeItem">🧾 예약 관리</div>
                  <div className="bbMarqueeItem">⏱️ 빠른 응답</div>
                  <div className="bbMarqueeItem">🛡️ 3단계 검증</div>
                  <div className="bbMarqueeItem">💬 실시간 채팅</div>
                  <div className="bbMarqueeItem">📍 위치 기반 탐색</div>
                </React.Fragment>
              ))}
            </div>
          </div>
        </div>
      </section>

      <div className="bbPinkLineFullBleed" aria-hidden="true" />

      <section className="bbStatsFullBleed" aria-label="Basisi 서비스 지표">
        <div className="content">
          <div className="bbStats bbStatsPlain">
            <div className="bbStat">
              <span className="bbStatLabel">등록된 베이비시터</span>
              <b className="bbStatValue">2,400+</b>
            </div>
            <div className="bbStat">
              <span className="bbStatLabel">부모 만족도</span>
              <b className="bbStatValue">98%</b>
            </div>
            <div className="bbStat">
              <span className="bbStatLabel">완료된 매칭</span>
              <b className="bbStatValue">15,000+</b>
            </div>
          </div>
        </div>
      </section>

      <div className="bbPinkLineFullBleed" aria-hidden="true" />

      <section className="bbSection bbSectionAlt" id="how">
        <div className="content">
          <div className="bbSectionHead">
            <div className="bbEyebrow">HOW IT WORKS</div>
            <h2 className="bbH2">간단한 3단계로 최적의 시터를 만나세요</h2>
            <p className="bbSub">조건 설정 → 프로필 확인 → 예약 확정까지 빠르게 연결합니다.</p>
          </div>

          <div className="bbHowCarousel" aria-label="이용 방법 단계">
            <div className="bbHowViewport">
              <button type="button" className="bbHowSideArrow left" onClick={goPrevHow} aria-label="이전 단계">
                &lt;
              </button>

              <div
                className="bbHowTrack"
                style={{ transform: `translateX(-${howStep * 100}%)` }}
                aria-live="polite"
              >
                {howSteps.map((step) => (
                  <div
                    key={step.title}
                    className="bbHowSlide"
                    role="group"
                    aria-roledescription="slide"
                    aria-label={step.title}
                  >
                    <div className="bbHowTitle">{step.title}</div>
                    <div className="bbHowDesc">{step.desc}</div>
                    <ul className="bbHowBullets">
                      {step.bullets.map((t) => (
                        <li key={t}>{t}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>

              <button type="button" className="bbHowSideArrow right" onClick={goNextHow} aria-label="다음 단계">
                &gt;
              </button>
            </div>

            <div className="bbHowNav">
              <button type="button" className="bbHowArrow" onClick={goPrevHow} aria-label="이전 단계">
                ←
              </button>
              <div className="bbHowPager" aria-label="단계 표시">
                <b>{howStep + 1}</b> / {totalHow}
              </div>
              <button type="button" className="bbHowArrow" onClick={goNextHow} aria-label="다음 단계">
                →
              </button>
            </div>
          </div>
        </div>
      </section>

      <section className="bbSection" id="sitters">
        <div className="content">
          <div className="bbSectionHead">
            <div className="bbEyebrow">FEATURED SITTERS</div>
            <h2 className="bbH2">인기 베이비시터를 만나보세요</h2>
            <p className="bbSub">지금은 데모 카드이며, 실제 검색/예약은 상단 메뉴의 시터검색에서 진행됩니다.</p>
          </div>

          <div className="bbSitterGrid">
            {featuredSitters.map((s) => (
              <div className="bbSitter" key={s.name}>
                <div className="bbSitterPhoto">{s.emoji}</div>
                <div className="bbSitterBody">
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, alignItems: 'center' }}>
                    <div className="bbSitterName">{s.name}</div>
                    <span className="badge">{s.tag}</span>
                  </div>
                  <div className="bbSitterMeta">{s.meta}</div>
                  <div className="bbSitterFooter">
                    <b>{s.rate}</b>
                    <span>★★★★★ 4.8+</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="bbHeroActions" style={{ marginTop: 18 }}>
            <button className="btn accent" onClick={() => onGoSearch?.()}>
              실제 시터 검색으로 이동 →
            </button>
          </div>
        </div>
      </section>

      <section className="bbSection bbSectionAlt" id="faq">
        <div className="content">
          <div className="bbSectionHead">
            <div className="bbEyebrow">FAQ</div>
            <h2 className="bbH2">자주 묻는 질문</h2>
            <p className="bbSub">서비스 이용 전 꼭 확인해주세요.</p>
          </div>

          <div className="bbFaq">
            {faq.map((item, idx) => (
              <div className="bbFaqItem" key={item.q}>
                <button className="bbFaqQ" onClick={() => setOpenFaqIdx(openFaqIdx === idx ? -1 : idx)}>
                  {item.q}
                </button>
                {openFaqIdx === idx ? <div className="bbFaqA">{item.a}</div> : null}
              </div>
            ))}
          </div>
        </div>
      </section>

      <footer className="bbFooter">
        <div className="content">
          <div className="bbFooterGrid">
            <div>
              <div className="bbFooterTitle">Basisi</div>
              <div className="bbSub">검증된 베이비시터와 가족을 연결하는 안심 돌봄 매칭 플랫폼.</div>
              <div className="bbSub" style={{ marginTop: 10, fontSize: 12, opacity: 0.8 }}>
                © 2026 Basisi. Graduation Project.
              </div>
            </div>
            <div>
              <div className="bbFooterTitle">서비스</div>
              <Link className="bbFooterLink" to="/search">시터 찾기</Link>
              <Link className="bbFooterLink" to="/reservations">예약</Link>
              <Link className="bbFooterLink" to="/profile">마이페이지</Link>
            </div>
            <div>
              <div className="bbFooterTitle">가이드</div>
              <a className="bbFooterLink" href="#how">이용 방법</a>
              <a className="bbFooterLink" href="#faq">FAQ</a>
            </div>
            <div>
              <div className="bbFooterTitle">계정</div>
              <Link className="bbFooterLink" to="/auth">로그인/회원가입</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

