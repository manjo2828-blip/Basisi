// 시터 탐색(Phase2) UI를 제공하는 패널입니다.
import React, { useEffect, useMemo, useState } from 'react';
import { TextInput } from '../components/TextInput.jsx';
import { Select } from '../components/Select.jsx';
import { SitterResultCard } from '../components/SitterResultCard.jsx';
import { searchSittersPage } from '../lib/sitterApi.js';
import { postSitterRecommend } from '../lib/recommendApi.js';
import { ApiError } from '../lib/api.js';

import './SitterSearchPanel.css';

const SORT_OPTIONS = [
  { value: 'flameScore,desc', label: '불꽃 점수 높은 순' },
  { value: 'yearsOfExperience,desc', label: '경력 높은 순' },
  { value: 'yearsOfExperience,asc', label: '경력 낮은 순' },
  { value: 'age,asc', label: '나이 어린 순' },
  { value: 'age,desc', label: '나이 많은 순' },
  { value: 'createdAt,desc', label: '최신 등록 순' },
];

const PAGE_SIZE_OPTIONS = [
  { value: '5', label: '5개' },
  { value: '10', label: '10개' },
  { value: '20', label: '20개' },
];

export function SitterSearchPanel({ session, onToast, onPickSitter, onViewDetail }) {
  const email = useMemo(() => session?.email || null, [session]);
  const role = useMemo(() => session?.role || null, [session]);

  // 시터 탐색 화면은 지역/정렬/페이지 크기 3개 조건으로 단순화합니다.
  const [region, setRegion] = useState('서울');
  const [pageSize, setPageSize] = useState('10');
  const [sort, setSort] = useState('flameScore,desc');

  const [results, setResults] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  // 탐색 버튼을 한 번이라도 눌렀는지 (false: AI 추천 / true: 결과)
  const [isSearched, setIsSearched] = useState(false);

  // 라우팅 이동/새로고침 시에도 검색 조건/응답을 복원합니다.
  const SESSION_KEY = 'basisi.sitterSearchState';
  const RESPONSE_KEY = 'basisi.sitterSearchLastResponse';
  const RESPONSE_TTL_MS = 2 * 60 * 1000;
  useEffect(() => {
    try {
      const raw = sessionStorage.getItem(SESSION_KEY);
      if (!raw) return;
      const s = JSON.parse(raw);
      if (s && typeof s === 'object') {
        const signature = JSON.stringify(s);
        if (s.region != null) setRegion(String(s.region));
        if (s.pageSize != null) setPageSize(String(s.pageSize));
        if (s.sort != null) setSort(String(s.sort));
        if (typeof s.page === 'number' && s.page >= 0) setPage(s.page);

        const rawResp = sessionStorage.getItem(RESPONSE_KEY);
        if (rawResp) {
          const r = JSON.parse(rawResp);
          const withinTtl = r?.at && Date.now() - r.at <= RESPONSE_TTL_MS;
          const sameSignature = r?.signature && r.signature === signature;
          if (withinTtl && sameSignature) {
            setResults(r?.results || []);
            setTotalPages(r?.totalPages ?? 0);
            setTotalElements(r?.totalElements ?? 0);
            if (typeof r?.page === 'number' && r.page >= 0) setPage(r.page);
            setIsSearched(true);
          }
        }
      }
    } catch (e) {
      /* 저장 포맷이 바뀌었을 때는 무시 */
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onSearch = async (targetPage = 0) => {
    setError('');
    setIsSearched(true);
    setLoading(true);
    try {
      const params = {
        region: region || null,
        gender: null,
        minYearsOfExperience: null,
        page: targetPage,
        size: pageSize ? Number(pageSize) : 10,
        sort,
      };
      const res = await searchSittersPage(params);
      setResults(res?.content || []);
      setPage(res?.number ?? targetPage);
      setTotalPages(res?.totalPages ?? 0);
      setTotalElements(res?.totalElements ?? 0);
      onToast?.({
        type: 'success',
        title: '탐색 완료',
        message: `총 ${res?.totalElements ?? 0}건 중 현재 ${(res?.content || []).length}건을 불러왔습니다.`,
      });

      const nextState = { region, pageSize, sort, page: targetPage };
      try {
        const signature = JSON.stringify(nextState);
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(nextState));
        const nextResponse = {
          at: Date.now(),
          signature,
          results: res?.content || [],
          page: res?.number ?? targetPage,
          totalPages: res?.totalPages ?? 0,
          totalElements: res?.totalElements ?? 0,
        };
        sessionStorage.setItem(RESPONSE_KEY, JSON.stringify(nextResponse));
      } catch (_) {
        /* 저장 실패는 무시 */
      }
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '시터 탐색 중 오류가 발생했습니다.';
      setError(msg);
      setResults([]);
      onToast?.({ type: 'error', title: '탐색 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  if (!email) {
    return (
      <div className="basisi-search">
        <div className="bs-shell">
          <div className="bs-header">
            <h1 className="bs-title">시터 탐색</h1>
            <p className="bs-subtitle">로그인 후 이용할 수 있습니다.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="basisi-search">
      <div className="bs-shell">
        {/* 페이지 헤더 */}
        <div className="bs-header">
          <h1 className="bs-title">시터 탐색</h1>
          <p className="bs-subtitle">지역/정렬/페이지 크기로 시터를 탐색합니다.</p>
        </div>

        {role !== 'PARENT' ? (
          <div className="bs-role-hint-wrap">
            <span className="bs-role-hint">
              부모(PARENT) 계정 기준 탐색/예약 흐름을 권장합니다. (시터 계정도 조회 가능)
            </span>
          </div>
        ) : null}

        {/* 검색 폼 */}
        <div className="bs-form">
          <div className="bs-form-row bs-form-row-full">
            <TextInput
              label="지역"
              value={region}
              onChange={setRegion}
              placeholder="예: 서울, 강남구"
            />
          </div>

          <div className="bs-form-row bs-form-row-2">
            <Select label="정렬" value={sort} onChange={setSort} options={SORT_OPTIONS} />
            <Select
              label="페이지 크기"
              value={pageSize}
              onChange={setPageSize}
              options={PAGE_SIZE_OPTIONS}
            />
          </div>

          <div className="bs-form-cta">
            <button
              type="button"
              className="btn primary lg"
              onClick={() => onSearch(0)}
              disabled={loading}
            >
              {loading ? '탐색중...' : '🔍 시터 탐색'}
            </button>
            {isSearched ? (
              <button
                type="button"
                className="bs-back-ai"
                onClick={() => {
                  setIsSearched(false);
                  setError('');
                }}
                disabled={loading}
                aria-label="AI 추천 시터 보기로 돌아가기"
              >
                <span aria-hidden>✨</span>
                AI 추천 시터
              </button>
            ) : null}
          </div>

          {error ? <div className="error">{error}</div> : null}
        </div>

        <div className="divider" />

        {isSearched ? (
          <>
            <div className="bs-result-head">
              <h2 className="bs-result-title">탐색 결과</h2>
              <span className="badge">총 {totalElements}건</span>
              <span className="badge">
                페이지 {totalPages === 0 ? 0 : page + 1}/{totalPages}
              </span>
            </div>

            {loading ? (
              <div className="bs-list">
                {Array.from({ length: 3 }).map((_, idx) => (
                  <div className="skeletonCard" key={idx}>
                    <div className="skeletonLine" style={{ width: '55%', marginBottom: 10 }} />
                    <div className="skeletonLine" style={{ width: '80%', marginBottom: 8 }} />
                    <div className="skeletonLine" style={{ width: '65%', marginBottom: 8 }} />
                    <div className="skeletonLine" style={{ width: '75%' }} />
                  </div>
                ))}
              </div>
            ) : results.length === 0 ? (
              <div className="emptyState">아직 결과가 없습니다. 위 조건으로 탐색해보세요.</div>
            ) : (
              <div className="bs-list">
                {results.map((r) => (
                  <SitterResultCard
                    key={r.sitterProfileId}
                    sitter={r}
                    onPickSitter={(picked) => {
                      onPickSitter?.(picked);
                      onToast?.({
                        type: 'success',
                        title: '선택됨',
                        message: `예약 대상 시터(ID ${picked.sitterProfileId})를 선택했습니다.`,
                      });
                    }}
                    onViewDetail={(id) => {
                      onViewDetail?.(id);
                      onToast?.({
                        type: 'success',
                        title: '상세 보기',
                        message: `시터(ID ${id}) 상세로 이동합니다.`,
                      });
                    }}
                  />
                ))}
              </div>
            )}

            {!loading ? (
              <div className="bs-pager">
                <button
                  type="button"
                  className="btn"
                  disabled={page <= 0}
                  onClick={() => onSearch(page - 1)}
                >
                  ← 이전 페이지
                </button>
                <button
                  type="button"
                  className="btn"
                  disabled={totalPages === 0 || page >= totalPages - 1}
                  onClick={() => onSearch(page + 1)}
                >
                  다음 페이지 →
                </button>
              </div>
            ) : null}
          </>
        ) : (
          <AiRecommendedSittersPreview
            onPickSitter={onPickSitter}
            onViewDetail={onViewDetail}
            onToast={onToast}
            isParent={role === 'PARENT'}
          />
        )}
      </div>
    </div>
  );
}

// 초기 진입 시 노출되는 AI 추천 시터 영역입니다.
function AiRecommendedSittersPreview({ onPickSitter, onViewDetail, onToast, isParent }) {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isParent) return;
    let cancelled = false;
    setLoading(true);
    setError('');
    postSitterRecommend({ useMyProfile: true, limit: 4 })
      .then((res) => {
        if (cancelled) return;
        setData(res);
      })
      .catch((e) => {
        if (cancelled) return;
        setError(e?.message || 'AI 추천을 불러오지 못했습니다.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [isParent]);

  const retry = () => {
    setError('');
    setLoading(true);
    postSitterRecommend({ useMyProfile: true, limit: 4 })
      .then((res) => setData(res))
      .catch((e) => setError(e?.message || 'AI 추천을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  };

  const isFallback = data?.matchMode === 'FALLBACK_TOP_SCORE';
  const items = Array.isArray(data?.items) ? data.items : [];

  return (
    <section className="bs-ai-section" aria-label="AI 추천 시터">
      <div>
        <div className="bs-ai-head">
          <span aria-hidden>✨</span>
          <span>AI 추천 시터</span>
          <span className="badge">BETA</span>
          {isFallback ? (
            <span className="badge is-info">차선책(불꽃 점수 순)</span>
          ) : null}
        </div>
        <p className="bs-ai-sub">
          {data?.summary ||
            '마이페이지에 등록된 부모·아이 프로필을 기반으로 AI가 어울리는 시터를 골라드려요.'}
        </p>
        {data?.llmFallback ? (
          <div className="bs-ai-notice">
            AI 분석 연결에 문제가 있어 기본 추천 결과로 보여드릴게요.
          </div>
        ) : null}
      </div>

      <div className="bs-ai-banner">
        <span aria-hidden>🌱</span>
        <span>
          아래는 등록한 프로필 기반의 <strong>추천 미리보기</strong>예요. 위에서 직접 조건을 설정하고{' '}
          <strong>[시터 탐색]</strong>을 누르면 일반 검색 결과로 전환됩니다.
        </span>
      </div>

      {!isParent ? (
        <div className="bs-ai-empty-hint">
          ※ AI 추천 시터는 부모(PARENT) 계정에서 마이페이지 정보를 기반으로 정확하게 동작합니다.
        </div>
      ) : loading ? (
        <div className="bs-list" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <p className="bs-ai-loading-text">AI가 프로필을 분석해 추천 시터를 고르고 있어요...</p>
          {Array.from({ length: 3 }).map((_, idx) => (
            <div className="skeletonCard" key={idx}>
              <div className="skeletonLine" style={{ width: '40%', marginBottom: 10 }} />
              <div className="skeletonLine" style={{ width: '75%', marginBottom: 8 }} />
              <div className="skeletonLine" style={{ width: '60%' }} />
            </div>
          ))}
        </div>
      ) : error ? (
        <div>
          <div className="error">{error}</div>
          <div className="row" style={{ marginTop: 10, justifyContent: 'center' }}>
            <button type="button" className="btn" onClick={retry}>
              다시 시도
            </button>
          </div>
          {String(error).includes('403') || String(error).includes('401') ? (
            <p className="bs-ai-sub" style={{ textAlign: 'center', marginTop: 8 }}>
              백엔드를 코드 반영 후 <strong>재시작</strong>(포트 8080)했는지 확인하고, 그래도 같으면 로그아웃 후 다시 로그인해 주세요.
            </p>
          ) : null}
        </div>
      ) : items.length === 0 ? (
        <div className="emptyState">아직 추천할 시터가 없습니다. 마이페이지의 필수 조건을 채워 보세요.</div>
      ) : (
        <div className="bs-list">
          {items.map((it) => (
            <SitterResultCard
              key={it.sitter?.sitterProfileId ?? it.rank}
              sitter={it.sitter}
              rank={it.rank}
              matchScore={it.matchScore}
              reasons={it.reasons}
              showRank
              showReasons
              onPickSitter={(picked) => {
                onPickSitter?.(picked);
                onToast?.({
                  type: 'success',
                  title: 'AI 추천',
                  message: `${picked.name} 시터를 예약 대상으로 선택했습니다.`,
                });
              }}
              onViewDetail={(id) => {
                onViewDetail?.(id);
                onToast?.({
                  type: 'success',
                  title: '상세 보기',
                  message: `시터(ID ${id}) 상세로 이동합니다.`,
                });
              }}
            />
          ))}
        </div>
      )}

      {data?.disclaimer ? (
        <p className="bs-ai-disclaimer">{data.disclaimer}</p>
      ) : null}
    </section>
  );
}
