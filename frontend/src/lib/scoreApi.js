import { apiFetch } from './api.js';

export async function getSitterScore(sitterProfileId) {
  if (sitterProfileId == null) {
    throw new Error('sitterProfileId가 필요합니다.');
  }
  return await apiFetch(`/sitter-scores/sitters/${sitterProfileId}`, {
    method: 'GET',
    public: true
  });
}

export async function getMySitterScore() {
  return await apiFetch('/sitter-scores/me', {
    method: 'GET'
  });
}
