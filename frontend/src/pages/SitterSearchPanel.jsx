// 시터 탐색(Phase2) UI를 제공하는 패널입니다.
import React, { useEffect, useMemo, useState } from 'react';
// 공통 입력 컴포넌트입니다.
import { TextInput } from '../components/TextInput.jsx';
// 공통 셀렉트 컴포넌트입니다.
import { Select } from '../components/Select.jsx';
// 결과/AI 추천 카드 공용 컴포넌트입니다.
import { SitterResultCard } from '../components/SitterResultCard.jsx';
// 시터 탐색 API입니다.
import { searchSittersPage } from '../lib/sitterApi.js';
// AI 시터 추천 API입니다.
import { postSitterRecommend } from '../lib/recommendApi.js';
// API 에러 타입입니다.
import { ApiError } from '../lib/api.js';

// 시터 탐색 패널을 렌더링합니다.
export function SitterSearchPanel({ session, onToast, onPickSitter, onViewDetail }) {
  // 로그인 상태를 확인합니다.
  const email = useMemo(() => session?.email || null, [session]);
  // 역할을 확인합니다.
  const role = useMemo(() => session?.role || null, [session]);

  // 검색 조건 상태입니다.
  // 세부 조건(성별/경력 등)은 AI 추천 시터에서 처리하므로
  // 시터 탐색 화면은 지역/정렬/페이지 크기 3개로만 단순화합니다.
  const [region, setRegion] = useState('서울');
  const [pageSize, setPageSize] = useState('10');
  const [sort, setSort] = useState('flameScore,desc');

  // 검색 결과 상태입니다.
  const [results, setResults] = useState([]);
  // 페이지 번호 상태입니다. (0부터 시작)
  const [page, setPage] = useState(0);
  // 전체 페이지 수 상태입니다.
  const [totalPages, setTotalPages] = useState(0);
  // 전체 건수 상태입니다.
  const [totalElements, setTotalElements] = useState(0);
  // 로딩 상태입니다.
  const [loading, setLoading] = useState(false);
  // 에러 메시지 상태입니다.
  const [error, setError] = useState('');
  // 탐색 버튼을 한 번이라도 눌렀는지 여부입니다.
  // false: 초기 상태(AI 추천 시터 노출) / true: 탐색 결과 노출
  const [isSearched, setIsSearched] = useState(false);

  // 라우팅 이동/새로고침 시에도 검색 조건을 복원합니다.
  const SESSION_KEY = 'basisi.sitterSearchState';
  const RESPONSE_KEY = 'basisi.sitterSearchLastResponse';
  const RESPONSE_TTL_MS = 2 * 60 * 1000; // 2분 캐시
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

        // 최근 조회 결과를 즉시 복원합니다.
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
            // 세션 캐시가 복원되면 사용자는 이전에 이미 탐색을 실행한 것이므로
            // 자연스럽게 탐색 결과 영역을 다시 표시합니다.
            setIsSearched(true);
          }
        }
      }
    } catch (e) {
      // 저장 포맷이 바뀌었을 때는 무시합니다.
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 검색을 실행합니다.
  const onSearch = async (targetPage = 0) => {
    // 메시지를 초기화합니다.
    setError('');
    // 탐색 결과 영역을 노출합니다(AI 추천 영역은 숨김 전환).
    setIsSearched(true);
    // 로딩을 시작합니다.
    setLoading(true);
    try {
      // 입력값을 신규 페이지 API 파라미터로 변환합니다.
      // 세부 조건(성별/경력)은 AI 추천 시터에서 다루므로 이곳에서는 보내지 않습니다.
      const params = {
        region: region || null,
        gender: null,
        minYearsOfExperience: null,
        page: targetPage,
        size: pageSize ? Number(pageSize) : 10,
        sort
      };
      // 페이징 탐색 API를 호출합니다.
      const res = await searchSittersPage(params);
      // 결과를 저장합니다.
      setResults(res?.content || []);
      // 페이지 정보를 저장합니다.
      setPage(res?.number ?? targetPage);
      setTotalPages(res?.totalPages ?? 0);
      setTotalElements(res?.totalElements ?? 0);
      // 토스트를 표시합니다.
      onToast?.({
        type: 'success',
        title: '탐색 완료',
        message: `총 ${res?.totalElements ?? 0}건 중 현재 ${(res?.content || []).length}건을 불러왔습니다.`
      });

      // 성공적으로 조회한 조건을 저장합니다.
      const nextState = {
        region,
        pageSize,
        sort,
        page: targetPage
      };
      try {
        const signature = JSON.stringify(nextState);
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(nextState));
        // 최근 조회 결과도 같이 캐싱합니다(새로고침 시 즉시 복원).
        const nextResponse = {
          at: Date.now(),
          signature,
          results: res?.content || [],
          page: res?.number ?? targetPage,
          totalPages: res?.totalPages ?? 0,
          totalElements: res?.totalElements ?? 0
        };
        sessionStorage.setItem(RESPONSE_KEY, JSON.stringify(nextResponse));
      } catch (_) {
        // 저장 실패는 무시합니다.
      }
    } catch (e) {
      // 에러 메시지를 설정합니다.
      const msg = e instanceof ApiError ? e.message : '시터 탐색 중 오류가 발생했습니다.';
      setError(msg);
      // 실패 시 현재 페이지 결과를 초기화합니다.
      setResults([]);
      // 토스트를 표시합니다.
      onToast?.({ type: 'error', title: '탐색 실패', message: msg });
    } finally {
      // 로딩을 종료합니다.
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 10 }}>시터 탐색</div>
      <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
        - 지역/성별/경력/정렬로 시터를 탐색합니다.
      </div>

      {!email ? (
        <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>로그인 후 이용할 수 있습니다.</div>
      ) : (
        <>
          {role !== 'PARENT' ? (
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
              부모(PARENT) 계정 기준 탐색/예약 흐름을 권장합니다. (시터 계정도 조회는 가능)
            </div>
          ) : null}

          <TextInput label="지역" value={region} onChange={setRegion} placeholder="예: 서울, 강남구" />
          <div className="row">
            <div style={{ flex: 1, minWidth: 180 }}>
              <Select
                label="정렬"
                value={sort}
                onChange={setSort}
                options={[
                  { value: 'flameScore,desc', label: '불꽃 점수 높은 순' },
                  { value: 'yearsOfExperience,desc', label: '경력 높은 순' },
                  { value: 'yearsOfExperience,asc', label: '경력 낮은 순' },
                  { value: 'age,asc', label: '나이 어린 순' },
                  { value: 'age,desc', label: '나이 많은 순' },
                  { value: 'createdAt,desc', label: '최신 등록 순' }
                ]}
              />
            </div>
            <div style={{ flex: 1, minWidth: 180 }}>
              <Select
                label="페이지 크기"
                value={pageSize}
                onChange={setPageSize}
                options={[
                  { value: '5', label: '5개' },
                  { value: '10', label: '10개' },
                  { value: '20', label: '20개' }
                ]}
              />
            </div>
          </div>

          <div className="row" style={{ alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
            <button className="btn primary" onClick={() => onSearch(0)} disabled={loading}>
              {loading ? '탐색중...' : '시터 탐색'}
            </button>
            {isSearched ? (
              <button
                type="button"
                onClick={() => {
                  setIsSearched(false);
                  setError('');
                }}
                disabled={loading}
                aria-label="AI 추천 시터 보기로 돌아가기"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 6,
                  padding: '8px 16px',
                  borderRadius: 999,
                  border: '1px solid #7dd3fc',
                  background: 'linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%)',
                  color: '#0c4a6e',
                  fontWeight: 700,
                  fontSize: 13,
                  cursor: loading ? 'not-allowed' : 'pointer',
                  boxShadow: '0 1px 2px rgba(14, 116, 144, 0.12)'
                }}
              >
                <span aria-hidden>✨</span>
                AI 추천 시터
              </button>
            ) : null}
          </div>

          {error ? <div className="error">{error}</div> : null}

          <div className="divider" />

          {isSearched ? (
            // ===== 시터 탐색 버튼 클릭 후: 탐색 결과 영역 =====
            <>
              <div style={{ fontWeight: 800, marginBottom: 10 }}>
                탐색 결과 <span className="badge">총 {totalElements}건</span>{' '}
                <span className="badge">
                  페이지 {totalPages === 0 ? 0 : page + 1}/{totalPages}
                </span>
              </div>

              {loading ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
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
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {results.map((r) => (
                    <SitterResultCard
                      key={r.sitterProfileId}
                      sitter={r}
                      onPickSitter={(picked) => {
                        onPickSitter?.(picked);
                        onToast?.({ type: 'success', title: '선택됨', message: `예약 대상 시터(ID ${picked.sitterProfileId})를 선택했습니다.` });
                      }}
                      onViewDetail={(id) => {
                        onViewDetail?.(id);
                        onToast?.({ type: 'success', title: '상세 보기', message: `시터(ID ${id}) 상세로 이동합니다.` });
                      }}
                    />
                  ))}
                </div>
              )}

              {!loading ? (
                <div className="row" style={{ marginTop: 12 }}>
                  <button className="btn" disabled={page <= 0} onClick={() => onSearch(page - 1)}>
                    이전 페이지
                  </button>
                  <button className="btn" disabled={totalPages === 0 || page >= totalPages - 1} onClick={() => onSearch(page + 1)}>
                    다음 페이지
                  </button>
                </div>
              ) : null}
            </>
          ) : (
            // ===== 초기 상태(탐색 전): AI 추천 시터 영역 =====
            <AiRecommendedSittersPreview
              onPickSitter={onPickSitter}
              onViewDetail={onViewDetail}
              onToast={onToast}
              isParent={role === 'PARENT'}
            />
          )}
        </>
      )}
    </div>
  );
}

