// AI ?? ?? ?? ??
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { SitterResultCard } from '../components/SitterResultCard.jsx';
import { ApiError } from '../lib/api.js';
import { getMyParentProfile } from '../lib/profileApi.js';
import { postSitterRecommend } from '../lib/recommendApi.js';

const QUICK_CHIPS = ['??? ??', '?? ?? ???', 'CCTV ??', '?? 3? ??', '?? ?? ??'];

const WORK_LABEL = {
  DUAL_INCOME: '???',
  HOMEMAKER: '?? ??�??'
};

const SCHEDULE_LABEL = {
  REGULAR: '???? ??',
  SPECIFIC: '??? ??',
  UNDECIDED: '?? ??'
};

function formatRegion(profile) {
  if (!profile) return null;
  if (profile.region && String(profile.region).trim()) return String(profile.region).trim();
  const parts = [profile.regionSido, profile.regionSigungu, profile.regionDong].filter((x) => x && String(x).trim());
  return parts.length ? parts.join(' ') : null;
}

function formatChildLine(child) {
  if (!child) return '';
  const g = child.gender === 'MALE' ? '??' : child.gender === 'FEMALE' ? '??' : '';
  const bd = child.birthDate ? String(child.birthDate).trim() : '';
  if (!bd) return '';
  return g ? `${bd} � ${g} ??` : bd;
}

function profileCompleteness(profile) {
  if (!profile) {
    return { percent: 0, missing: ['??? ??'], isEmpty: true };
  }
  const checks = [
    { key: 'region', ok: !!formatRegion(profile), label: '?? ??' },
    {
      key: 'children',
      ok: Array.isArray(profile.children) && profile.children.some((c) => (c.birthDate || '').trim()),
      label: '?? ??'
    },
    {
      key: 'keywords',
      ok: Array.isArray(profile.expectationKeywords) && profile.expectationKeywords.length > 0,
      label: '??? ?? ???'
    },
    { key: 'schedule', ok: !!profile.scheduleType, label: '?? ??' },
    { key: 'work', ok: !!profile.parentWorkType, label: '?? ?? ??' }
  ];
  const done = checks.filter((c) => c.ok).length;
  const missing = checks.filter((c) => !c.ok).map((c) => c.label);
  return {
    percent: Math.round((done / checks.length) * 100),
    missing,
    isEmpty: done === 0
  };
}

