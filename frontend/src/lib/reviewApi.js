import { apiFetch } from './api.js';

export async function createReview(payload) {
  return await apiFetch('/reviews', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function getSitterReviews(sitterProfileId) {
  return await apiFetch(`/reviews/sitters/${sitterProfileId}`, {
    method: 'GET',
    public: true
  });
}

export async function getSitterReviewSummary(sitterProfileId) {
  return await apiFetch(`/reviews/sitters/${sitterProfileId}/summary`, {
    method: 'GET',
    public: true
  });
}

