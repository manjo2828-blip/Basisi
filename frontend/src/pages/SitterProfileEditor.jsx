import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { TextInput } from '../components/TextInput.jsx';
import { Select } from '../components/Select.jsx';
import { ApiError } from '../lib/api.js';
import {
  deleteMySitterProfile,
  deleteSitterProfileImage,
  getMySitterProfile,
  getSitterProfileImageUrl,
  upsertMySitterProfile,
  uploadSitterProfileImage
} from '../lib/profileApi.js';
import { SITTER_ACTIVITY_KEYWORDS } from '../data/sitterActivityKeywords.js';
import { SITTER_AGE_OPTIONS } from '../data/sitterAgeOptions.js';
import { SIDO_LIST, listDong, listSigungu } from '../data/koreaRegions.js';

const TABS = [
  { id: 'BASIC', label: '기본 정보' },
  { id: 'NAT', label: '국적' },
  { id: 'ACT', label: '가능 활동' },
  { id: 'WAGE', label: '희망 시급' },
  { id: 'CCTV', label: 'CCTV' },
  { id: 'REG', label: '희망 지역' },
  { id: 'AGE', label: '선호 연령' },
  { id: 'IMG', label: '프로필 사진' },
  { id: 'BIO', label: '자기소개' }
];

function emptyRegion() {
  return { sido: '', sigungu: '', dong: '' };
}

function applyServer(setters, res) {
  const {
    setPhone,
    setBio,
    setAge,
    setGender,
    setYears,
    setHasCert,
    setRegion,
    setNationality,
    setActivities,
    setWage,
    setHourlyNegotiable,
    setCctv,
    setPreferredRegions,
    setAgeGroups,
    setPhotoIds
  } = setters;
  setPhone(res?.phoneNumber ?? '');
  setBio(res?.bio ?? '');
  setAge(res?.age != null ? String(res.age) : '');
  setGender(res?.gender ?? 'FEMALE');
  setYears(res?.yearsOfExperience != null ? String(res.yearsOfExperience) : '');
  setHasCert(res?.hasCertificate === true ? 'YES' : res?.hasCertificate === false ? 'NO' : 'NO');
  setRegion(res?.region ?? '');
  setNationality(res?.nationalityType ?? '');
  setActivities(Array.isArray(res?.availableActivities) ? [...res.availableActivities] : []);
  setWage(res?.childcareHourlyWage != null ? String(res.childcareHourlyWage) : '');
  setHourlyNegotiable(!!res?.hourlyNegotiable);
  if (res?.cctvConsent === true) setCctv('OK');
  else if (res?.cctvConsent === false) setCctv('NO');
  else setCctv('');
  const pr = Array.isArray(res?.preferredRegions) ? res.preferredRegions : [];
  setPreferredRegions(pr.length ? pr.map((r) => ({ sido: r.sido ?? '', sigungu: r.sigungu ?? '', dong: r.dong ?? '' })) : [emptyRegion()]);
  setAgeGroups(Array.isArray(res?.preferredAgeGroups) ? res.preferredAgeGroups.map((a) => (typeof a === 'string' ? a : a?.name || '')).filter(Boolean) : []);
  setPhotoIds(Array.isArray(res?.profilePhotoIds) ? [...res.profilePhotoIds] : []);
}