export function AiSitterRecommendPanel({ session, onToast, onPickSitter, onViewDetail }) {
  const email = useMemo(() => session?.email || null, [session]);
  const role = useMemo(() => session?.role || null, [session]);

  const [useMyProfile, setUseMyProfile] = useState(true);
  const [profile, setProfile] = useState(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState('');

  const [additionalRequest, setAdditionalRequest] = useState('');
  const [limit, setLimit] = useState(5);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  const completeness = useMemo(() => profileCompleteness(profile), [profile]);
  const regionText = useMemo(() => formatRegion(profile), [profile]);
  const childLines = useMemo(() => {
    const list = Array.isArray(profile?.children) ? profile.children : [];
    return list.map(formatChildLine).filter(Boolean);
  }, [profile]);

  const loadProfile = useCallback(async () => {
    setProfileError('');
    setProfileLoading(true);
    try {
      const res = await getMyParentProfile();
      setProfile(res || null);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '?? ???? ???? ?????.';
      setProfile(null);
      setProfileError(msg);
    } finally {
      setProfileLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!email || role !== 'PARENT') return;
    loadProfile();
  }, [email, role, loadProfile]);

  const appendChip = (text) => {
    setAdditionalRequest((prev) => {
      const trimmed = prev.trim();
      const next = trimmed ? `${trimmed} ${text}` : text;
      return next.length > 500 ? next.slice(0, 500) : next;
    });
  };

  const canRecommend =
    !loading &&
    (useMyProfile || additionalRequest.trim().length > 0 || (!completeness.isEmpty && completeness.percent > 0));

  const onRecommend = async () => {
    setError('');
    setLoading(true);
    setResult(null);
    try {
      const res = await postSitterRecommend({
        useMyProfile,
        additionalRequest: additionalRequest.trim() || undefined,
        limit: Number(limit) || 5
      });
      setResult(res || null);
      const count = (res?.items || []).length;
      onToast?.({
        type: 'success',
        title: '?? ??',
        message: count > 0 ? `${count}?? ??? ??????.` : '??? ?? ??? ????.'
      });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : 'AI ?? ? ??? ??????.';
      setError(msg);
      onToast?.({ type: 'error', title: '?? ??', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const items = result?.items || [];

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 10 }}>AI ?? ?? ??</div>
      <p style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', margin: '0 0 12px 0' }}>
        ?????? ??? ??? ?? ??? ???? ?? ??? ??? ???.
      </p>

      {!email ? (
        <p style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>??? ? ??? ? ????.</p>
      ) : role !== 'PARENT' ? (
        <div>
          <p style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', margin: '0 0 10px 0' }}>
            AI ?? ?? ??? <strong>??(PARENT)</strong> ?? ?? ?????. ?? ????? ?????(?? ???)?
            ????? ?? ???? ?? ??? ??? ? ???.
          </p>
          <div className="row" style={{ flexWrap: 'wrap', gap: 8 }}>
            <Link to="/profile" className="btn">
              ?????
            </Link>
            <Link to="/search" className="btn primary">
              ?? ??
            </Link>
          </div>
        </div>
      ) : (
        <div>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, fontSize: 13, cursor: 'pointer' }}>
            <input type="checkbox" checked={useMyProfile} onChange={(e) => setUseMyProfile(e.target.checked)} />
            <span>????? ?? ??</span>
          </label>

          <div>
            <div
              style={{
                border: '1px solid rgba(26,21,35,0.12)',
                borderRadius: 14,
                padding: 12,
                marginBottom: 14,
                background: 'rgba(26,21,35,0.03)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                <span style={{ fontWeight: 800 }}>? ?? ??</span>
                {!profileLoading && profile ? (
                  <span className="badge">??? ??? {completeness.percent}%</span>
                ) : null}
              </div>
              <p style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', margin: '4px 0 10px 0' }}>
                AI? ???? ????? ?????.
              </p>

              {profileLoading ? (
                <div className="skeletonCard">
                  <div className="skeletonLine" style={{ width: '40%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '70%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '55%' }} />
                </div>
              ) : profileError ? (
                <div>
                  <div>
                    <div className="error" style={{ marginBottom: 8 }}>
                      {profileError}
                    </div>
                    <button type="button" className="btn" onClick={loadProfile}>
                      ?? ????
                    </button>
                  </div>
                </div>
              ) : (
                <div>
                  {!useMyProfile ? (
                    <p style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', margin: '0 0 8px 0' }}>
                      ????? ??? ?? ????. ?? ????? ?????.
                    </p>
                  ) : null}

                  {useMyProfile && completeness.percent < 100 ? (
                    <div
                      style={{
                        fontSize: 12,
                        padding: 10,
                        borderRadius: 10,
                        marginBottom: 10,
                        background: 'rgba(199, 61, 106, 0.08)',
                        border: '1px solid rgba(199, 61, 106, 0.2)'
                      }}
                    >
                      <p style={{ margin: '0 0 6px 0' }}>
                        ???? ? ??? ??? ?????. ???: {completeness.missing.join(', ')}
                      </p>
                      <Link to="/profile" className="btn" style={{ fontSize: 12 }}>
                        ??? ??? ??
                      </Link>
                    </div>
                  ) : null}

                  <dl style={{ margin: 0, fontSize: 13 }}>
                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>?? ??</dt>
                    <dd style={{ margin: '0 0 10px 0', color: 'rgba(26,21,35,0.72)' }}>{regionText || '?? ???'}</dd>

                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>??</dt>
                    <dd style={{ margin: '0 0 10px 0', color: 'rgba(26,21,35,0.72)' }}>
                      {childLines.length > 0
                        ? childLines.map((line, idx) => (
                            <span key={idx}>
                              {idx > 0 ? <br /> : null}
                              {line}
                            </span>
                          ))
                        : '?? ?? ??'}
                    </dd>

                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>????? ??? ??</dt>
                    <dd style={{ margin: '0 0 10px 0' }}>
                      {Array.isArray(profile?.expectationKeywords) && profile.expectationKeywords.length > 0 ? (
                        <div>
                          {profile.expectationKeywords.map((k) => (
                            <span key={k} className="badge" style={{ marginRight: 6, marginBottom: 4, display: 'inline-block' }}>
                              {k}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span style={{ color: 'rgba(26,21,35,0.58)' }}>?? ??? ??</span>
                      )}
                    </dd>

                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>?? ??</dt>
                    <dd style={{ margin: '0 0 10px 0', color: 'rgba(26,21,35,0.72)' }}>
                      {profile?.scheduleType ? SCHEDULE_LABEL[profile.scheduleType] || profile.scheduleType : '?? ???'}
                    </dd>

                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>?? ?? ??</dt>
                    <dd style={{ margin: '0 0 10px 0', color: 'rgba(26,21,35,0.72)' }}>
                      {profile?.parentWorkType ? WORK_LABEL[profile.parentWorkType] || profile.parentWorkType : '???'}
                    </dd>

                    <dt style={{ fontWeight: 700, marginBottom: 4 }}>?? ?</dt>
                    <dd style={{ margin: 0, color: 'rgba(26,21,35,0.72)', whiteSpace: 'pre-wrap' }}>
                      {profile?.sitterMessage?.trim() ? profile.sitterMessage.trim() : '??? ??? ??'}
                    </dd>
                  </dl>
                </div>
              )}
            </div>
          </div>

          <div style={{ marginBottom: 14 }}>
            <label style={{ display: 'block', fontWeight: 700, marginBottom: 6 }}>???? ? ??? ?? ? (??)</label>
            <p style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', margin: '0 0 8px 0' }}>
              ?) ?? ? ?�? ?? 2~6?, 4? ?, ? ??? ?? ??? ? ???? ????.
            </p>
            <textarea
              style={{ width: '100%', minHeight: 100, borderRadius: 12, padding: 10, fontFamily: 'inherit', boxSizing: 'border-box' }}
              maxLength={500}
              value={additionalRequest}
              onChange={(e) => setAdditionalRequest(e.target.value)}
              placeholder="??? ??? ??? ???? ?????."
            />
            <p style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', textAlign: 'right', margin: '4px 0 8px 0' }}>
              {additionalRequest.length} / 500
            </p>
            <div className="row" style={{ flexWrap: 'wrap', gap: 6 }}>
              {QUICK_CHIPS.map((chip) => (
                <button
                  key={chip}
                  type="button"
                  className="btn"
                  style={{ fontSize: 12, borderRadius: 999, padding: '6px 12px' }}
                  onClick={() => appendChip(chip)}
                >
                  {chip}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: 14 }}>
            <span style={{ fontWeight: 700, marginRight: 12 }}>?? ??</span>
            <label style={{ marginRight: 14, fontSize: 13, cursor: 'pointer' }}>
              <input
                type="radio"
                name="recommendLimit"
                value={5}
                checked={limit === 5}
                onChange={() => setLimit(5)}
                style={{ marginRight: 6 }}
              />
              5?
            </label>
            <label style={{ fontSize: 13, cursor: 'pointer' }}>
              <input
                type="radio"
                name="recommendLimit"
                value={3}
                checked={limit === 3}
                onChange={() => setLimit(3)}
                style={{ marginRight: 6 }}
              />
              3?
            </label>
          </div>

          <div className="row" style={{ marginBottom: 12 }}>
            <button type="button" className="btn primary" onClick={onRecommend} disabled={!canRecommend || loading}>
              {loading ? '?? ?...' : '\u2728 AI \uB9DE\uCDA4 \uCD94\uCC9C \uBC1B\uAE30'}
            </button>
          </div>

          <p style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', margin: '0 0 12px 0' }}>
            AI ??? ?????, ?? ??? ???? ???.
          </p>

          {error ? <div className="error">{error}</div> : null}

          <div className="divider" />

          {loading ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <p style={{ fontSize: 13, color: 'rgba(26,21,35,0.66)', margin: 0 }}>
                AI가 조건을 분석하고 맞춤 추천을 준비하고 있어요. 잠시만 기다려 주세요...
              </p>
              {Array.from({ length: 3 }).map((_, idx) => (
                <div className="skeletonCard" key={idx}>
                  <div className="skeletonLine" style={{ width: '30%', marginBottom: 10 }} />
                  <div className="skeletonLine" style={{ width: '85%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '60%', marginBottom: 8 }} />
                  <div className="skeletonLine" style={{ width: '75%' }} />
                </div>
              ))}
            </div>
          ) : result ? (
            <div>
              <div style={{ fontWeight: 800, marginBottom: 10 }}>
                ?? ?? <span className="badge">{items.length}?</span>
              </div>

              {result.summary ? (
                <div>
                  {result.llmFallback ? (
                    <div
                      style={{
                        padding: 10,
                        borderRadius: 12,
                        marginBottom: 10,
                        background: 'rgba(56, 189, 248, 0.12)',
                        border: '1px solid rgba(56, 189, 248, 0.28)',
                        fontSize: 12,
                        lineHeight: 1.5,
                        color: '#0c4a6e'
                      }}
                    >
                      AI 분석 연결에 문제가 있어 기본 추천 결과로 보여드릴게요.
                    </div>
                  ) : null}
                  <div
                    style={{
                      padding: 12,
                      borderRadius: 12,
                      marginBottom: 12,
                      background: 'rgba(199, 61, 106, 0.08)',
                      border: '1px solid rgba(199, 61, 106, 0.18)',
                      fontSize: 13,
                      lineHeight: 1.5
                    }}
                  >
                    <span style={{ marginRight: 6 }} aria-hidden>
                      {'\u2728'}
                    </span>
                    {result.summary}
                  </div>
                </div>
              ) : null}

              {result.disclaimer ? (
                <p style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', margin: '0 0 12px 0' }}>{result.disclaimer}</p>
              ) : null}

              {items.length === 0 ? (
                <div className="emptyState">
                  ??? ?? ??? ???. ???�?? ??? ????? ?? ??? ???.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {items.map((item) => (
                    <SitterResultCard
                      key={item.sitter?.sitterProfileId ?? item.rank}
                      sitter={item.sitter}
                      rank={item.rank}
                      matchScore={item.matchScore}
                      reasons={item.reasons}
                      showRank
                      showReasons
                      onPickSitter={(s) => {
                        onPickSitter?.(s);
                        onToast?.({
                          type: 'success',
                          title: '???',
                          message: `?? ?? ??(ID ${s.sitterProfileId})? ??????.`
                        });
                      }}
                      onViewDetail={(id) => {
                        onViewDetail?.(id);
                        onToast?.({
                          type: 'success',
                          title: '?? ??',
                          message: `??(ID ${id}) ??? ?????.`
                        });
                      }}
                    />
                  ))}
                </div>
              )}

              <div className="row" style={{ marginTop: 12, flexWrap: 'wrap', gap: 8 }}>
                <button type="button" className="btn" onClick={onRecommend} disabled={loading}>
                  ?? ??
                </button>
                <Link to="/search" className="btn accent">
                  ?? ????
                </Link>
              </div>
            </div>
          ) : (
            <div className="emptyState">??? ??? ??? ? AI ?? ??? ?????.</div>
          )}
        </div>
      )}
    </div>
  );
}

