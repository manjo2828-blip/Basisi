import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { TextInput } from '../components/TextInput.jsx';
import { Select } from '../components/Select.jsx';
import { ApiError } from '../lib/api.js';
import {
  deleteMyParentProfile,
  getMyParentProfile,
  upsertMyParentProfile
} from '../lib/profileApi.js';
import { PARENT_EXPECTATION_KEYWORDS } from '../data/parentExpectationKeywords.js';
import { SIDO_LIST, listDong, listSigungu } from '../data/koreaRegions.js';

const TABS = [
  { id: 'REGION', label: '지역 선택' },
  { id: 'CHILDREN', label: '아이 정보 등록' },
  { id: 'WORK', label: '부모 정보 등록' },
  { id: 'CARE', label: '돌봄 아이 선택' },
  { id: 'KEYWORDS', label: '맘시터 요청' },
  { id: 'SCHEDULE', label: '필요 일정' },
  { id: 'MESSAGE', label: '전할 말' }
];

const PREFERRED_AGE_OPTIONS = [
  { value: 'TWENTIES', label: '20대 (20~29세)' },
  { value: 'THIRTIES', label: '30대 (30~39세)' },
  { value: 'FORTIES', label: '40대 (40~49세)' },
  { value: 'FIFTIES', label: '50대 (50~59세)' }
];

const PREFERRED_GENDER_OPTIONS = [
  { value: 'FEMALE', label: '여성' },
  { value: 'MALE', label: '남성' }
];

const PREFERRED_EXPERIENCE_OPTIONS = [
  { value: 'UNDER_1', label: '신입 ~ 1년 미만' },
  { value: 'FROM_2_TO_5', label: '2년 ~ 5년 미만' },
  { value: 'FROM_5_TO_9', label: '5년 ~ 9년 미만' },
  { value: 'OVER_10', label: '10년 이상' }
];

const PREFERRED_NATIONALITY_OPTIONS = [
  { value: 'KOREAN', label: '내국인' },
  { value: 'FOREIGNER', label: '외국인' }
];

function emptyChild() {
  return { id: typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : String(Date.now()), birthDate: '', gender: '' };
}

function PreferredRadioGroup({ label, name, options, value, onChange }) {
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ fontSize: 13, fontWeight: 700, marginBottom: 6 }}>{label}</div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {options.map((opt) => {
          const on = value === opt.value;
          return (
            <label
              key={opt.value}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                padding: '6px 12px',
                borderRadius: 999,
                border: on ? '1.5px solid #c73d6a' : '1px solid rgba(26,21,35,0.18)',
                background: on ? 'rgba(199, 61, 106, 0.08)' : '#fff',
                cursor: 'pointer',
                fontSize: 12,
                fontWeight: on ? 700 : 500,
                color: on ? '#c73d6a' : '#1a1523'
              }}
            >
              <input
                type="radio"
                name={name}
                value={opt.value}
                checked={on}
                onChange={() => onChange(opt.value)}
                style={{ accentColor: '#c73d6a' }}
              />
              {opt.label}
            </label>
          );
        })}
        {value ? (
          <button
            type="button"
            className="btn"
            style={{ fontSize: 11, borderRadius: 999, padding: '4px 10px' }}
            onClick={() => onChange('')}
          >
            선택 해제
          </button>
        ) : null}
      </div>
    </div>
  );
}

