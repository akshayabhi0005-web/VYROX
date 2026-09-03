import React, { useEffect, useState } from 'react';
import { Truck, MapPin, KeyRound, CheckCircle2, Phone } from 'lucide-react';
import { apiClient } from '../api/apiClient';

export const DeliveryPage: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [orderId, setOrderId] = useState('');
  const [otp, setOtp] = useState('');
  const [resultMsg, setResultMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient.get('/delivery/dashboard')
      .then((res) => setStats(res.data))
      .catch(() => {
        setStats({
          partnerName: 'Ramesh Kumar',
          vehicleNumber: 'KA-01-VY-4098',
          rating: 4.92,
          completedDeliveries: 348,
          assignedOrders: [],
        });
      })
      .finally(() => setLoading(false));
  }, []);

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setResultMsg(null);
    try {
      const res = await apiClient.post(`/delivery/verify-otp?orderId=${orderId}&otp=${otp}`);
      setResultMsg(res.data.message || 'Doorstep delivery verified successfully!');
      setOtp('');
    } catch (err: any) {
      setResultMsg('Error: ' + (err.response?.data?.message || 'Invalid Doorstep OTP.'));
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 sm:p-8 rounded-3xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#00D2FF] text-xs font-black tracking-widest uppercase mb-1">
            <Truck className="w-4 h-4" /> DELIVERY PARTNER PORTAL
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">{stats?.partnerName || 'Ramesh Kumar'}</h1>
          <p className="text-xs text-slate-300 mt-1">Vehicle: {stats?.vehicleNumber} • Rating: {stats?.rating}★ ({stats?.completedDeliveries} completed)</p>
        </div>

        <div className="bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 px-3.5 py-1.5 rounded-full text-xs font-bold flex items-center gap-1.5 self-start md:self-auto">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping"></span>
          <span>Duty Status: ONLINE (Accepting Orders)</span>
        </div>
      </div>

      {/* Doorstep OTP Verification Box */}
      <div className="bg-white rounded-3xl border border-slate-200 p-6 max-w-lg shadow-xs space-y-4">
        <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
          <KeyRound className="w-4 h-4 text-[#FF6500]" /> Verify Doorstep Customer OTP
        </h3>
        <p className="text-xs text-slate-500">
          Enter the 4-digit OTP provided by the customer at doorstep to mark the delivery as DELIVERED.
        </p>

        {resultMsg && (
          <div className={`p-3 rounded-xl text-xs font-bold ${
            resultMsg.startsWith('Error') ? 'bg-rose-50 text-rose-700 border border-rose-200' : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
          }`}>
            {resultMsg}
          </div>
        )}

        <form onSubmit={handleVerifyOtp} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Order ID (Internal DB ID)</label>
            <input
              type="number"
              required
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
              placeholder="e.g. 1"
              className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Customer 4-Digit OTP</label>
            <input
              type="text"
              required
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="e.g. 4829"
              className="w-full px-3 py-2.5 border border-slate-200 rounded-xl text-base font-mono tracking-widest text-center outline-none focus:border-[#2B6CB0]"
            />
          </div>

          <button
            type="submit"
            className="w-full py-3 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs rounded-xl shadow-md transition-all"
          >
            Confirm & Complete Delivery
          </button>
        </form>
      </div>
    </div>
  );
};
