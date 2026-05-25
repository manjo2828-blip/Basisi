// AI 시터 추천 API (백엔드 연동 전에는 mock 폴백)

import { apiFetch } from './api.js';

/**
 * @param {{ useMyProfile: boolean, additionalRequest?: string, limit?: number }} payload
 * @returns {Promise<{ summary: string, items: Array<{ rank: number, matchScore?: number, reasons: string[], sitter: object }>, disclaimer?: string }>}
 */
export async function postSitterRecommend(payload) {
  try {
    return await apiFetch('/recommendations/sitters', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  } catch (e) {
    // 엔드포인트 미배포(404) 또는 서버 미기동(0)일 때만 미리보기 mock 사용
    if (e?.status === 404 || e?.status === 0) {
      return getMockRecommendations(payload?.limit ?? 5);
    }
    // 401/403: 토큰 만료·백엔드 미재시작(신규 API 미반영) 등 — mock으로 숨기지 않고 그대로 전달
    throw e;
  }
}

/** 백엔드 연동 전 UI·데모용 미리보기 데이터 */
function getMockRecommendations(limit = 5) {
  const pool = [
    {
      rank: 1,
      matchScore: 94,
      reasons: ['등록하신 지역과 활동 희망 조건이 잘 맞습니다', '불꽃 점수·평점이 높은 편입니다', '영유아 돌봄 경력이 있습니다'],
      sitter: {
        sitterProfileId: 101,
        userId: 201,
        name: '김지현',
        bio: '책 읽기와 실내놀이를 중심으로 아이와 따뜻하게 지내요.',
        age: 28,
        gender: 'FEMALE',
        yearsOfExperience: 5,
        hasCertificate: true,
        region: '서울 강남구',
        flameScore: 88,
        flameGrade: 'GOLD',
        completedReservationCount: 24,
        averageRating: 4.8,
        recentActivityCount: 6
      }
    },
    {
      rank: 2,
      matchScore: 89,
      reasons: ['요청하신 활동(책·놀이)을 제공할 수 있습니다', '희망 지역 인근에서 활동 가능합니다'],
      sitter: {
        sitterProfileId: 102,
        userId: 202,
        name: '이수진',
        bio: '유아·초등 저학년 돌봄 경험이 많습니다.',
        age: 26,
        gender: 'FEMALE',
        yearsOfExperience: 3,
        hasCertificate: true,
        region: '서울 송파구',
        flameScore: 76,
        flameGrade: 'SILVER',
        completedReservationCount: 15,
        averageRating: 4.6,
        recentActivityCount: 4
      }
    },
    {
      rank: 3,
      matchScore: 85,
      reasons: ['경력 3년 이상', 'CCTV 동의 가능 시터입니다'],
      sitter: {
        sitterProfileId: 103,
        userId: 203,
        name: '박민준',
        bio: '체육·야외 활동과 함께 안전하게 돌봅니다.',
        age: 30,
        gender: 'MALE',
        yearsOfExperience: 4,
        hasCertificate: false,
        region: '경기 성남시',
        flameScore: 72,
        flameGrade: 'SILVER',
        completedReservationCount: 11,
        averageRating: 4.5,
        recentActivityCount: 3
      }
    }
  ];
  return {
    summary:
      '마이페이지 조건과 추가 요청을 반영해, 지역·활동·평점을 고려한 시터를 골랐어요. (현재는 화면 미리보기용 예시 결과입니다. AI 추천 API 연동 후 실제 데이터가 표시됩니다.)',
    items: pool.slice(0, Math.min(limit, pool.length)),
    disclaimer: 'AI 추천은 참고용이며, 최종 선택은 부모님이 하세요.'
  };
}
