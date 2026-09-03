import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { tokenStorage } from '../api/client';
import * as authApi from '../api/auth';
import type { AuthenticatedUser } from '../types';

const USER_KEY = 'ecommerce_user';

interface AuthContextValue {
  user: AuthenticatedUser | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadStoredUser(): AuthenticatedUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthenticatedUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthenticatedUser | null>(loadStoredUser);

  const persistSession = useCallback((auth: { token: string; name: string; email: string; role: 'USER' | 'ADMIN' }) => {
    tokenStorage.set(auth.token);
    const authenticatedUser: AuthenticatedUser = { name: auth.name, email: auth.email, role: auth.role };
    localStorage.setItem(USER_KEY, JSON.stringify(authenticatedUser));
    setUser(authenticatedUser);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await authApi.login({ email, password });
      persistSession(response);
    },
    [persistSession],
  );

  const register = useCallback(
    async (name: string, email: string, password: string) => {
      const response = await authApi.register({ name, email, password });
      persistSession(response);
    },
    [persistSession],
  );

  const logout = useCallback(() => {
    tokenStorage.clear();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: user !== null, login, register, logout }),
    [user, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
}
