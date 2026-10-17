const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1').replace(/\/+$/, '');
const SESSION_KEY = 'fleet-telematics-session';

export class ApiError extends Error {
  constructor(message, status = 0, payload = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}

export function readSession() {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const session = JSON.parse(raw);
    if (!session?.accessToken || !session?.expiresAt || session.expiresAt <= Date.now()) {
      clearSession();
      return null;
    }
    return session;
  } catch {
    clearSession();
    return null;
  }
}

export function saveSession(session) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  sessionStorage.removeItem(SESSION_KEY);
}

export async function apiRequest(path, options = {}) {
  const { method = 'GET', body, skipAuth = false, token: explicitToken, headers: extraHeaders = {}, ...rest } = options;
  const headers = { Accept: 'application/json', ...extraHeaders };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const saved = !skipAuth ? readSession() : null;
  const token = explicitToken || saved?.accessToken;
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: 'omit',
      ...rest,
    });
  } catch (error) {
    throw new ApiError('Cannot reach the backend. Confirm Spring Boot is running on http://localhost:8080.', 0, error);
  }

  if (response.status === 204) return null;
  const raw = await response.text();
  let payload = null;
  if (raw) {
    try { payload = JSON.parse(raw); } catch { payload = { message: raw }; }
  }
  if (!response.ok) {
    if (response.status === 401 && !skipAuth) {
      clearSession();
      window.dispatchEvent(new CustomEvent('fleet:unauthorized'));
    }
    const message = payload?.message || payload?.detail || payload?.error ||
      (response.status === 401 ? 'Your session has expired or is invalid. Please sign in again.' :
       response.status === 403 ? 'You do not have permission to perform this action.' :
       response.status === 404 ? 'The requested record was not found.' :
       `Request failed with HTTP ${response.status}.`);
    throw new ApiError(message, response.status, payload);
  }
  return payload;
}

export function apiUrl(path) {
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
}