function applyServerProfile(setters, res) {
  const {
    setPhone,
    setChildNote,
    setSido,
    setSigungu,
    setDong,
    setParentWorkType,
    setScheduleType,
    setCareChildId,
    setChildren,
    setKeywords,
    setSitterMessage,
    setPreferredSitterAgeRange,
    setPreferredSitterGender,
    setPreferredSitterExperience,
    setPreferredSitterNationality,
    setPreferredRegionSido,
    setPreferredRegionSigungu,
    setPreferredRegionDong
  } = setters;
  setPhone(res?.phoneNumber ?? '');
  setChildNote(res?.childNote ?? '');
  setSido(res?.regionSido ?? '');
  setSigungu(res?.regionSigungu ?? '');
  setDong(res?.regionDong ?? '');
  setParentWorkType(res?.parentWorkType ?? '');
  setScheduleType(res?.scheduleType ?? '');
  setCareChildId(res?.careChildId ?? '');
  const ch = Array.isArray(res?.children) ? res.children : [];
  setChildren(
    ch.length
      ? ch.map((c) => ({
          id: c.id,
          birthDate: c.birthDate ?? '',
          gender: c.gender ?? ''
        }))
      : []
  );
  setKeywords(Array.isArray(res?.expectationKeywords) ? [...res.expectationKeywords] : []);
  setSitterMessage(res?.sitterMessage ?? '');
  setPreferredSitterAgeRange(res?.preferredSitterAgeRange ?? '');
  setPreferredSitterGender(res?.preferredSitterGender ?? '');
  setPreferredSitterExperience(res?.preferredSitterExperience ?? '');
  setPreferredSitterNationality(res?.preferredSitterNationality ?? '');
  setPreferredRegionSido(res?.preferredRegionSido ?? '');
  setPreferredRegionSigungu(res?.preferredRegionSigungu ?? '');
  setPreferredRegionDong(res?.preferredRegionDong ?? '');
}

