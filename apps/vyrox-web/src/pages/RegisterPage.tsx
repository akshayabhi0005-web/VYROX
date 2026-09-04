import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { User, Mail, Lock, Phone, ArrowRight, AlertCircle, ShieldCheck } from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login, executePendingAction } = useAuth();
  const redirectParam = searchParams.get('redirect');
  const redirectUrl = redirectParam ? (redirectParam.startsWith('/') ? redirectParam : decodeURIComponent(redirectParam)) : '/';

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const cleanName = fullName.trim();
    const cleanEmail = email.trim().toLowerCase();
    const cleanMobile = mobile.replace(/\D/g, '').slice(-10);

    if (!cleanName) {
      setError('Please enter your full name.');
      return;
    }
    if (!cleanEmail || !cleanEmail.includes('@')) {
      setError('Please enter a valid email address.');
      return;
    }
    if (!cleanMobile || cleanMobile.length !== 10) {
      setError('Please enter a valid 10-digit mobile number.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (!termsAccepted) {
      setError('Please agree to the VYROX Terms of Use and Privacy Policy.');
      return;
    }

    setLoading(true);

    try {
      console.log('[REGISTER REQUEST]', { fullName: cleanName, email: cleanEmail, mobile: cleanMobile });

      const res = await apiClient.post('/auth/register', {
        fullName: cleanName,
        email: cleanEmail,
        mobile: cleanMobile,
        password,
      });

      console.log('[REGISTER RESPONSE]', { status: res.status, user: res.data?.user });

      if (res.data && res.data.user && res.data.accessToken) {
        login(res.data.user, res.data.accessToken);
        try {
          await executePendingAction();
        } catch (e) {
          console.warn('Pending action sync warning', e);
        }
        navigate(redirectUrl, { replace: true });
      } else {
        throw new Error('Invalid response received from authentication server.');
      }
    } catch (err: any) {
      console.error('[REGISTER ERROR]', err.response?.data || err.message || err);
      let msg = 'Registration failed. Please check your details and try again.';
      if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
        msg = 'Unable to connect to VYROX server. Request timed out. Please try again.';
      } else if (err.response?.data?.message) {
        msg = err.response.data.message;
      } else if (err.response?.data?.error) {
        msg = err.response.data.error;
      } else if (!err.response) {
        msg = 'Unable to connect to VYROX server. Please check your network connection.';
      }
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="bg-white rounded-3xl border border-slate-200 shadow-2xl p-6 sm:p-10 max-w-md w-full space-y-6">
        <div className="text-center space-y-2">
          <Link to="/" className="inline-block">
            <img src="/vyrox-logo.png" alt="VYROX Logo" className="h-12 w-auto mx-auto object-contain" />
          </Link>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Create VYROX Account</h1>
          <p className="text-xs text-slate-500">
            Join millions of smart shoppers and get <strong>100 Free VYROX Coins</strong> instantly!
          </p>
        </div>

        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-3.5">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
            <div className="relative">
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Akshay N"
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
            <div className="relative">
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mobile Number</label>
            <div className="relative">
              <input
                type="tel"
                required
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="9876543210"
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
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

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Confirm</label>
              <div className="relative">
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              </div>
            </div>
          </div>

          <label className="flex items-start gap-2 text-xs text-slate-600 pt-1 cursor-pointer">
            <input
              type="checkbox"
              checked={termsAccepted}
              onChange={(e) => setTermsAccepted(e.target.checked)}
              className="mt-0.5 text-[#0B192C] rounded"
            />
            <span>I agree to the VYROX Terms of Use and Privacy Policy.</span>
          </label>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs sm:text-sm rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
          >
            <span>{loading ? 'Creating Account...' : 'Create VYROX Account'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="text-center text-xs text-slate-500 pt-2 border-t border-slate-100">
          <span>Already have an account? </span>
          <Link to={`/login?redirect=${encodeURIComponent(redirectUrl)}`} className="text-[#2B6CB0] font-bold hover:underline">
            Login here
          </Link>
        </div>
      </div>
    </div>
  );
};
