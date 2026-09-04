import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Sparkles, Filter, SlidersHorizontal, ArrowUpDown, Tag } from 'lucide-react';
import { ProductCard } from '../components/ProductCard';
import { ProductSummary } from '../types';
import { apiClient } from '../api/apiClient';

import { fallbackSummaryList } from '../data/fallbackCatalog';

const dealCategories = [
  { label: 'All Deals', slug: '' },
  { label: 'Mobiles', slug: 'mobiles' },
  { label: 'Electronics', slug: 'electronics' },
  { label: 'Fashion', slug: 'fashion' },
  { label: 'Appliances', slug: 'appliances' },
  { label: 'Quick 15-Min', slug: 'quick-commerce' },
];

export const TopDealsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummary[]>(fallbackSummaryList);
  const [loading, setLoading] = useState(false);

  const selectedCategory = searchParams.get('category') || '';
  const queryParam = searchParams.get('query') || '';
  const [minDiscount, setMinDiscount] = useState<number | null>(null);
  const [minRating, setMinRating] = useState<number | null>(null);
  const [sortBy, setSortBy] = useState('popularity');

  const fetchDeals = async () => {
    try {
      const params: any = {
        isTopDeal: true,
        sortBy,
      };

      if (queryParam) params.query = queryParam;
      if (minRating) params.minRating = minRating;

      const res = await apiClient.get('/products', { params });
      let list: ProductSummary[] = (res.data && (res.data.content || res.data)) || [];

      if (!list.length) {
        list = [...fallbackSummaryList];
      }

      // Category filter in memory if slug
      if (selectedCategory) {
        list = list.filter((p) =>
          p.categoryName?.toLowerCase().includes(selectedCategory.toLowerCase()) ||
          (selectedCategory === 'quick-commerce' && p.isQuickCommerceEligible)
        );
      }

      // Discount filter
      if (minDiscount) {
        list = list.filter((p) => p.discountPercentage >= minDiscount);
      }

      setProducts(list);
    } catch (err) {
      console.warn('Deals API waking up; using built-in catalog');
      let list = [...fallbackSummaryList];
      if (selectedCategory) {
        list = list.filter((p) =>
          p.categoryName?.toLowerCase().includes(selectedCategory.toLowerCase()) ||
          (selectedCategory === 'quick-commerce' && p.isQuickCommerceEligible)
        );
      }
      if (minDiscount) {
        list = list.filter((p) => p.discountPercentage >= minDiscount);
      }
      setProducts(list);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeals();
  }, [selectedCategory, queryParam, minDiscount, minRating, sortBy]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Header & Title */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 rounded-3xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#FF6500] text-xs font-black tracking-widest uppercase mb-1">
            <Sparkles className="w-4 h-4" /> VYROX TOP DEALS
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">
            {queryParam ? `Deals for "${queryParam}"` : 'All Your Deals in One Place'}
          </h1>
          <p className="text-xs text-slate-300 mt-1">
            Showing verified authentic products with high seller ratings and exclusive instant discounts.
          </p>
        </div>

        {/* Category Filter Tabs */}
        <div className="flex flex-wrap gap-2">
          {dealCategories.map((c) => {
            const isActive = selectedCategory === c.slug;
            return (
              <button
                key={c.slug}
                onClick={() => {
                  const newParams = new URLSearchParams(searchParams);
                  if (c.slug) newParams.set('category', c.slug);
                  else newParams.delete('category');
                  setSearchParams(newParams);
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                  isActive
                    ? 'bg-[#FF6500] text-white shadow-md'
                    : 'bg-white/10 hover:bg-white/20 text-slate-200'
                }`}
              >
                {c.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Filter & Sort Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-4">
        {/* Quick Discount Filters */}
        <div className="flex flex-wrap items-center gap-2 text-xs">
          <span className="font-bold text-slate-700 flex items-center gap-1">
            <Filter className="w-3.5 h-3.5" /> Discount:
          </span>
          {[
            { label: 'All', val: null },
            { label: '10%+ Off', val: 10 },
            { label: '20%+ Off', val: 20 },
            { label: '30%+ Off', val: 30 },
          ].map((item, idx) => (
            <button
              key={idx}
              onClick={() => setMinDiscount(item.val)}
              className={`px-2.5 py-1 rounded-lg font-medium transition-all ${
                minDiscount === item.val
                  ? 'bg-[#0B192C] text-white font-bold'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
              }`}
            >
              {item.label}
            </button>
          ))}

          {/* Rating Filter */}
          <span className="font-bold text-slate-700 ml-2">Rating:</span>
          {[
            { label: 'All', val: null },
            { label: '4.5★+', val: 4.5 },
            { label: '4.0★+', val: 4.0 },
          ].map((item, idx) => (
            <button
              key={idx}
              onClick={() => setMinRating(item.val)}
              className={`px-2.5 py-1 rounded-lg font-medium transition-all ${
                minRating === item.val
                  ? 'bg-emerald-700 text-white font-bold'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        {/* Sort Dropdown */}
        <div className="flex items-center gap-2 text-xs">
          <ArrowUpDown className="w-3.5 h-3.5 text-slate-500" />
          <span className="font-bold text-slate-700">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="bg-slate-100 border border-slate-200 rounded-lg px-2.5 py-1 text-xs font-semibold text-slate-800 outline-none focus:border-[#2B6CB0]"
          >
            <option value="popularity">Popularity</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
            <option value="discount">Highest Discount</option>
            <option value="newest">Newest Arrivals</option>
          </select>
        </div>
      </div>

      {/* Product Deals Grid */}
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 animate-pulse">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
            <div key={i} className="bg-slate-200 rounded-2xl h-80"></div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
          <Tag className="w-12 h-12 text-slate-300 mx-auto" />
          <h3 className="font-bold text-base text-slate-800">No deals found matching your filter</h3>
          <p className="text-xs text-slate-500">Try clearing filters or search for another category.</p>
          <button
            onClick={() => {
              setMinDiscount(null);
              setMinRating(null);
              setSearchParams({});
            }}
            className="px-4 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} onAddToCartSuccess={fetchDeals} />
          ))}
        </div>
      )}
    </div>
  );
};
