import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import './HomePage.css';

const TICKER_ITEMS = [
  '빠른 응답',
  '3단계 검증',
  '실시간 채팅',
  '위치 기반 탐색',
  '평점/후기',
  '예약 관리',
];

const PREVIEW_SITTERS = [
  {
    name: '김지현',
    meta: '서울 마포구 · 경력 5년',
    rate: '₩18,000/시간',
    avatar: '👩‍👧',
    badge: '인기',
    badgeClass: '',
    rating: '4.8',
  },
  {
    name: '이수진',
    meta: '서울 강남구 · 경력 3년',
    rate: '₩15,000/시간',
    avatar: '👩‍🏫',
    badge: '신규',
    badgeClass: 'is-new',
    rating: '4.8',
  },
  {
    name: '최예린',
    meta: '서울 송파구 · 경력 7년',
    rate: '₩20,000/시간',
    avatar: '🧑‍🎨',
    badge: '최고평점',
    badgeClass: 'is-top',
    rating: '4.8',
  },
];

const FEATURED_SITTERS = [
  {
    name: '김지현',
    meta: '서울 마포구 · 경력 5년',
    rate: '₩18,000/시간',
    img: '👩‍👧',
    badge: '인기',
    badgeClass: '',
  },
  {
    name: '이수진',
    meta: '서울 강남구 · 경력 3년',
    rate: '₩15,000/시간',
    img: '👩‍🏫',
    badge: '신규',
    badgeClass: 'is-new',
  },
  {
    name: '박민준',
    meta: '경기 성남시 · 경력 4년',
    rate: '₩16,000/시간',
    img: '👨‍👦',
    badge: '추천',
    badgeClass: 'is-recommend',
  },
  {
    name: '최예린',
    meta: '서울 송파구 · 경력 7년',
    rate: '₩20,000/시간',
    img: '🧑‍🎨',
    badge: '최고평점',
    badgeClass: 'is-top',
  },
];

const HOW_STEPS = [
  {
    num: '01',
    icon: '🔍',
    title: '조건 설정 & 검색',
    desc: '지역/거리/시급/시간대/경력을 설정해 원하는 시터를 빠르게 찾습니다. 조건을 넓히면 더 많은 후보를, 좁히면 더 정확한 후보를 추천받을 수 있어요.',
    bullets: ['원하는 시간대/예산으로 필터', '거리 기반으로 가까운 시터 우선', '경력/자격 등 핵심 정보 한눈에'],
  },
  {
    num: '02',
    icon: '👤',
    title: '프로필 비교 & 확인',
    desc: '시터의 상세 프로필, 후기, 경력 사항을 꼼꼼히 비교하세요. 3단계 검증을 통과한 시터만 플랫폼에 등록됩니다.',
    bullets: ['신원/이력/자격 검증 완료', '실제 이용 부모님의 솔직한 후기', '시터와 실시간 채팅 문의 가능'],
  },
  {
    num: '03',
    icon: '📅',
    title: '예약 신청 & 확정',
    desc: '마음에 드는 시터를 찾았다면 바로 예약 신청! 시터의 수락 후 일정이 확정되며, 모든 과정이 앱 안에서 이뤄집니다.',
    bullets: ['간편한 예약 신청 및 관리', '예약 확정 즉시 알림 발송', '일정 변경/취소도 앱에서 간편하게'],
  },
];

const FAQS = [
  {
    q: '시터 신원 검증은 어떻게 이루어지나요?',
    a: '신원 확인, 이력/자격 검증, 활동 모니터링 등 단계적 검증을 거칩니다.',
  },
  {
    q: '예약 취소/환불은 어떻게 되나요?',
    a: '예약 취소는 마이페이지에서 진행할 수 있으며, 취소 정책에 따라 환불이 처리됩니다. 자세한 내용은 서비스 이용약관을 참고해 주세요.',
  },
  {
    q: '돌봄 중 문제가 생기면 어떻게 하나요?',
    a: '실시간 채팅 및 고객센터를 통해 즉시 지원받을 수 있습니다. Basisi는 돌봄 진행 중에도 24시간 모니터링을 통해 안전한 서비스를 보장합니다.',
  },
];

