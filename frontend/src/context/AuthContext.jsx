import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiRequest, clearSession, readSession, saveSession } from '../lib/api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
  }, []);

  useEffect(() => {
    let active = true;
    const onUnauthorized = () => { if (active) logout(); };
    window.addEventListener('fleet:unauthorized', onUnauthorized);
    const saved = readSession();
    if (!saved) {
      setLoading(false);
    } else {
      apiRequest('/auth/me', { token: saved.accessToken })
        .then((profile) => {
          if (!active) return;
          const refreshed = { ...saved, username: profile.username, roles: profile.roles || [] };
          saveSession(refreshed);
          setUser({ username: refreshed.username, roles: refreshed.roles });
        })
        .catch(() => { if (active) logout(); })
        .finally(() => { if (active) setLoading(false); });
    }
    return () => {
      active = false;
      window.removeEventListener('fleet:unauthorized', onUnauthorized);
    };
  }, [logout]);

  const login = useCallback(async (username, password) => {
    const result = await apiRequest('/auth/login', {
      method: 'POST', skipAuth: true, body: { username, password },
    });
    if (!result?.accessToken) throw new Error('The API login response did not include an access token.');
    const session = {
      accessToken: result.accessToken,
      expiresAt: Date.now() + Number(result.expiresInSeconds || 3600) * 1000,
      username: result.username || username,
      roles: result.roles || [],
    };
    saveSession(session);
    setUser({ username: session.username, roles: session.roles });
    return session;
  }, []);

  const value = useMemo(() => ({ user, loading, login, logout }), [user, loading, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider.');
  return context;
}
