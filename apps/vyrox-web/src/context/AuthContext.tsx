import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';
import { apiClient } from '../api/apiClient';

interface PendingAction {
  type: 'ADD_TO_CART' | 'BUY_NOW' | 'ADD_TO_WISHLIST';
  productId: number;
  quantity?: number;
  redirectUrl?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  pendingAction: PendingAction | null;
  setPendingAction: (action: PendingAction | null) => void;
  executePendingAction: () => Promise<void>;
  login: (userData: User, token: string) => void;
  logout: () => void;
  refreshUserData: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(() => {
    const saved = localStorage.getItem('vyrox_pending_action');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    const storedToken = localStorage.getItem('vyrox_access_token');
    const storedUser = localStorage.getItem('vyrox_user');

    if (storedToken && storedUser) {
      try {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } catch (e) {
        localStorage.removeItem('vyrox_access_token');
        localStorage.removeItem('vyrox_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = (userData: User, accessToken: string) => {
    setUser(userData);
    setToken(accessToken);
    localStorage.setItem('vyrox_access_token', accessToken);
    localStorage.setItem('vyrox_user', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('vyrox_access_token');
    localStorage.removeItem('vyrox_user');
    localStorage.removeItem('vyrox_pending_action');
  };

  const refreshUserData = async () => {
    if (!token) return;
    try {
      const res = await apiClient.get('/coins');
      if (user && res.data) {
        const updated = { ...user, coinBalance: res.data.balance };
        setUser(updated);
        localStorage.setItem('vyrox_user', JSON.stringify(updated));
      }
    } catch (e) {
      console.error('Failed to refresh user stats', e);
    }
  };

  const executePendingAction = async () => {
    if (!pendingAction) return;

    try {
      if (pendingAction.type === 'ADD_TO_CART') {
        await apiClient.post('/cart/add', {
          productId: pendingAction.productId,
          quantity: pendingAction.quantity || 1,
        });
      } else if (pendingAction.type === 'BUY_NOW') {
        await apiClient.post('/cart/add', {
          productId: pendingAction.productId,
          quantity: pendingAction.quantity || 1,
        });
      } else if (pendingAction.type === 'ADD_TO_WISHLIST') {
        await apiClient.post(`/wishlist/add/${pendingAction.productId}`);
      }
    } catch (err) {
      console.error('Failed to execute pending user action', err);
    } finally {
      localStorage.removeItem('vyrox_pending_action');
      setPendingAction(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isLoading,
        pendingAction,
        setPendingAction: (action) => {
          setPendingAction(action);
          if (action) {
            localStorage.setItem('vyrox_pending_action', JSON.stringify(action));
          } else {
            localStorage.removeItem('vyrox_pending_action');
          }
        },
        executePendingAction,
        login,
        logout,
        refreshUserData,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