export function ParentProfileEditor({ onToast, onAuthChanged }) {
  const [tab, setTab] = useState('REGION');
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState('');

  const [phone, setPhone] = useState('');
  const [childNote, setChildNote] = useState('');
  const [sido, setSido] = useState('');
  const [sigungu, setSigungu] = useState('');
  const [dong, setDong] = useState('');
  const [parentWorkType, setParentWorkType] = useState('');
  const [scheduleType, setScheduleType] = useState('');
  const [careChildId, setCareChildId] = useState('');
  const [children, setChildren] = useState([]);
  const [keywords, setKeywords] = useState([]);
  const [sitterMessage, setSitterMessage] = useState('');

  // ===== 시터 매칭용 필수 조건 =====
  const [preferredSitterAgeRange, setPreferredSitterAgeRange] = useState('');
  const [preferredSitterGender, setPreferredSitterGender] = useState('');
  const [preferredSitterExperience, setPreferredSitterExperience] = useState('');
  const [preferredSitterNationality, setPreferredSitterNationality] = useState('');
  const [preferredRegionSido, setPreferredRegionSido] = useState('');
  const [preferredRegionSigungu, setPreferredRegionSigungu] = useState('');
  const [preferredRegionDong, setPreferredRegionDong] = useState('');

  useEffect(() => {
    const complete = children.filter((c) => (c.birthDate || '').trim());
    if (complete.length === 1) {
      setCareChildId((prev) => (prev === complete[0].id ? prev : complete[0].id));
      return;
    }
    setCareChildId((prev) => {
      if (!prev) return prev;
      return complete.some((c) => c.id === prev) ? prev : '';
    });
  }, [children]);

  const sigunguOptions = useMemo(() => {
    if (!sido) return [{ value: '', label: '시·도를 먼저 선택하세요' }];
    const list = listSigungu(sido);
    if (!list.length) return [{ value: '', label: '목록 없음' }];
    return [{ value: '', label: '시·군·구 선택' }, ...list.map((s) => ({ value: s, label: s }))];
  }, [sido]);

  const dongOptions = useMemo(() => {
    if (!sido || !sigungu) return [{ value: '', label: '시·군·구를 먼저 선택하세요' }];
    const list = listDong(sido, sigungu);
    if (!list.length) return [{ value: '', label: '목록 없음' }];
    return [{ value: '', label: '동·읍·면 선택' }, ...list.map((s) => ({ value: s, label: s }))];
  }, [sido, sigungu]);

  const sidoOptions = useMemo(
    () => [{ value: '', label: '시·도 선택' }, ...SIDO_LIST.map((s) => ({ value: s, label: s }))],
    []
  );

  const preferredSigunguOptions = useMemo(() => {
    if (!preferredRegionSido) return [{ value: '', label: '시·도를 먼저 선택하세요' }];
    const list = listSigungu(preferredRegionSido);
    if (!list.length) return [{ value: '', label: '목록 없음' }];
    return [{ value: '', label: '시·군·구 선택' }, ...list.map((s) => ({ value: s, label: s }))];
  }, [preferredRegionSido]);

  const preferredDongOptions = useMemo(() => {
    if (!preferredRegionSido || !preferredRegionSigungu) {
      return [{ value: '', label: '시·군·구를 먼저 선택하세요' }];
    }
    const list = listDong(preferredRegionSido, preferredRegionSigungu);
    if (!list.length) return [{ value: '', label: '목록 없음' }];
    return [{ value: '', label: '동·읍·면 선택 (선택 사항)' }, ...list.map((s) => ({ value: s, label: s }))];
  }, [preferredRegionSido, preferredRegionSigungu]);

  const onPreferredSidoChange = (v) => {
    setPreferredRegionSido(v);
    setPreferredRegionSigungu('');
    setPreferredRegionDong('');
  };

  const onPreferredSigunguChange = (v) => {
    setPreferredRegionSigungu(v);
    setPreferredRegionDong('');
  };

  const reload = useCallback(async () => {
    setLoadError('');
    try {
      const res = await getMyParentProfile({ timeoutMs: 60000 });
      applyServerProfile(
        {
          setPhone,
          setChildNote,
          setSido,
          setSigungu,
          setDong,
          setParentWorkType,
          setScheduleType,
          setCareChildId,
          setChildren,
          setKeywords,
          setSitterMessage,
          setPreferredSitterAgeRange,
          setPreferredSitterGender,
          setPreferredSitterExperience,
          setPreferredSitterNationality,
          setPreferredRegionSido,
          setPreferredRegionSigungu,
          setPreferredRegionDong
        },
        res
      );
    } catch (e) {
      if (e instanceof ApiError && (e.status === 404 || (e.message && e.message.includes('부모 프로필이 존재하지 않습니다')))) {
        setLoadError('');
        return;
      }
      if (e instanceof ApiError) {
        setLoadError(e.message);
      } else {
        setLoadError('프로필을 불러오지 못했습니다.');
      }
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const onSidoChange = (v) => {
    setSido(v);
    setSigungu('');
    setDong('');
  };

  const onSigunguChange = (v) => {
    setSigungu(v);
    setDong('');
  };

  const buildPayload = useCallback(() => {
    const completeChildren = children
      .filter((c) => (c.birthDate || '').trim())
      .map((c) => ({
        id: c.id,
        birthDate: c.birthDate,
        gender: (c.gender || '').trim() || null
      }));
    let care = null;
    if (completeChildren.length === 1) {
      care = completeChildren[0].id;
    } else if (completeChildren.length > 1) {
      care =
        careChildId && completeChildren.some((c) => c.id === careChildId) ? careChildId : null;
    }
    return {
      phoneNumber: phone.trim(),
      region: null,
      childNote: (childNote || '').trim() || null,
      regionSido: (sido || '').trim() || null,
      regionSigungu: (sigungu || '').trim() || null,
      regionDong: (dong || '').trim() || null,
      parentWorkType: parentWorkType || null,
      scheduleType: scheduleType || null,
      careChildId: care,
      children: completeChildren,
      expectationKeywords: keywords,
      sitterMessage: (sitterMessage || '').trim() || null,
      // ===== 시터 매칭용 필수 조건 =====
      preferredSitterAgeRange: preferredSitterAgeRange || null,
      preferredSitterGender: preferredSitterGender || null,
      preferredSitterExperience: preferredSitterExperience || null,
      preferredSitterNationality: preferredSitterNationality || null,
      preferredRegionSido: (preferredRegionSido || '').trim() || null,
      preferredRegionSigungu: (preferredRegionSigungu || '').trim() || null,
      preferredRegionDong: (preferredRegionDong || '').trim() || null
    };
  }, [
    phone,
    childNote,
    sido,
    sigungu,
    dong,
    parentWorkType,
    scheduleType,
    careChildId,
    children,
    keywords,
    sitterMessage,
    preferredSitterAgeRange,
    preferredSitterGender,
    preferredSitterExperience,
    preferredSitterNationality,
    preferredRegionSido,
    preferredRegionSigungu,
    preferredRegionDong
  ]);

  const validateForTab = (tabId) => {
    if (!phone.trim()) {
      return '전화번호를 입력해주세요. (지역 선택 탭)';
    }
    if (tabId === 'REGION') {
      if (!sido || !sigungu || !dong) {
        return '시·도 / 시·군·구 / 동·읍·면을 모두 선택해주세요.';
      }
    }
    if (tabId === 'CHILDREN') {
      for (const c of children) {
        if (!c.birthDate?.trim()) {
          return '등록한 모든 아이의 생년월일을 입력해주세요. (빈 카드는 삭제하세요)';
        }
      }
    }
    if (tabId === 'WORK') {
      if (!parentWorkType) {
        return '맞벌이 또는 전업 주부 여부를 선택해주세요.';
      }
    }
    if (tabId === 'CARE') {
      const complete = children.filter((c) => (c.birthDate || '').trim());
      if (complete.length > 1 && !careChildId) {
        return '돌봄이 필요한 아이를 한 명 선택해주세요.';
      }
    }
    if (tabId === 'SCHEDULE') {
      if (!scheduleType) {
        return '일정 유형을 선택해주세요.';
      }
    }
    if (tabId === 'MESSAGE') {
      if ((childNote || '').length > 200) {
        return '추가 메모는 200자 이하여야 합니다.';
      }
    }
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
      const res = await upsertMyParentProfile(buildPayload(), { timeoutMs: 60000 });
      applyServerProfile(
        {
          setPhone,
          setChildNote,
          setSido,
          setSigungu,
          setDong,
          setParentWorkType,
          setScheduleType,
          setCareChildId,
          setChildren,
          setKeywords,
          setSitterMessage,
          setPreferredSitterAgeRange,
          setPreferredSitterGender,
          setPreferredSitterExperience,
          setPreferredSitterNationality,
          setPreferredRegionSido,
          setPreferredRegionSigungu,
          setPreferredRegionDong
        },
        res
      );
      onAuthChanged?.();
      onToast?.({ type: 'success', title: '저장', message: '부모 프로필이 저장되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '저장 중 오류가 발생했습니다.';
      onToast?.({ type: 'error', title: '저장 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const onDelete = async () => {
    if (!window.confirm('부모 프로필을 정말 삭제할까요?')) return;
    setLoading(true);
    try {
      await deleteMyParentProfile();
      setPhone('');
      setChildNote('');
      setSido('');
      setSigungu('');
      setDong('');
      setParentWorkType('');
      setScheduleType('');
      setCareChildId('');
      setChildren([]);
      setKeywords([]);
      setSitterMessage('');
      setPreferredSitterAgeRange('');
      setPreferredSitterGender('');
      setPreferredSitterExperience('');
      setPreferredSitterNationality('');
      setPreferredRegionSido('');
      setPreferredRegionSigungu('');
      setPreferredRegionDong('');
      onAuthChanged?.();
      onToast?.({ type: 'success', title: '삭제', message: '부모 프로필이 삭제되었습니다.' });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : '삭제 중 오류가 발생했습니다.';
      onToast?.({ type: 'error', title: '삭제 실패', message: msg });
    } finally {
      setLoading(false);
    }
  };

  const toggleKeyword = (k) => {
    setKeywords((prev) => {
      if (prev.includes(k)) return prev.filter((x) => x !== k);
      if (prev.length >= 5) {
        onToast?.({ type: 'error', title: '선택 제한', message: '키워드는 최대 5개까지 선택할 수 있습니다.' });
        return prev;
      }
      return [...prev, k];
    });
  };

  const addChildRow = () => {
    if (children.length >= 5) {
      onToast?.({ type: 'error', title: '제한', message: '아이는 최대 5명까지 등록할 수 있습니다.' });
      return;
    }
    setChildren((prev) => [...prev, emptyChild()]);
  };

  const updateChild = (id, patch) => {
    setChildren((prev) => prev.map((c) => (c.id === id ? { ...c, ...patch } : c)));
  };

  const removeChild = (id) => {
    setChildren((prev) => prev.filter((c) => c.id !== id));
    if (careChildId === id) setCareChildId('');
  };

  const scheduleCards = [
    {
      id: 'REGULAR',
      title: '정기적인 일정',
      desc: '정해진 일정에 맞춰 일할 분을 찾아요',
      icon: '📅'
    },
    {
      id: 'SPECIFIC',
      title: '특정일 일정',
      desc: '필요한 날짜만 일할 분을 찾아요',
      icon: '⏰'
    },
    {
      id: 'UNDECIDED',
      title: '일정 미정',
      desc: '아직 일정이 정해지지 않았어요',
      icon: '⏳'
    }
  ];

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 10 }}>부모 프로필</div>
      <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
        아래 탭별로 정보를 입력한 뒤 <strong>저장</strong>을 누르면 서버에 반영됩니다.
      </div>

      {loadError ? <div className="error">{loadError}</div> : null}

      <div className="row" style={{ flexWrap: 'wrap', gap: 8, marginBottom: 14 }}>
        {TABS.map((t) => (
          <button
            key={t.id}
            type="button"
            className={`btn ${tab === t.id ? 'primary' : ''}`}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
        <button type="button" className="btn" onClick={reload} disabled={loading}>
          새로고침
        </button>
      </div>

      <div className="divider" />

      {tab === 'REGION' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>맘시터가 방문할 곳</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
            선택한 지역을 기준으로 맘시터를 찾을 수 있어요.
          </div>
          <TextInput label="전화번호" value={phone} onChange={setPhone} placeholder="010-0000-0000" />
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
              gap: 10,
              marginTop: 8
            }}
          >
            <Select label="시·도" value={sido} onChange={onSidoChange} options={sidoOptions} />
            <Select label="시·군·구" value={sigungu} onChange={onSigunguChange} options={sigunguOptions} />
            <Select label="동·읍·면" value={dong} onChange={setDong} options={dongOptions} />
          </div>
        </div>
      ) : null}

      {tab === 'CHILDREN' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>아이 정보를 등록해주세요</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
            생년월일을 입력해주세요. (최대 5명)
          </div>
          {children.map((c, idx) => (
            <div
              key={c.id}
              style={{
                border: '1px solid rgba(199, 61, 106, 0.18)',
                borderRadius: 12,
                padding: 12,
                marginBottom: 10,
                background: 'rgba(255, 255, 255, 0.9)'
              }}
            >
              <div className="row" style={{ justifyContent: 'space-between', marginBottom: 8 }}>
                <span style={{ fontWeight: 800 }}>아이 {idx + 1}</span>
                <button type="button" className="btn" onClick={() => removeChild(c.id)}>
                  삭제
                </button>
              </div>
              <div className="field">
                <label>생년월일</label>
                <input type="date" value={c.birthDate} onChange={(e) => updateChild(c.id, { birthDate: e.target.value })} />
              </div>
              <div style={{ marginTop: 8, fontSize: 12, color: 'rgba(26,21,35,0.58)' }}>성별</div>
              <div className="row" style={{ marginTop: 6 }}>
                <button
                  type="button"
                  className={`btn ${c.gender === 'FEMALE' ? 'primary' : ''}`}
                  onClick={() => updateChild(c.id, { gender: 'FEMALE' })}
                >
                  여자 아이
                </button>
                <button
                  type="button"
                  className={`btn ${c.gender === 'MALE' ? 'primary' : ''}`}
                  onClick={() => updateChild(c.id, { gender: 'MALE' })}
                >
                  남자 아이
                </button>
              </div>
            </div>
          ))}
          <button type="button" className="btn accent" onClick={addChildRow} disabled={children.length >= 5}>
            + 아이 추가
          </button>
        </div>
      ) : null}

      {tab === 'WORK' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>부모 가구 형태</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>해당하는 항목을 한 가지 선택해주세요.</div>
          <div className="row" style={{ flexWrap: 'wrap', gap: 10 }}>
            <button
              type="button"
              className={`btn ${parentWorkType === 'DUAL_INCOME' ? 'primary' : ''}`}
              style={{ minWidth: 140, minHeight: 72 }}
              onClick={() => setParentWorkType('DUAL_INCOME')}
            >
              맞벌이
            </button>
            <button
              type="button"
              className={`btn ${parentWorkType === 'HOMEMAKER' ? 'primary' : ''}`}
              style={{ minWidth: 140, minHeight: 72 }}
              onClick={() => setParentWorkType('HOMEMAKER')}
            >
              전업 주부·주부
            </button>
          </div>
        </div>
      ) : null}

      {tab === 'CARE' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>돌봄이 필요한 아이를 선택해주세요</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
            아이 정보 탭에서 생년월일을 입력한 아이만 여기에 표시됩니다.
          </div>
          {children.filter((c) => (c.birthDate || '').trim()).length === 0 ? (
            <div style={{ fontSize: 13, color: 'rgba(26,21,35,0.58)' }}>먼저 &quot;아이 정보 등록&quot; 탭에서 아이를 등록해주세요.</div>
          ) : (
            <div
              style={{
                border: '1px solid rgba(199, 61, 106, 0.18)',
                borderRadius: 12,
                padding: 12
              }}
            >
              <div style={{ fontWeight: 700, marginBottom: 8 }}>등록한 아이</div>
              {children
                .filter((c) => (c.birthDate || '').trim())
                .map((c) => {
                  const g = c.gender === 'MALE' ? '남자' : c.gender === 'FEMALE' ? '여자' : '';
                  const label = `${c.birthDate}${g ? ` · ${g} 아이` : ''}`;
                  return (
                    <label
                      key={c.id}
                      style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, cursor: 'pointer' }}
                    >
                      <input
                        type="radio"
                        name="careChild"
                        checked={careChildId === c.id}
                        onChange={() => setCareChildId(c.id)}
                      />
                      <span>{label}</span>
                    </label>
                  );
                })}
              <div style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', marginTop: 8 }}>
                * 한 명의 맘시터는 안전상의 이유로 아이 2명까지 돌볼 수 있어요(서비스 정책은 별도 안내를 참고하세요).
              </div>
            </div>
          )}
        </div>
      ) : null}

      {tab === 'KEYWORDS' ? (
        <div>
          {/* ===== [필수 조건 선택] 섹션 ===== */}
          <div
            style={{
              border: '1px solid rgba(199, 61, 106, 0.18)',
              borderRadius: 14,
              padding: 16,
              marginBottom: 18,
              background: 'rgba(255, 255, 255, 0.9)'
            }}
          >
            <div style={{ fontWeight: 800, marginBottom: 6 }}>맘시터 필수 조건 선택</div>
            <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 14 }}>
              매칭 정확도를 높이기 위한 항목입니다. 선택하지 않은 항목은 매칭에서 무관 조건으로 처리돼요.
            </div>

            <PreferredRadioGroup
              label="희망 나이"
              name="preferredSitterAgeRange"
              options={PREFERRED_AGE_OPTIONS}
              value={preferredSitterAgeRange}
              onChange={setPreferredSitterAgeRange}
            />
            <PreferredRadioGroup
              label="희망 성별"
              name="preferredSitterGender"
              options={PREFERRED_GENDER_OPTIONS}
              value={preferredSitterGender}
              onChange={setPreferredSitterGender}
            />
            <PreferredRadioGroup
              label="희망 경력"
              name="preferredSitterExperience"
              options={PREFERRED_EXPERIENCE_OPTIONS}
              value={preferredSitterExperience}
              onChange={setPreferredSitterExperience}
            />
            <PreferredRadioGroup
              label="희망 국적"
              name="preferredSitterNationality"
              options={PREFERRED_NATIONALITY_OPTIONS}
              value={preferredSitterNationality}
              onChange={setPreferredSitterNationality}
            />

            <div style={{ marginTop: 14 }}>
              <div style={{ fontSize: 13, fontWeight: 700, marginBottom: 6 }}>희망 활동 지역</div>
              <div style={{ fontSize: 11, color: 'rgba(26,21,35,0.58)', marginBottom: 8 }}>
                동·읍·면은 선택 사항이며, 정확한 매칭을 위해 가능하면 시·군·구까지 선택해주세요.
              </div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
                  gap: 10
                }}
              >
                <Select
                  label="시·도"
                  value={preferredRegionSido}
                  onChange={onPreferredSidoChange}
                  options={sidoOptions}
                />
                <Select
                  label="시·군·구"
                  value={preferredRegionSigungu}
                  onChange={onPreferredSigunguChange}
                  options={preferredSigunguOptions}
                />
                <Select
                  label="동·읍·면"
                  value={preferredRegionDong}
                  onChange={setPreferredRegionDong}
                  options={preferredDongOptions}
                />
              </div>
            </div>
          </div>

          {/* ===== 기존: 맘시터에게 바라는 활동(키워드 칩) ===== */}
          <div style={{ fontWeight: 800, marginBottom: 8 }}>맘시터에게 바라는 활동</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 8 }}>
            필수로 요청하고 싶은 활동을 <strong>최대 5개</strong>까지 선택해주세요.
          </div>
          <div style={{ fontSize: 12, marginBottom: 10 }}>
            선택됨: {keywords.length}/5
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {PARENT_EXPECTATION_KEYWORDS.map((k) => {
              const on = keywords.includes(k);
              return (
                <button
                  key={k}
                  type="button"
                  className={`btn ${on ? 'primary' : ''}`}
                  style={{ fontSize: 12, borderRadius: 999, padding: '6px 12px' }}
                  onClick={() => toggleKeyword(k)}
                >
                  {k}
                </button>
              );
            })}
          </div>
        </div>
      ) : null}

      {tab === 'SCHEDULE' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>맘시터가 필요한 일정</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
            일정에 가장 적합한 맘시터 매칭에 활용됩니다.
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {scheduleCards.map((s) => (
              <button
                key={s.id}
                type="button"
                className={`btn ${scheduleType === s.id ? 'primary' : ''}`}
                style={{
                  textAlign: 'left',
                  padding: 14,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  justifyContent: 'space-between'
                }}
                onClick={() => setScheduleType(s.id)}
              >
                <span style={{ fontSize: 22 }}>{s.icon}</span>
                <span style={{ flex: 1 }}>
                  <div style={{ fontWeight: 800 }}>{s.title}</div>
                  <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginTop: 4 }}>{s.desc}</div>
                </span>
                <span aria-hidden>›</span>
              </button>
            ))}
          </div>
        </div>
      ) : null}

      {tab === 'MESSAGE' ? (
        <div>
          <div style={{ fontWeight: 800, marginBottom: 8 }}>맘시터에게 전할 말이 있나요?</div>
          <div style={{ fontSize: 12, color: 'rgba(26,21,35,0.58)', marginBottom: 12 }}>
            마이페이지에서 언제든 수정할 수 있어요.
          </div>
          <div className="field">
            <label>메시지 (최대 2000자)</label>
            <textarea
              style={{ width: '100%', minHeight: 160, borderRadius: 12, padding: 10, fontFamily: 'inherit' }}
              maxLength={2000}
              value={sitterMessage}
              onChange={(e) => setSitterMessage(e.target.value)}
              placeholder="예) 안녕하세요. 4살 딸 아이가 있습니다. 오후 2시부터 6시까지..."
            />
            <div style={{ fontSize: 11, color: 'rgba(26,21,35,0.48)', textAlign: 'right' }}>
              {sitterMessage.length} / 2000
            </div>
          </div>
          <TextInput
            label="추가 메모(선택, 200자)"
            value={childNote}
            onChange={setChildNote}
            placeholder="기타 전달 사항"
          />
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
