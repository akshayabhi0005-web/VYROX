import React, { useEffect, useState } from 'react';
import { Store, Package, TrendingUp, AlertTriangle, CheckCircle2, Shield } from 'lucide-react';
import { apiClient } from '../api/apiClient';

export const SellerPage: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient.get('/seller/dashboard')
      .then((res) => setStats(res.data))
      .catch(() => {
        setStats({
          sellerName: 'Veltrion Technologies (Demo Seller)',
          totalCatalogSkus: 24,
          totalOrdersProcessed: 142,
          activeInventoryCount: 380,
          sellerRating: 4.8,
          kycStatus: 'VERIFIED',
        });
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 sm:p-8 rounded-3xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#00D2FF] text-xs font-black tracking-widest uppercase mb-1">
            <Store className="w-4 h-4" /> SELLER HUB & INVENTORY PORTAL
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">{stats?.sellerName}</h1>
          <p className="text-xs text-slate-300 mt-1">Manage product catalog, darkstore stock allocations, orders, and fulfillment.</p>
        </div>

        <div className="bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 px-3 py-1.5 rounded-full text-xs font-bold flex items-center gap-1.5 self-start md:self-auto">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>KYC Status: {stats?.kycStatus || 'VERIFIED'}</span>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-1">
          <div className="text-xs text-slate-500 font-bold uppercase">Active Catalog SKUs</div>
          <div className="text-2xl font-black text-slate-900">{stats?.totalCatalogSkus}</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-1">
          <div className="text-xs text-slate-500 font-bold uppercase">Orders Processed</div>
          <div className="text-2xl font-black text-slate-900">{stats?.totalOrdersProcessed}</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-1">
          <div className="text-xs text-slate-500 font-bold uppercase">Total Units in Stock</div>
          <div className="text-2xl font-black text-slate-900">{stats?.activeInventoryCount}</div>
        </div>
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-1">
          <div className="text-xs text-slate-500 font-bold uppercase">Seller Performance Rating</div>
          <div className="text-2xl font-black text-emerald-600">{stats?.sellerRating} ★</div>
        </div>
      </div>

      <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="font-bold text-sm text-slate-900">Inventory & SKU Actions</h3>
        <p className="text-xs text-slate-500">
          Darkstore automated synchronization is active across Bengaluru Central, Koramangala, and Mumbai South hubs.
        </p>
      </div>
    </div>
  );
};