export function SitterProfileEditor({ onToast, onAuthChanged, onSaved }) {
  const [tab, setTab] = useState('BASIC');
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState('');

  const [phone, setPhone] = useState('');
  const [bio, setBio] = useState('');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('FEMALE');
  const [years, setYears] = useState('');
  const [hasCert, setHasCert] = useState('NO');
  const [region, setRegion] = useState('');

  const [nationality, setNationality] = useState('');
  const [activities, setActivities] = useState([]);
  const [wage, setWage] = useState('');
  const [hourlyNegotiable, setHourlyNegotiable] = useState(false);
  const [cctv, setCctv] = useState('');
  const [preferredRegions, setPreferredRegions] = useState([emptyRegion()]);
  const [ageGroups, setAgeGroups] = useState([]);
  const [photoIds, setPhotoIds] = useState([]);

  const reload = useCallback(async () => {
    setLoadError('');
    try {
      const res = await getMySitterProfile();
      applyServer(
        {
          setPhone,
          setBio,
          setAge,
          setGender,
          setYears,
          setHasCert,
          setRegion,
          setNationality,
          setActivities,
          setWage,
          setHourlyNegotiable,
          setCctv,
          setPreferredRegions,
          setAgeGroups,
          setPhotoIds
        },
        res
      );
    } catch (e) {
      if (e instanceof ApiError && (e.message || '').includes('시터 프로필이 존재하지 않습니다')) {
        setLoadError('');
        return;
      }
      if (e instanceof ApiError) setLoadError(e.message);
      else setLoadError('프로필을 불러오지 못했습니다.');
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const buildPayload = useCallback(() => {
    const regions = preferredRegions
      .filter((r) => (r.sido || '').trim() && (r.sigungu || '').trim() && (r.dong || '').trim())
      .map((r) => ({ sido: r.sido.trim(), sigungu: r.sigungu.trim(), dong: r.dong.trim() }));
    const wageNum = wage.trim() ? Number(wage) : null;
    let cctvVal = null;
    if (cctv === 'OK') cctvVal = true;
    if (cctv === 'NO') cctvVal = false;
    return {
      phoneNumber: phone.trim(),
      age: Number(age),
      gender,
      yearsOfExperience: Number(years),
      hasCertificate: hasCert === 'YES',
      region: region.trim(),
      bio: (bio || '').trim() || null,
      nationalityType: nationality || null,
      availableActivities: activities,
      childcareHourlyWage: Number.isFinite(wageNum) && wageNum > 0 ? wageNum : null,
      hourlyNegotiable,
      cctvConsent: cctvVal,
      preferredRegions: regions,
      preferredAgeGroups: ageGroups,
      profilePhotoIds: photoIds
    };
  }, [phone, bio, age, gender, years, hasCert, region, nationality, activities, wage, hourlyNegotiable, cctv, preferredRegions, ageGroups, photoIds]);

  const validateForTab = (tabId) => {
    if (!phone.trim()) return '전화번호를 입력해주세요. (기본 정보 탭)';
    if (!age.trim() || Number(age) < 0) return '나이를 입력해주세요.';
    if (!years.trim() || Number(years) < 0) return '경력(년)을 입력해주세요.';
    if (!region.trim()) return '거주 지역을 입력해주세요.';
    if (tabId === 'NAT' && !nationality) return '국적을 선택해주세요.';
    if (tabId === 'ACT' && activities.length === 0) return '가능한 활동을 한 가지 이상 선택해주세요.';
    if (tabId === 'WAGE') {
      if (!hourlyNegotiable && (!wage.trim() || Number(wage) < 1)) {
        return '희망 시급을 입력하거나, 협의 가능을 체크해주세요.';
      }
      if (hourlyNegotiable && wage.trim() && Number(wage) < 1) return '시급은 1원 이상이어야 합니다.';
    }
    if (tabId === 'CCTV' && cctv !== 'OK' && cctv !== 'NO') return 'CCTV 동의 여부를 선택해주세요.';
    if (tabId === 'REG') {
      const partial = preferredRegions.some(
        (r) =>
          ((r.sido || '').trim() || (r.sigungu || '').trim() || (r.dong || '').trim()) &&
          !((r.sido || '').trim() && (r.sigungu || '').trim() && (r.dong || '').trim())
      );
      if (partial) return '희망 지역은 시·도 / 시·군·구 / 동·읍·면을 모두 선택해주세요.';
      const filled = preferredRegions.filter((r) => (r.sido || '').trim() && (r.sigungu || '').trim() && (r.dong || '').trim());
      if (filled.length > 3) return '희망 지역은 최대 3곳까지 선택할 수 있습니다.';
    }
    if (tabId === 'AGE' && ageGroups.length === 0) return '선호하는 아이 연령대를 한 가지 이상 선택해주세요.';
    if (tabId === 'BIO' && bio.length > 2000) return '자기소개는 2000자 이하여야 합니다.';
    return '';
  };

  const commit = async () => {
    const err = validateForTab(tab);
    if (err) {
      onToast?.({ type: 'error', title: '확인', message: err });
      return;
    }
    setLoading(true);
    try {
      const res = await upsertMySitterProfile(buildPayload());
      applyServer(
        {
          setPhone,
          setBio,
          setAge,
          setGender,
          setYears,
          setHasCert,
          setRegion,
          setNationality,
          setActivities,
          setWage,
          setHourlyNegotiable,
          setCctv,
          setPreferredRegions,
          setAgeGroups,
          setPhotoIds
        },
        res
      );
      onAuthChanged?.();
      onSaved?.();
      onToast?.({ type: 'success', title: '저장', message: '시터 프로필이 저장되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '저장 중 오류가 발생했습니다.';
      onToast?.({ type: 'error', title: '저장 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const onDelete = async () => {
    if (!window.confirm('시터 프로필을 정말 삭제할까요?')) return;
    setLoading(true);
    try {
      await deleteMySitterProfile();
      setPhone('');
      setBio('');
      setAge('');
      setGender('FEMALE');
      setYears('');
      setHasCert('NO');
      setRegion('');
      setNationality('');
      setActivities([]);
      setWage('');
      setHourlyNegotiable(false);
      setCctv('');
      setPreferredRegions([emptyRegion()]);
      setAgeGroups([]);
      setPhotoIds([]);
      onAuthChanged?.();
      onSaved?.();
      onToast?.({ type: 'success', title: '삭제', message: '시터 프로필이 삭제되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '삭제 중 오류가 발생했습니다.';
      onToast?.({ type: 'error', title: '삭제 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const toggleActivity = (k) => {
    setActivities((prev) => (prev.includes(k) ? prev.filter((x) => x !== k) : [...prev, k]));
  };

  const toggleAge = (id) => {
    setAgeGroups((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const updateRegionRow = (idx, patch) => {
    setPreferredRegions((prev) => prev.map((r, i) => (i === idx ? { ...r, ...patch } : r)));
  };

  const onSidoRow = (idx, v) => {
    updateRegionRow(idx, { sido: v, sigungu: '', dong: '' });
  };

  const onSigunguRow = (idx, v) => {
    updateRegionRow(idx, { sigungu: v, dong: '' });
  };

  const addRegionRow = () => {
    const filled = preferredRegions.filter((r) => (r.sido || '').trim() && (r.sigungu || '').trim() && (r.dong || '').trim());
    if (filled.length >= 3) {
      onToast?.({ type: 'error', title: '제한', message: '희망 지역은 최대 3곳까지입니다.' });
      return;
    }
    setPreferredRegions((prev) => [...prev, emptyRegion()]);
  };

  const removeRegionRow = (idx) => {
    setPreferredRegions((prev) => (prev.length <= 1 ? [emptyRegion()] : prev.filter((_, i) => i !== idx)));
  };

  const rowSigunguOptions = (sido) => {
    if (!sido) return [{ value: '', label: '시·도 먼저' }];
    const list = listSigungu(sido);
    return [{ value: '', label: '시·군·구' }, ...list.map((s) => ({ value: s, label: s }))];
  };

  const rowDongOptions = (sido, sigungu) => {
    if (!sido || !sigungu) return [{ value: '', label: '시·군·구 먼저' }];
    const list = listDong(sido, sigungu);
    return [{ value: '', label: '동·읍·면' }, ...list.map((s) => ({ value: s, label: s }))];
  };

  const sidoOpts = useMemo(
    () => [{ value: '', label: '시·도' }, ...SIDO_LIST.map((s) => ({ value: s, label: s }))],
    []
  );

  const onPickPhotos = async (e) => {
    const files = e.target.files;
    if (!files?.length) return;
    e.target.value = '';
    for (const file of Array.from(files)) {
      if (photoIds.length >= 5) {
        onToast?.({ type: 'error', title: '제한', message: '사진은 최대 5장입니다.' });
        break;
      }
      try {
        const res = await uploadSitterProfileImage(file);
        if (res?.id) setPhotoIds((p) => [...p, res.id]);
      } catch (err) {
        const msg = err instanceof ApiError ? err.message : '업로드 실패';
        onToast?.({ type: 'error', title: '업로드', message: msg });
      }
    }
  };

  const removePhoto = async (id) => {
    try {
      await deleteSitterProfileImage(id);
    } catch (err) {
      // 서버에 없어도 UI에서 제거
    }
    setPhotoIds((p) => p.filter((x) => x !== id));
  };

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 10 }}>시터 프로필</div>
      <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
        탭별로 입력 후 <strong>저장</strong>을 누르면 서버에 반영됩니다.
      </div>

      {loadError ? <div className="error">{loadError}</div> : null}

      <div className="row" style={{ flexWrap: 'wrap', gap: 8, marginBottom: 14 }}>
        {TABS.map((t) => (
          <button key={t.id} type="button" className={`btn ${tab === t.id ? 'primary' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
        <button type="button" className="btn" onClick={reload} disabled={loading}>
          새로고침
        </button>
      </div>

      <div className="divider" />

      {tab === 'BASIC' ? (
        <div>
          <TextInput label="전화번호" value={phone} onChange={setPhone} placeholder="010-0000-0000" />
          <TextInput label="나이" value={age} onChange={setAge} placeholder="예: 25" />
          <Select
            label="성별"
            value={gender}
            onChange={setGender}
            options={[
              { value: 'FEMALE', label: '여' },
              { value: 'MALE', label: '남' }
            ]}
          />
          <TextInput label="경력(년)" value={years} onChange={setYears} placeholder="예: 3" />
          <Select
            label="자격증 유무"
            value={hasCert}
            onChange={setHasCert}
            options={[
              { value: 'YES', label: '유' },
              { value: 'NO', label: '무' }
            ]}
          />
          <TextInput label="거주 지역" value={region} onChange={setRegion} placeholder="예: 서울 강남구" />
        </div>
      ) : null}

      {tab === 'NAT' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>국적을 선택해주세요</div>
          <div className="row" style={{ flexWrap: 'wrap', gap: 10 }}>
            <button type="button" className={`btn ${nationality === 'KOREAN' ? 'primary' : ''}`} onClick={() => setNationality('KOREAN')}>
              내국인이에요
            </button>
            <button type="button" className={`btn ${nationality === 'FOREIGNER' ? 'primary' : ''}`} onClick={() => setNationality('FOREIGNER')}>
              외국인이에요
            </button>
          </div>
        </div>
      ) : null}

      {tab === 'ACT' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>가능한 활동을 선택해주세요</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 10 }}>
            Tip: 가능한 활동이 많을수록 더 높은 시급을 설정할 수 있어요(서비스 정책에 따름).
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {SITTER_ACTIVITY_KEYWORDS.map((k) => {
              const on = activities.includes(k);
              return (
                <button key={k} type="button" className={`btn ${on ? 'primary' : ''}`} style={{ fontSize: 12, borderRadius: 999, padding: '6px 12px' }} onClick={() => toggleActivity(k)}>
                  {k}
                </button>
              );
            })}
          </div>
        </div>
      ) : null}

      {tab === 'WAGE' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>보육·돌봄 희망 시급</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 10 }}>보육·돌봄 분야 기준 시급(원)입니다.</div>
          <div className="field">
            <label>시급 (원)</label>
            <input
              type="number"
              min={0}
              value={wage}
              onChange={(e) => setWage(e.target.value)}
              placeholder="예: 15000"
              disabled={hourlyNegotiable}
              style={{ width: '100%', borderRadius: 12, padding: 10 }}
            />
          </div>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 12, cursor: 'pointer' }}>
            <input type="checkbox" checked={hourlyNegotiable} onChange={(e) => setHourlyNegotiable(e.target.checked)} />
            <span>협의 가능</span>
          </label>
        </div>
      ) : null}

      {tab === 'CCTV' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 12 }}>CCTV 촬영에 동의하시나요?</div>
          <button
            type="button"
            className={`btn ${cctv === 'OK' ? 'primary' : ''}`}
            style={{ width: '100%', textAlign: 'left', padding: 14, marginBottom: 10 }}
            onClick={() => setCctv('OK')}
          >
            <div style={{ fontWeight: 800 }}>CCTV가 있어도 괜찮아요</div>
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>(대부분의 맘시터가 선택했어요)</div>
          </button>
          <button type="button" className={`btn ${cctv === 'NO' ? 'primary' : ''}`} style={{ width: '100%', textAlign: 'left', padding: 14 }} onClick={() => setCctv('NO')}>
            <div style={{ fontWeight: 800 }}>CCTV 촬영을 원하지 않아요</div>
          </button>
          <div style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', marginTop: 12 }}>
            맘시터님의 동의 없이 CCTV를 통해 수집된 영상·음성 정보를 제3자에게 제공할 수 없습니다.
          </div>
        </div>
      ) : null}

      {tab === 'REG' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>활동 희망 지역 (최대 3곳)</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>마이페이지에서 언제든 수정할 수 있어요.</div>
          {preferredRegions.map((r, idx) => (
            <div
              key={idx}
              style={{
                border: '1px solid rgba(199, 61, 106, 0.18)',
                borderRadius: 12,
                padding: 12,
                marginBottom: 10
              }}
            >
              <div className="row" style={{ justifyContent: 'space-between', marginBottom: 8 }}>
                <span style={{ fontWeight: 800 }}>지역 {idx + 1}</span>
                {preferredRegions.length > 1 ? (
                  <button type="button" className="btn" onClick={() => removeRegionRow(idx)}>
                    삭제
                  </button>
                ) : null}
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 8 }}>
                <Select label="시·도" value={r.sido} onChange={(v) => onSidoRow(idx, v)} options={sidoOpts} />
                <Select label="시·군·구" value={r.sigungu} onChange={(v) => onSigunguRow(idx, v)} options={rowSigunguOptions(r.sido)} />
                <Select label="동·읍·면" value={r.dong} onChange={(v) => updateRegionRow(idx, { dong: v })} options={rowDongOptions(r.sido, r.sigungu)} />
              </div>
            </div>
          ))}
          <button type="button" className="btn accent" onClick={addRegionRow}>
            + 지역 추가
          </button>
        </div>
      ) : null}

      {tab === 'AGE' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>선호하는 아이 연령대</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>복수 선택 가능합니다.</div>
          {SITTER_AGE_OPTIONS.map((a) => {
            const on = ageGroups.includes(a.id);
            return (
              <button
                key={a.id}
                type="button"
                className={`btn ${on ? 'primary' : ''}`}
                style={{ width: '100%', textAlign: 'left', padding: 12, marginBottom: 8 }}
                onClick={() => toggleAge(a.id)}
              >
                <div style={{ fontWeight: 800 }}>{a.title}</div>
                <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>{a.desc}</div>
              </button>
            );
          })}
        </div>
      ) : null}

      {tab === 'IMG' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>프로필 사진 (최대 5장)</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 10 }}>증명사진 또는 본인/돌봄 관련 사진을 올려주세요. (jpg/png 등)</div>
          <input type="file" accept="image/*" multiple onChange={onPickPhotos} disabled={photoIds.length >= 5} />
          <div style={{ fontSize: 12, marginTop: 8 }}>{photoIds.length} / 5</div>
          <div className="row" style={{ flexWrap: 'wrap', gap: 10, marginTop: 12 }}>
            {photoIds.map((id) => (
              <div key={id} style={{ position: 'relative' }}>
                <img
                  src={getSitterProfileImageUrl(id)}
                  alt="프로필"
                  style={{ width: 96, height: 96, objectFit: 'cover', borderRadius: 12, border: '1px solid rgba(0,0,0,0.08)' }}
                />
                <button type="button" className="btn" style={{ marginTop: 4, fontSize: 11 }} onClick={() => removePhoto(id)}>
                  삭제
                </button>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {tab === 'BIO' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>간단한 자기소개</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 10 }}>연락처·이메일·카카오 ID는 입력하지 마세요.</div>
          <div className="field">
            <label>자기소개 (최대 2000자)</label>
            <textarea
              style={{ width: '100%', minHeight: 160, borderRadius: 12, padding: 10, fontFamily: 'inherit' }}
              maxLength={2000}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="아이를 돌본 경험, 성격, 가능한 활동 등을 적어주세요."
            />
            <div style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', textAlign: 'right' }}>{bio.length} / 2000</div>
          </div>
        </div>
      ) : null}

      <div className="divider" />

      <div className="row" style={{ marginTop: 12 }}>
        <button type="button" className="btn primary" onClick={commit} disabled={loading}>
          {loading ? '저장 중...' : '저장'}
        </button>
        <button type="button" className="btn accent" onClick={onDelete} disabled={loading}>
          프로필 삭제
        </button>
      </div>
    </div>
  );
}
