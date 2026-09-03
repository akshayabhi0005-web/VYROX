import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, Zap, ArrowRight, ShieldCheck, Tag, Gift, Flame, TrendingUp, Award } from 'lucide-react';
import { ProductCard } from '../components/ProductCard';
import { ProductSummary } from '../types';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

export const HomePage: React.FC = () => {
  const { user } = useAuth();
  const [topDeals, setTopDeals] = useState<ProductSummary[]>([]);
  const [trending, setTrending] = useState<ProductSummary[]>([]);
  const [bestSellers, setBestSellers] = useState<ProductSummary[]>([]);
  const [quickCommerce, setQuickCommerce] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchCatalog = async () => {
    try {
      const [dealsRes, trendRes, bestRes, qcRes] = await Promise.all([
        apiClient.get('/products/top-deals'),
        apiClient.get('/products/trending'),
        apiClient.get('/products/best-sellers'),
        apiClient.get('/products/quick-commerce'),
      ]);
      setTopDeals(dealsRes.data || []);
      setTrending(trendRes.data || []);
      setBestSellers(bestRes.data || []);
      setQuickCommerce(qcRes.data || []);
    } catch (err) {
      console.error('Failed to load home catalog', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCatalog();
  }, []);

  return (
    <div className="space-y-8 pb-16">
      {/* Hero Banner Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 pt-4">
        <div className="relative rounded-3xl overflow-hidden bg-gradient-to-r from-[#0B192C] via-[#1E3E62] to-[#2B6CB0] text-white p-6 sm:p-12 shadow-2xl border border-slate-700/50">
          <div className="relative z-10 max-w-xl space-y-4">
            <div className="inline-flex items-center gap-2 bg-[#FF6500] text-white px-3 py-1 rounded-full text-xs font-black tracking-wider uppercase shadow-md">
              <Sparkles className="w-3.5 h-3.5" /> VYROX SUPER SALE 2026
            </div>
            <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
              Shop Smart. <br />
              <span className="text-[#00D2FF]">Compare Better.</span> <br />
              Live Better.
            </h1>
            <p className="text-slate-200 text-xs sm:text-sm leading-relaxed">
              Discover flagship laptops, smartphones, 15-minute quick delivery groceries, and AI-powered recommendations at unmatchable prices.
            </p>
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <Link
                to="/top-deals"
                className="px-6 py-3 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs sm:text-sm rounded-xl shadow-lg transition-all flex items-center gap-2"
              >
                <span>Explore Top Deals</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/compare"
                className="px-5 py-3 bg-white/10 hover:bg-white/20 text-white font-semibold text-xs sm:text-sm rounded-xl backdrop-blur-md transition-all"
              >
                4-Way Compare
              </Link>
            </div>
          </div>

          {/* Decorative Hero Elements */}
          <div className="hidden lg:block absolute right-12 top-1/2 -translate-y-1/2">
            <div className="relative w-80 h-80 rounded-full bg-gradient-to-br from-cyan-400/20 to-orange-500/20 blur-2xl -z-10"></div>
            <img
              src="/vyrox-logo.png"
              alt="VYROX Hero"
              className="w-72 h-auto object-contain drop-shadow-2xl animate-pulse"
            />
          </div>
        </div>
      </div>

      {/* Quick Commerce 15-Minute Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="bg-gradient-to-r from-amber-500/10 via-orange-500/10 to-rose-500/10 rounded-3xl p-6 border border-amber-200/80">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-6">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-[#FF6500] text-white flex items-center justify-center shadow-md">
                <Zap className="w-5 h-5 fill-current" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  VYROX Quick Commerce
                  <span className="bg-[#0B192C] text-white text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase">15-MIN</span>
                </h2>
                <p className="text-xs text-slate-500">Delivered directly from nearest darkstore to your doorstep</p>
              </div>
            </div>
            <Link to="/top-deals?category=quick-commerce" className="text-xs font-bold text-[#FF6500] hover:underline flex items-center gap-1">
              View All 15-Min Items <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {quickCommerce.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCartSuccess={fetchCatalog} />
            ))}
          </div>
        </div>
      </section>

      {/* Top Deals Section (Inspired by reference layout) */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="p-1.5 bg-orange-100 text-[#FF6500] rounded-lg">
              <Flame className="w-5 h-5 fill-current" />
            </div>
            <div>
              <h2 className="text-lg sm:text-xl font-bold text-slate-900">All Your Deals in One Place</h2>
              <p className="text-xs text-slate-500">Handpicked massive discounts across top brands</p>
            </div>
          </div>
          <Link to="/top-deals" className="text-xs font-bold text-[#FF6500] hover:underline flex items-center gap-1">
            See All Deals <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 animate-pulse">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="bg-slate-200 rounded-2xl h-72"></div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {topDeals.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCartSuccess={fetchCatalog} />
            ))}
          </div>
        )}
      </section>

      {/* Reference Inspired: "Still looking for these?" Personalized Recommendations */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-bold text-slate-900">
                {user ? `${user.fullName?.split(' ')[0]}, still looking for these?` : 'Recommended For You'}
              </h2>
              <p className="text-xs text-slate-500">Based on your interests and trending catalog searches</p>
            </div>
            <span className="text-[11px] bg-blue-50 text-[#2B6CB0] font-semibold px-2.5 py-1 rounded-full border border-blue-200">
              Personalized AI Picks
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {trending.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCartSuccess={fetchCatalog} />
            ))}
          </div>
        </div>
      </section>

      {/* Best Sellers Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="p-1.5 bg-blue-100 text-[#2B6CB0] rounded-lg">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg sm:text-xl font-bold text-slate-900">Best Sellers</h2>
              <p className="text-xs text-slate-500">Highest rated products loved by VYROX shoppers</p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {bestSellers.map((product) => (
            <ProductCard key={product.id} product={product} onAddToCartSuccess={fetchCatalog} />
          ))}
        </div>
      </section>

      {/* Coupons & VYROX Coins Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Coupon Banner */}
          <div className="bg-gradient-to-r from-purple-900 to-indigo-900 text-white rounded-3xl p-6 flex items-center justify-between shadow-md">
            <div className="space-y-1.5">
              <span className="text-[10px] font-bold bg-white/20 text-purple-200 px-2 py-0.5 rounded uppercase">COUPON CODE</span>
              <h3 className="text-xl font-black tracking-tight text-white">Use Code: VYROX100</h3>
              <p className="text-xs text-purple-200">Flat ₹100 instant discount on orders above ₹499</p>
            </div>
            <Tag className="w-12 h-12 text-purple-300/40 flex-shrink-0" />
          </div>

          {/* Coins Banner */}
          <div className="bg-gradient-to-r from-amber-600 to-orange-700 text-white rounded-3xl p-6 flex items-center justify-between shadow-md">
            <div className="space-y-1.5">
              <span className="text-[10px] font-bold bg-white/20 text-amber-200 px-2 py-0.5 rounded uppercase">VYROX REWARDS</span>
              <h3 className="text-xl font-black tracking-tight text-white">Earn 5% Coins on Every Order</h3>
              <p className="text-xs text-amber-100">Redeem coins directly at checkout (1 Coin = ₹1)</p>
            </div>
            <Gift className="w-12 h-12 text-amber-200/40 flex-shrink-0" />
          </div>
        </div>
      </section>
    </div>
  );
};
