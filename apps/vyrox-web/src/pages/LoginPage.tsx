import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Mail, Lock, Phone, AlertCircle, ArrowRight, Check, Settings, ShieldCheck } from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login, executePendingAction } = useAuth();

  const redirectUrl = searchParams.get('redirect') || '/';

  const [authMode, setAuthMode] = useState<'email' | 'otp'>('email');
  const [emailOrMobile, setEmailOrMobile] = useState('');
  const [password, setPassword] = useState('');
  const [otpMobile, setOtpMobile] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [devOtpHint, setDevOtpHint] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [oauthNotice, setOauthNotice] = useState<string | null>(null);
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [configStatus, setConfigStatus] = useState<any>(null);

  const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
  const FACEBOOK_APP_ID = import.meta.env.VITE_FACEBOOK_APP_ID || '';

  // Fetch backend config status
  useEffect(() => {
    apiClient.get('/auth/config-status')
      .then((res) => setConfigStatus(res.data))
      .catch(() => console.warn('Unable to load auth config status'));
  }, []);

  // Initialize Google Identity Services if client ID is provided
  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) return;

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      if ((window as any).google?.accounts?.id) {
        (window as any).google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: handleGoogleCredentialResponse,
        });
      }
    };
    document.head.appendChild(script);

    return () => {
      if (script.parentNode) script.parentNode.removeChild(script);
    };
  }, [GOOGLE_CLIENT_ID]);

  // Initialize Facebook SDK if app ID is provided
  useEffect(() => {
    if (!FACEBOOK_APP_ID) return;

    (window as any).fbAsyncInit = function () {
      (window as any).FB.init({
        appId: FACEBOOK_APP_ID,
        cookie: true,
        xfbml: true,
        version: 'v20.0',
      });
    };

    const script = document.createElement('script');
    script.src = 'https://connect.facebook.net/en_US/sdk.js';
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);

    return () => {
      if (script.parentNode) script.parentNode.removeChild(script);
    };
  }, [FACEBOOK_APP_ID]);

  const handleGoogleCredentialResponse = async (response: any) => {
    if (!response.credential) {
      setError('Google login cancelled or no token returned.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.post('/auth/oauth/google', {
        token: response.credential,
        provider: 'google',
      });
      login(res.data.user, res.data.accessToken);
      await executePendingAction();
      navigate(redirectUrl);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Google token validation failed on backend.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleAuth = async () => {
    setError(null);
    setOauthNotice(null);

    if (GOOGLE_CLIENT_ID && (window as any).google?.accounts?.id) {
      (window as any).google.accounts.id.prompt((notification: any) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${encodeURIComponent(window.location.origin + '/login')}&response_type=token%20id_token&scope=openid%20email%20profile&nonce=vyrox_${Date.now()}`;
          window.location.href = googleAuthUrl;
        }
      });
      return;
    }

    // Clear helpful notice
    setError('Google OAuth configuration missing: Please set VITE_GOOGLE_CLIENT_ID in apps/vyrox-web/.env & GOOGLE_CLIENT_ID in backend.');
  };

  const handleFacebookAuth = async () => {
    setError(null);
    setOauthNotice(null);

    if (FACEBOOK_APP_ID && (window as any).FB) {
      (window as any).FB.login((response: any) => {
        if (response.authResponse?.accessToken) {
          setLoading(true);
          apiClient.post('/auth/oauth/facebook', {
            token: response.authResponse.accessToken,
            provider: 'facebook',
          })
            .then(async (res) => {
              login(res.data.user, res.data.accessToken);
              await executePendingAction();
              navigate(redirectUrl);
            })
            .catch((err) => {
              setError(err.response?.data?.message || 'Facebook token validation failed.');
            })
            .finally(() => setLoading(false));
        } else {
          setError('Facebook login cancelled.');
        }
      }, { scope: 'public_profile,email' });
      return;
    }

    // Clear helpful notice
    setError('Facebook OAuth configuration missing: Please set VITE_FACEBOOK_APP_ID in apps/vyrox-web/.env & FACEBOOK_APP_ID in backend.');
  };

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await apiClient.post('/auth/login', {
        identifier: emailOrMobile,
        password,
      });

      login(res.data.user, res.data.accessToken);
      await executePendingAction();
      navigate(redirectUrl);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials. Please check your email/mobile and password.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendOtp = async () => {
    if (!otpMobile || otpMobile.length < 10) {
      setError('Please enter a valid 10-digit mobile number.');
      return;
    }
    setError(null);
    setLoading(true);

    try {
      const res = await apiClient.post('/auth/otp/send', { mobile: otpMobile });
      setOtpSent(true);
      if (res.data.devOtp) {
        setDevOtpHint(res.data.devOtp);
        setOtpCode(res.data.devOtp); // Auto-fill in development mode
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send OTP. Try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await apiClient.post('/auth/otp/verify', {
        mobile: otpMobile,
        otp: otpCode,
      });

      login(res.data.user, res.data.accessToken);
      await executePendingAction();
      navigate(redirectUrl);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid OTP code entered.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="bg-white rounded-3xl border border-slate-200 shadow-2xl p-6 sm:p-10 max-w-md w-full space-y-6">
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <Link to="/" className="inline-block">
            <div className="flex items-center justify-center gap-2.5">
              <img src="/vyrox-logo.png" alt="VYROX Logo" className="h-10 w-auto object-contain" />
              <span className="text-2xl font-black tracking-tight text-slate-900">
                VY<span className="text-[#FF6500]">ROX</span>
              </span>
            </div>
          </Link>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Sign in to your account</h1>
          <p className="text-xs font-bold text-[#FF6500] uppercase tracking-wider">
            Shop Smart. Compare Better. Live Better.
          </p>
        </div>

        {/* Error message */}
        {error && (
          <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-700 flex items-start gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* Social / OAuth Buttons */}
        <div className="space-y-2.5">
          <button
            type="button"
            onClick={handleGoogleAuth}
            disabled={loading}
            className="w-full py-2.5 px-4 border border-slate-200 hover:border-slate-300 hover:bg-slate-50 rounded-xl text-xs font-bold text-slate-700 flex items-center justify-center gap-2 transition-all shadow-xs"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path fill="#EA4335" d="M12 5c1.6 0 3 .6 4.1 1.6l3.1-3.1C17.3 1.7 14.8 1 12 1 7.5 1 3.7 3.6 1.9 7.3l3.7 2.9C6.5 7.3 9 5 12 5z" />
              <path fill="#4285F4" d="M23.5 12.3c0-.8-.1-1.6-.2-2.3H12v4.5h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5 3.7-8.8z" />
              <path fill="#FBBC05" d="M5.6 14.8c-.2-.7-.4-1.5-.4-2.3s.2-1.6.4-2.3L1.9 7.3C.7 9.7 0 12.3 0 15s.7 5.3 1.9 7.7l3.7-2.9z" />
              <path fill="#34A853" d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3 0-5.5-2.3-6.4-5.2L1.9 16C3.7 19.7 7.5 23 12 23z" />
            </svg>
            <span>Continue with Google</span>
          </button>

          <button
            type="button"
            onClick={handleFacebookAuth}
            disabled={loading}
            className="w-full py-2.5 px-4 border border-slate-200 hover:border-slate-300 hover:bg-slate-50 rounded-xl text-xs font-bold text-slate-700 flex items-center justify-center gap-2 transition-all shadow-xs"
          >
            <svg className="w-4 h-4 fill-[#1877F2]" viewBox="0 0 24 24">
              <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
            </svg>
            <span>Continue with Facebook</span>
          </button>
        </div>

        <div className="relative flex items-center justify-center">
          <div className="border-t border-slate-200 w-full"></div>
          <span className="bg-white px-3 text-[11px] font-bold text-slate-400 uppercase">OR</span>
        </div>

        {/* Tab Selection */}
        <div className="flex bg-slate-100 p-1 rounded-xl">
          <button
            type="button"
            onClick={() => setAuthMode('email')}
            className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all ${
              authMode === 'email' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-500'
            }`}
          >
            Email Login
          </button>
          <button
            type="button"
            onClick={() => setAuthMode('otp')}
            className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all ${
              authMode === 'otp' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-500'
            }`}
          >
            Mobile OTP
          </button>
        </div>

        {/* EMAIL LOGIN FORM */}
        {authMode === 'email' ? (
          <form onSubmit={handleEmailLogin} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Email or Mobile Number</label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={emailOrMobile}
                  onChange={(e) => setEmailOrMobile(e.target.value)}
                  placeholder="customer@vyrox.com or 9876543210"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              </div>
            </div>

            <div>
              <div className="flex justify-between items-center mb-1">
                <label className="text-xs font-semibold text-slate-700">Password</label>
                <Link to="/forgot-password" className="text-[11px] text-[#2B6CB0] font-semibold hover:underline">
                  Forgot Password?
                </Link>
              </div>
              <div className="relative">
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs sm:text-sm rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
            >
              <span>{loading ? 'Signing in...' : 'Sign In to VYROX'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        ) : (
          /* MOBILE OTP FORM */
          <form onSubmit={handleVerifyOtp} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Mobile Number</label>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <input
                    type="tel"
                    required
                    value={otpMobile}
                    onChange={(e) => setOtpMobile(e.target.value)}
                    placeholder="9876543210"
                    disabled={otpSent}
                    className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0] disabled:bg-slate-100"
                  />
                  <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                </div>
                {!otpSent && (
                  <button
                    type="button"
                    onClick={handleSendOtp}
                    disabled={loading || !otpMobile}
                    className="px-4 py-2.5 bg-[#FF6500] hover:bg-[#FF884B] text-white text-xs font-bold rounded-xl whitespace-nowrap transition-colors"
                  >
                    Send OTP
                  </button>
                )}
              </div>
            </div>

            {otpSent && (
              <div className="space-y-3">
                {devOtpHint && (
                  <div className="p-2.5 bg-emerald-50 border border-emerald-200 rounded-xl text-[11px] text-emerald-800 flex items-center gap-1 font-mono">
                    <Check className="w-3.5 h-3.5" /> Dev Mode OTP: <strong>{devOtpHint}</strong>
                  </div>
                )}
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Enter 6-Digit OTP</label>
                  <input
                    type="text"
                    required
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value)}
                    placeholder="123456"
                    className="w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-mono tracking-widest text-center outline-none focus:border-[#2B6CB0]"
                  />
                </div>

                <div className="flex justify-between items-center text-[11px]">
                  <button
                    type="button"
                    onClick={() => setOtpSent(false)}
                    className="text-slate-500 hover:underline"
                  >
                    Change Number
                  </button>
                  <button
                    type="button"
                    onClick={handleSendOtp}
                    className="text-[#2B6CB0] font-bold hover:underline"
                  >
                    Resend OTP
                  </button>
                </div>

                <button
                  type="submit"
                  disabled={loading || !otpCode}
                  className="w-full py-3 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs sm:text-sm rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
                >
                  <span>Verify OTP & Login</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            )}
          </form>
        )}

        {/* Continue as Guest */}
        <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-500">
          <button
            type="button"
            onClick={() => setShowConfigModal(true)}
            className="flex items-center gap-1 text-slate-500 hover:text-slate-800 font-medium"
          >
            <Settings className="w-3.5 h-3.5 text-slate-400" />
            <span>OAuth Credentials Guide</span>
          </button>
          <Link to="/" className="text-slate-500 hover:underline">
            Continue as Guest →
          </Link>
        </div>

        {/* Footer Link */}
        <div className="text-center text-xs text-slate-500 pt-1">
          <span>New to VYROX? </span>
          <Link to={`/register?redirect=${encodeURIComponent(redirectUrl)}`} className="text-[#FF6500] font-bold hover:underline">
            Create an Account
          </Link>
        </div>
      </div>

      {/* OAuth Credentials Guide Modal (Clean, No Google Maps) */}
      {showConfigModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 space-y-4 shadow-2xl border border-slate-200 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-[#FF6500]" />
                OAuth Configuration Details
              </h3>
              <button
                onClick={() => setShowConfigModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-sm"
              >
                ✕
              </button>
            </div>

            <p className="text-xs text-slate-600">
              VYROX OAuth status and developer parameters for Google Sign-In and Facebook Login:
            </p>

            <div className="space-y-3 text-xs">
              {/* Google Sign-In Status */}
              <div className="p-3 bg-slate-50 rounded-2xl border border-slate-200">
                <div className="flex items-center justify-between font-bold">
                  <span>Google Sign-In (OAuth 2.0)</span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] ${configStatus?.googleAuthConfigured ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'}`}>
                    {configStatus?.googleAuthConfigured ? 'CONFIGURED' : 'CREDENTIALS PENDING'}
                  </span>
                </div>
                <div className="text-[11px] text-slate-500 mt-1">
                  Env: <code className="bg-slate-200 px-1 py-0.5 rounded font-mono">GOOGLE_CLIENT_ID</code> / <code className="bg-slate-200 px-1 py-0.5 rounded font-mono">VITE_GOOGLE_CLIENT_ID</code>
                </div>
              </div>

              {/* Facebook Sign-In Status */}
              <div className="p-3 bg-slate-50 rounded-2xl border border-slate-200">
                <div className="flex items-center justify-between font-bold">
                  <span>Facebook Login (Graph API)</span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] ${configStatus?.facebookAuthConfigured ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'}`}>
                    {configStatus?.facebookAuthConfigured ? 'CONFIGURED' : 'CREDENTIALS PENDING'}
                  </span>
                </div>
                <div className="text-[11px] text-slate-500 mt-1">
                  Env: <code className="bg-slate-200 px-1 py-0.5 rounded font-mono">FACEBOOK_APP_ID</code> / <code className="bg-slate-200 px-1 py-0.5 rounded font-mono">VITE_FACEBOOK_APP_ID</code>
                </div>
              </div>
            </div>

            <div className="p-3 bg-blue-50 border border-blue-200 rounded-2xl text-[11px] text-blue-900 space-y-1">
              <div className="font-bold">Android Application Parameters:</div>
              <div>Package: <code className="font-mono font-bold">com.veltrion.vyrox</code></div>
              <div>Debug SHA-1: <code className="font-mono text-[10px] break-all">{configStatus?.androidDebugSha1 || '36:C9:D3:61:54:EA:19:86:86:2A:D5:15:AB:EA:A4:C2:BF:E4:97:6F'}</code></div>
              <div>Release SHA-1: <code className="font-mono text-[10px] break-all">{configStatus?.androidReleaseSha1 || 'DB:08:25:AA:1C:61:FC:96:37:7D:01:01:85:88:29:55:7B:3E:B4:CC'}</code></div>
              <div>Facebook Debug Key Hash: <code className="font-mono text-[10px]">NsnTYVTqGYaGKtUVq+qkwr/kl28=</code></div>
            </div>

            <button
              type="button"
              onClick={() => setShowConfigModal(false)}
              className="w-full py-2.5 bg-[#0B192C] text-white text-xs font-bold rounded-xl"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
