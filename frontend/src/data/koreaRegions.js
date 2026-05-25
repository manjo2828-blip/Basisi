/**
 * 시·도 / 시·군·구 / 동 3단 선택용 샘플 데이터입니다.
 * 실제 행정구역 전체를 넣으면 파일이 커지므로, 주요 시·도와 서울 25개 자치구를 포함합니다.
 */
const SEOUL_GU = [
  '종로구',
  '중구',
  '용산구',
  '성동구',
  '광진구',
  '동대문구',
  '중랑구',
  '성북구',
  '강북구',
  '도봉구',
  '노원구',
  '은평구',
  '서대문구',
  '마포구',
  '양천구',
  '강서구',
  '구로구',
  '금천구',
  '영등포구',
  '동작구',
  '관악구',
  '서초구',
  '강남구',
  '송파구',
  '강동구'
];

function seoulTriples() {
  return SEOUL_GU.flatMap((gu) => {
    const base = gu.replace(/구$/, '');
    return [`${base} 제1동`, `${base} 제2동`, `${base} 제3동`].map((dong) => ['서울특별시', gu, dong]);
  });
}

const OTHER = [
  ['경기도', '수원시 장안구', '장안동'],
  ['경기도', '수원시 장안구', '파장동'],
  ['경기도', '성남시 분당구', '정자동'],
  ['경기도', '성남시 분당구', '야탑동'],
  ['경기도', '고양시 일산동구', '마두동'],
  ['경기도', '용인시 기흥구', '보정동'],
  ['인천광역시', '연수구', '송도동'],
  ['인천광역시', '남동구', '구월동'],
  ['부산광역시', '해운대구', '우동'],
  ['부산광역시', '부산진구', '부전동'],
  ['대전광역시', '유성구', '봉명동'],
  ['대구광역시', '수성구', '범어동'],
  ['울산광역시', '남구', '삼산동'],
  ['세종특별자치시', '세종특별자치시', '한솔동']
];

const REGION_TRIPLES = [...seoulTriples(), ...OTHER];

export const SIDO_LIST = [...new Set(REGION_TRIPLES.map((r) => r[0]))].sort((a, b) => a.localeCompare(b, 'ko'));

export function listSigungu(sido) {
  const set = new Set();
  for (const r of REGION_TRIPLES) {
    if (r[0] === sido) set.add(r[1]);
  }
  return [...set].sort((a, b) => a.localeCompare(b, 'ko'));
}

export function listDong(sido, sigungu) {
  const set = new Set();
  for (const r of REGION_TRIPLES) {
    if (r[0] === sido && r[1] === sigungu) set.add(r[2]);
  }
  return [...set].sort((a, b) => a.localeCompare(b, 'ko'));
}
