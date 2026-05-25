// 시터 탐색 관련 API 호출 모음입니다.

// 공통 API fetch 함수를 import합니다.
import { apiFetch } from './api.js';

// 공통 숫자 파라미터를 쿼리에 추가합니다.
function appendNumberParam(qs, key, value) {
  // null/undefined/빈문자열은 제외합니다.
  if (value === null || value === undefined || value === '') return;
  // 숫자형으로 변환합니다.
  const n = Number(value);
  // 숫자가 아니면 제외합니다.
  if (Number.isNaN(n)) return;
  // 쿼리에 추가합니다.
  qs.set(key, String(n));
}

// 공통 문자열 파라미터를 쿼리에 추가합니다.
function appendStringParam(qs, key, value) {
  // null/undefined/빈문자열은 제외합니다.
  if (value === null || value === undefined || value === '') return;
  // 양끝 공백을 제거해 추가합니다.
  qs.set(key, String(value).trim());
}

// 시터 탐색 API를 호출합니다.
export async function searchSitters(params) {
  // URLSearchParams로 쿼리 스트링을 구성합니다.
  const qs = new URLSearchParams();
  // 문자열/숫자 파라미터를 구성합니다.
  appendStringParam(qs, 'region', params?.region);
  appendStringParam(qs, 'gender', params?.gender);
  appendNumberParam(qs, 'minYearsOfExperience', params?.minYearsOfExperience);

  // 쿼리 스트링을 붙여 GET 요청을 보냅니다.
  const query = qs.toString() ? `?${qs.toString()}` : '';
  // 탐색 엔드포인트를 호출합니다.
  // 로그인 후 검색 화면에서만 쓰이며 JWT를 보냅니다(permitAll·authenticated 모두 통과).
  return await apiFetch(`/sitters/search${query}`, {
    method: 'GET'
  });
}

// 시터 탐색 페이지 API를 호출합니다. (복합필터/페이지/정렬)
export async function searchSittersPage(params) {
  // URLSearchParams로 쿼리 스트링을 구성합니다.
  const qs = new URLSearchParams();
  // 필터 파라미터를 구성합니다.
  appendStringParam(qs, 'region', params?.region);
  appendStringParam(qs, 'gender', params?.gender);
  appendNumberParam(qs, 'minYearsOfExperience', params?.minYearsOfExperience);
  appendNumberParam(qs, 'page', params?.page);
  appendNumberParam(qs, 'size', params?.size);
  // 정렬 파라미터를 구성합니다.
  appendStringParam(qs, 'sort', params?.sort);

  // 쿼리 스트링을 붙여 GET 요청을 보냅니다.
  const query = qs.toString() ? `?${qs.toString()}` : '';
  // 페이징 탐색 엔드포인트를 호출합니다.
  return await apiFetch(`/sitters/search/page${query}`, {
    method: 'GET'
  });
}

// 지도/목록 클릭 시 사용하는 공개 시터 상세 API입니다.
export async function getPublicSitterDetail(sitterProfileId) {
  if (!sitterProfileId && sitterProfileId !== 0) {
    throw new Error('sitterProfileId가 필요합니다.');
  }
  return await apiFetch(`/sitters/${sitterProfileId}`, {
    method: 'GET',
    public: true
  });
}

