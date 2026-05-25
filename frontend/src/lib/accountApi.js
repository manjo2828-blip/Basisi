import { apiFetch } from './api.js';

export async function getAccountMe() {
  return await apiFetch('/account/me', { method: 'GET' });
}

export async function updateAccountName(name) {
  return await apiFetch('/account/name', {
    method: 'PUT',
    body: JSON.stringify({ name })
  });
}

export async function updateAccountEmail(email) {
  return await apiFetch('/account/email', {
    method: 'PUT',
    body: JSON.stringify({ email })
  });
}

export async function updateAccountPassword(currentPassword, newPassword) {
  return await apiFetch('/account/password', {
    method: 'PUT',
    body: JSON.stringify({ currentPassword, newPassword })
  });
}

