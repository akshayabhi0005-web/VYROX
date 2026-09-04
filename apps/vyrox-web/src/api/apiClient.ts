import axios from 'axios';

const isLocal = typeof window !== 'undefined' && (
  window.location.hostname === 'localhost' || 
  window.location.hostname === '127.0.0.1' ||
  window.location.hostname.startsWith('192.168.')
);

const API_BASE_URL = isLocal 
  ? 'http://localhost:8080/api/v1' 
  : (import.meta.env.VITE_API_BASE_URL || 'https://vyrox-backend-rg3r.onrender.com/api/v1');

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
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
