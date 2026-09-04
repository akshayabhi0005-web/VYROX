import axios from 'axios';

const getApiBaseUrl = (): string => {
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname === '127.0.0.1' || hostname.startsWith('192.168.')) {
      return `http://${hostname}:8080/api/v1`;
    }
  }
  return import.meta.env.VITE_API_BASE_URL || 'https://vyrox-backend-rg3r.onrender.com/api/v1';
};

export const API_BASE_URL = getApiBaseUrl();

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('vyrox_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Don't auto-redirect if browsing public paths
      const isPublicPath = ['/login', '/register', '/', '/top-deals', '/compare'].includes(window.location.pathname) ||
                           window.location.pathname.startsWith('/product/');
      if (!isPublicPath) {
        localStorage.removeItem('vyrox_access_token');
        localStorage.removeItem('vyrox_user');
      }
    }
    return Promise.reject(error);
  }
);