/** IntersectionObserver 기반 스크롤 리빌 훅. */
function useReveal() {
  const ref = useRef(null);

  useEffect(() => {
    const root = ref.current;
    if (!root || typeof IntersectionObserver === 'undefined') return undefined;

    const targets = root.querySelectorAll('.bh-reveal');
    if (!targets.length) return undefined;

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 },
    );

    targets.forEach((el) => io.observe(el));
    return () => io.disconnect();
  }, []);

  return ref;
}

export function HomePage({ onGoSearch }) {
  const navigate = useNavigate();
  const [openFaq, setOpenFaq] = useState(0);
  const rootRef = useReveal();

  const tickerCycle = useMemo(() => [0, 1], []);

  const handleGoSearch = () => {
    if (onGoSearch) onGoSearch();
    else navigate('/search');
  };

  const handleToggleFaq = (idx) => {
    setOpenFaq((cur) => (cur === idx ? -1 : idx));
  };

  return (
    <div className="basisi-home" ref={rootRef}>
      {/* HERO */}
      <section className="bh-hero" aria-label="Basisi 소개">
        <div className="bh-hero-text">
          <div className="bh-hero-badge">🛡️ 3단계 검증 완료 시터</div>
          <h1>
            우리 가족을 위한
            <br />
            <em>프로페셔널 돌봄</em>을
            <br />
            더 빠르고 안전하게.
          </h1>
          <p className="bh-hero-sub">
            검증된 시터 탐색, 프로필 비교, 예약 신청/수락까지.
            <br />
            Basisi에서 한 번에 진행하세요.
          </p>
          <div className="bh-hero-cta">
            <button type="button" className="bh-cta-main" onClick={handleGoSearch}>
              🔍 시터 검색하기
            </button>
            <Link to="/about" className="bh-cta-sec">
              서비스 알아보기 →
            </Link>
          </div>
        </div>

        <div className="bh-hero-visual" aria-hidden>
          <div className="bh-floating-stat fs-1">
            <div className="bh-num">98%</div>
            <div className="bh-label">부모 만족도</div>
          </div>

          <div className="bh-preview-card">
            <h3 className="bh-preview-title">🌟 인기 시터 미리보기</h3>
            <div className="bh-preview-list">
              {PREVIEW_SITTERS.map((s) => (
                <div className="bh-preview-row" key={s.name}>
                  <div className="bh-preview-avatar">{s.avatar}</div>
                  <div className="bh-preview-info">
                    <div className="bh-preview-name">
                      {s.name}
                      <span className={`bh-badge ${s.badgeClass}`}>{s.badge}</span>
                    </div>
                    <div className="bh-preview-meta">{s.meta}</div>
                  </div>
                  <div>
                    <div className="bh-preview-rate">{s.rate}</div>
                    <div className="bh-preview-stars">
                      ★★★★★ <span className="bh-star-num">{s.rating}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bh-floating-stat fs-2">
            <div className="bh-num">15,000+</div>
            <div className="bh-label">완료된 매칭</div>
          </div>
        </div>
      </section>

      {/* TICKER */}
      <div className="bh-ticker-wrap" aria-label="Basisi 핵심 기능">
        <div className="bh-ticker-track" aria-hidden="true">
          {tickerCycle.map((cycle) => (
            <React.Fragment key={cycle}>
              {TICKER_ITEMS.map((item) => (
                <span className="bh-ticker-item" key={`${cycle}-${item}`}>
                  <span className="bh-ticker-dot" />
                  {item}
                </span>
              ))}
            </React.Fragment>
          ))}
        </div>
      </div>

      {/* STATS */}
      <section className="bh-stats-section" aria-label="Basisi 서비스 지표">
        <div className="bh-stats-grid">
          <div className="bh-stat-cell bh-reveal">
            <div className="bh-stat-num">2,400+</div>
            <div className="bh-stat-label">등록된 베이비시터</div>
          </div>
          <div className="bh-stat-cell bh-reveal">
            <div className="bh-stat-num">98%</div>
            <div className="bh-stat-label">부모 만족도</div>
          </div>
          <div className="bh-stat-cell bh-reveal">
            <div className="bh-stat-num">15,000+</div>
            <div className="bh-stat-label">완료된 매칭</div>
          </div>
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section className="bh-section is-white" id="how">
        <p className="bh-section-tag">HOW IT WORKS</p>
        <h2 className="bh-section-title">간단한 3단계로 최적의 시터를 만나세요</h2>
        <p className="bh-section-sub">조건 설정 → 프로필 확인 → 예약 확정까지 빠르게 연결합니다.</p>

        <div className="bh-steps-grid">
          {HOW_STEPS.map((step) => (
            <article className="bh-step-card bh-reveal" key={step.num}>
              <div className="bh-step-num">{step.num}</div>
              <div className="bh-step-icon" aria-hidden>{step.icon}</div>
              <h3 className="bh-step-title">{step.title}</h3>
              <p className="bh-step-desc">{step.desc}</p>
              <ul className="bh-step-list">
                {step.bullets.map((b) => (
                  <li key={b}>{b}</li>
                ))}
              </ul>
            </article>
          ))}
        </div>
      </section>

      {/* FEATURED SITTERS */}
      <section className="bh-section is-light" id="sitters">
        <p className="bh-section-tag">FEATURED SITTERS</p>
        <h2 className="bh-section-title">인기 베이비시터를 만나보세요</h2>
        <div className="bh-sitters-note">
          💡 지금은 데모 카드이며, 실제 검색/예약은 상단 메뉴의 시터검색에서 진행됩니다.
        </div>

        <div className="bh-sitters-grid">
          {FEATURED_SITTERS.map((s) => (
            <article
              className="bh-sitter-card bh-reveal"
              key={s.name}
              role="button"
              tabIndex={0}
              onClick={handleGoSearch}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleGoSearch();
                }
              }}
            >
              <div className="bh-sitter-img" aria-hidden>{s.img}</div>
              <div className="bh-sitter-body">
                <div className="bh-sitter-head">
                  <span className="bh-sitter-name">{s.name}</span>
                  <span className={`bh-badge ${s.badgeClass}`}>{s.badge}</span>
                </div>
                <div className="bh-sitter-loc">{s.meta}</div>
                <div className="bh-sitter-foot">
                  <span className="bh-sitter-price">{s.rate}</span>
                  <span className="bh-sitter-stars">
                    ★★★★★ <span className="bh-star-num">4.8+</span>
                  </span>
                </div>
              </div>
            </article>
          ))}
        </div>

        <button type="button" className="bh-link-cta" onClick={handleGoSearch}>
          실제 시터 검색으로 이동 →
        </button>
      </section>

      {/* FAQ */}
      <section className="bh-section is-white" id="faq">
        <p className="bh-section-tag">FAQ</p>
        <h2 className="bh-section-title">자주 묻는 질문</h2>
        <p className="bh-section-sub">서비스 이용 전 꼭 확인해주세요.</p>

        <div className="bh-faq-list">
          {FAQS.map((item, idx) => {
            const open = openFaq === idx;
            return (
              <div className={`bh-faq-item ${open ? 'is-open' : ''}`} key={item.q}>
                <button
                  type="button"
                  className="bh-faq-q"
                  onClick={() => handleToggleFaq(idx)}
                  aria-expanded={open}
                >
                  {item.q}
                  <span className="bh-faq-arrow" aria-hidden>▼</span>
                </button>
                <div className="bh-faq-a" role="region" aria-hidden={!open}>
                  {item.a}
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* FOOTER (다크) */}
      <footer className="bh-footer">
        <div className="bh-footer-grid">
          <div className="bh-footer-brand">
            <div className="bh-footer-logo">
              🐣 BASIS<em>I</em>
            </div>
            <p>
              검증된 베이비시터와 가족을 연결하는
              <br />
              안심 돌봄 매칭 플랫폼.
            </p>
          </div>

          <div className="bh-footer-col">
            <h4>서비스</h4>
            <Link to="/search">시터 찾기</Link>
            <Link to="/reservations">예약</Link>
            <Link to="/profile">마이페이지</Link>
          </div>

          <div className="bh-footer-col">
            <h4>가이드</h4>
            <a href="#how">이용 방법</a>
            <a href="#faq">FAQ</a>
          </div>

          <div className="bh-footer-col">
            <h4>계정</h4>
            <Link to="/auth">로그인/회원가입</Link>
          </div>
        </div>

        <div className="bh-footer-bottom">© 2026 Basisi. Graduation Project.</div>
      </footer>
    </div>
  );
}
