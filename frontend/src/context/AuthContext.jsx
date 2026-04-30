import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('taskmanager_token');
    if (!token) {
      setLoading(false);
      return;
    }

    api.me()
      .then(setUser)
      .catch(() => localStorage.removeItem('taskmanager_token'))
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo(() => ({
    user,
    loading,
    async login(payload) {
      const response = await api.login(payload);
      localStorage.setItem('taskmanager_token', response.token);
      setUser(response.user);
      return response.user;
    },
    async signup(payload) {
      const response = await api.signup(payload);
      localStorage.setItem('taskmanager_token', response.token);
      setUser(response.user);
      return response.user;
    },
    logout() {
      localStorage.removeItem('taskmanager_token');
      setUser(null);
    }
  }), [user, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