// 초기 진입 시 노출되는 AI 추천 시터 영역입니다.
// 백엔드(/api/recommendations/sitters)를 호출하여 1순위 매칭 또는 차선책(불꽃 점수 순) 결과를 받아 카드로 렌더링합니다.
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

  const isFallback = data?.matchMode === 'FALLBACK_TOP_SCORE';
  const items = Array.isArray(data?.items) ? data.items : [];

  return (
    <section aria-label="AI 추천 시터">
      <div style={{ marginBottom: 8 }}>
        <div style={{ fontWeight: 800, fontSize: 16 }}>
          <span aria-hidden style={{ marginRight: 6 }}>✨</span>
          AI 추천 시터
          <span className="badge" style={{ marginLeft: 8 }}>BETA</span>
          {isFallback ? (
            <span
              className="badge"
              style={{ marginLeft: 8, background: 'rgba(56, 189, 248, 0.18)', color: '#0c4a6e' }}
            >
              차선책(불꽃 점수 순)
            </span>
          ) : null}
        </div>
        <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginTop: 4 }}>
          {data?.summary || '마이페이지에 등록된 부모·아이 프로필을 기반으로 AI가 어울리는 시터를 골라드려요.'}
        </div>
      </div>

      <div
        style={{
          border: '1px dashed rgba(199, 61, 106, 0.32)',
          borderRadius: 14,
          padding: 12,
          background: 'linear-gradient(135deg, rgba(255, 246, 250, 0.9) 0%, rgba(255, 252, 246, 0.9) 100%)',
          marginBottom: 12,
          fontSize: 12,
          color: 'rgba(26,21,35,0.66)',
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          flexWrap: 'wrap'
        }}
      >
        <span aria-hidden>🌱</span>
        <span>
          아래는 등록한 프로필 기반의 <strong>추천 미리보기</strong>예요. 위에서 직접 조건을 설정하고{' '}
          <strong>[시터 탐색]</strong>을 누르면 일반 검색 결과로 전환됩니다.
        </span>
      </div>

      {!isParent ? (
        <div style={{ textAlign: 'center', fontSize: 12, color: 'rgba(26,21,35,0.58)', padding: '24px 0' }}>
          ※ AI 추천 시터는 부모(PARENT) 계정에서 마이페이지 정보를 기반으로 정확하게 동작합니다.
        </div>
      ) : loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
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
          <div className="row" style={{ marginTop: 10 }}>
            <button
              type="button"
              className="btn"
              onClick={() => {
                setError('');
                setLoading(true);
                postSitterRecommend({ useMyProfile: true, limit: 4 })
                  .then((res) => setData(res))
                  .catch((e) => setError(e?.message || 'AI 추천을 불러오지 못했습니다.'))
                  .finally(() => setLoading(false));
              }}
            >
              다시 시도
            </button>
          </div>
          {String(error).includes('403') || String(error).includes('401') ? (
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginTop: 8 }}>
              백엔드를 코드 반영 후 <strong>재시작</strong>(포트 8080)했는지 확인하고, 그래도 같으면 로그아웃 후 다시 로그인해 주세요.
            </div>
          ) : null}
        </div>
      ) : items.length === 0 ? (
        <div className="emptyState">아직 추천할 시터가 없습니다. 마이페이지의 필수 조건을 채워 보세요.</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
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
                  message: `${picked.name} 시터를 예약 대상으로 선택했습니다.`
                });
              }}
              onViewDetail={(id) => {
                onViewDetail?.(id);
                onToast?.({
                  type: 'success',
                  title: '상세 보기',
                  message: `시터(ID ${id}) 상세로 이동합니다.`
                });
              }}
            />
          ))}
        </div>
      )}

      {data?.disclaimer ? (
        <div style={{ marginTop: 10, fontSize: 11, color: 'rgba(26,21,35,0.48)', textAlign: 'center' }}>
          {data.disclaimer}
        </div>
      ) : null}
    </section>
  );
}

