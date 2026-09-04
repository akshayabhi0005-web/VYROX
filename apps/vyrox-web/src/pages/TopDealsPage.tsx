import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Sparkles, Filter, SlidersHorizontal, ArrowUpDown, Tag, RotateCcw } from 'lucide-react';
import { ProductCard } from '../components/ProductCard';
import { ProductSummary } from '../types';
import { apiClient } from '../api/apiClient';

import { fallbackSummaryList } from '../data/fallbackCatalog';

const dealCategories = [
  { label: 'All Deals', slug: '' },
  { label: 'Mobiles', slug: 'mobiles' },
  { label: 'Electronics', slug: 'electronics' },
  { label: 'Fashion', slug: 'fashion' },
  { label: 'Home', slug: 'home-living' },
  { label: 'Appliances', slug: 'appliances' },
  { label: 'Beauty', slug: 'beauty' },
  { label: 'Quick 15-Min', slug: 'quick-commerce' },
];

function filterAndSortProducts(
  sourceList: ProductSummary[],
  selectedCategory: string,
  queryParam: string,
  minDiscount: number | null,
  minRating: number | null,
  sortBy: string
): ProductSummary[] {
  let list = [...sourceList];

  // 1. Search Query
  if (queryParam) {
    const q = queryParam.toLowerCase();
    list = list.filter(
      (p) =>
        p.title.toLowerCase().includes(q) ||
        (p.brandName && p.brandName.toLowerCase().includes(q)) ||
        (p.categoryName && p.categoryName.toLowerCase().includes(q))
    );
  }

  // 2. Category Filter
  if (selectedCategory) {
    const norm = selectedCategory.toLowerCase();
    if (norm === 'mobiles' || norm === 'smartphones') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('mobile') ||
          p.categoryName?.toLowerCase().includes('smartphone') ||
          p.categoryId === 1
      );
    } else if (norm === 'electronics' || norm === 'laptops') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('electronic') ||
          p.categoryName?.toLowerCase().includes('laptop') ||
          p.categoryName?.toLowerCase().includes('audio') ||
          p.categoryId === 2
      );
    } else if (norm === 'fashion' || norm === 'clothing') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('fashion') ||
          p.categoryName?.toLowerCase().includes('shoe') ||
          p.categoryId === 4
      );
    } else if (norm === 'home' || norm === 'home-living') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('home') ||
          p.categoryName?.toLowerCase().includes('furniture') ||
          p.categoryId === 6
      );
    } else if (norm === 'appliances') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('appliance') ||
          p.categoryId === 5
      );
    } else if (norm === 'beauty') {
      list = list.filter(
        (p) =>
          p.categoryName?.toLowerCase().includes('beauty') ||
          p.categoryName?.toLowerCase().includes('skin') ||
          p.categoryId === 8
      );
    } else if (norm === 'quick-commerce' || norm === 'groceries') {
      list = list.filter(
        (p) => p.isQuickCommerceEligible || p.categoryName?.toLowerCase().includes('grocer') || p.categoryId === 7
      );
    } else {
      list = list.filter((p) => p.categoryName?.toLowerCase().includes(norm));
    }
  }

  // 3. Discount Filter
  if (minDiscount) {
    list = list.filter((p) => p.discountPercentage >= minDiscount);
  }

  // 4. Rating Filter
  if (minRating) {
    list = list.filter((p) => p.averageRating >= minRating);
  }

  // 5. Sorting
  if (sortBy === 'price-low') {
    list.sort((a, b) => a.sellingPrice - b.sellingPrice);
  } else if (sortBy === 'price-high') {
    list.sort((a, b) => b.sellingPrice - a.sellingPrice);
  } else if (sortBy === 'rating') {
    list.sort((a, b) => b.averageRating - a.averageRating);
  } else if (sortBy === 'discount') {
    list.sort((a, b) => b.discountPercentage - a.discountPercentage);
  } else {
    // Popularity / default
    list.sort((a, b) => b.reviewCount - a.reviewCount);
  }

  return list;
}

export const TopDealsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedCategory = searchParams.get('category') || '';
  const queryParam = searchParams.get('query') || '';

  const [minDiscount, setMinDiscount] = useState<number | null>(null);
  const [minRating, setMinRating] = useState<number | null>(null);
  const [sortBy, setSortBy] = useState('popularity');
  const [allProducts, setAllProducts] = useState<ProductSummary[]>(fallbackSummaryList);
  const [products, setProducts] = useState<ProductSummary[]>(() =>
    filterAndSortProducts(fallbackSummaryList, selectedCategory, queryParam, minDiscount, minRating, sortBy)
  );
  const [loading, setLoading] = useState(false);

  const fetchDeals = async () => {
    try {
      const res = await apiClient.get('/products');
      const list: ProductSummary[] = (res.data && (res.data.content || res.data)) || [];
      if (Array.isArray(list) && list.length > 0) {
        setAllProducts(list);
        setProducts(
          filterAndSortProducts(list, selectedCategory, queryParam, minDiscount, minRating, sortBy)
        );
      } else {
        setProducts(
          filterAndSortProducts(fallbackSummaryList, selectedCategory, queryParam, minDiscount, minRating, sortBy)
        );
      }
    } catch (err) {
      setProducts(
        filterAndSortProducts(allProducts, selectedCategory, queryParam, minDiscount, minRating, sortBy)
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeals();
  }, [selectedCategory, queryParam, minDiscount, minRating, sortBy]);

  const handleCategoryClick = (slug: string) => {
    const newParams = new URLSearchParams(searchParams);
    if (slug) {
      newParams.set('category', slug);
    } else {
      newParams.delete('category');
    }
    setSearchParams(newParams);
  };

  const handleResetFilters = () => {
    setMinDiscount(null);
    setMinRating(null);
    setSortBy('popularity');
    const newParams = new URLSearchParams();
    setSearchParams(newParams);
  };

  const isCurrentCategory = (slug: string) => {
    if (!slug && !selectedCategory) return true;
    if (slug === 'home-living' && (selectedCategory === 'home' || selectedCategory === 'home-living')) return true;
    return selectedCategory.toLowerCase() === slug.toLowerCase();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Header & Title */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 rounded-3xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#FF6500] text-xs font-black tracking-widest uppercase mb-1">
            <Sparkles className="w-4 h-4" /> VYROX TOP DEALS
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">Today's Best E-Commerce Deals</h1>
          <p className="text-xs text-slate-300 mt-1">
            Unbeatable limited-time discounts up to 50% Off across flagship categories.
          </p>
        </div>

        {/* Reset Filters CTA */}
        {(minDiscount !== null || minRating !== null || selectedCategory || sortBy !== 'popularity') && (
          <button
            onClick={handleResetFilters}
            className="self-start md:self-auto px-3.5 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" /> Reset Filters
          </button>
        )}
      </div>

      {/* Category Pills Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 no-scrollbar">
        {dealCategories.map((cat, idx) => (
          <button
            key={idx}
            onClick={() => handleCategoryClick(cat.slug)}
            className={`px-4 py-2 rounded-2xl text-xs font-bold whitespace-nowrap transition-all shadow-xs ${
              isCurrentCategory(cat.slug)
                ? 'bg-[#0B192C] text-white shadow-md'
                : 'bg-white text-slate-700 hover:bg-slate-100 border border-slate-200'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {/* Filter and Sorting Controls */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-1.5 text-xs font-bold text-slate-700">
            <SlidersHorizontal className="w-4 h-4 text-slate-500" /> Filters:
          </div>

          {/* Discount Pills */}
          <div className="flex items-center gap-1.5">
            {[
              { label: 'All Discounts', val: null },
              { label: '10%+ Off', val: 10 },
              { label: '20%+ Off', val: 20 },
              { label: '30%+ Off', val: 30 },
            ].map((d, i) => (
              <button
                key={i}
                onClick={() => setMinDiscount(d.val)}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors ${
                  minDiscount === d.val
                    ? 'bg-[#FF6500] text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {d.label}
              </button>
            ))}
          </div>

          <div className="h-4 w-px bg-slate-200 hidden sm:block"></div>

          {/* Rating Pills */}
          <div className="flex items-center gap-1.5">
            {[
              { label: 'All Ratings', val: null },
              { label: '4.5★+', val: 4.5 },
              { label: '4.0★+', val: 4.0 },
            ].map((r, i) => (
              <button
                key={i}
                onClick={() => setMinRating(r.val)}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors ${
                  minRating === r.val
                    ? 'bg-emerald-700 text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {dRatingLabel(r.val, r.label)}
              </button>
            ))}
          </div>
        </div>

        {/* Sort Select */}
        <div className="flex items-center gap-2">
          <ArrowUpDown className="w-4 h-4 text-slate-500" />
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5 text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#0B192C]"
          >
            <option value="popularity">Sort by: Popularity</option>
            <option value="price-low">Price: Low to High</option>
            <option value="price-high">Price: High to Low</option>
            <option value="rating">Customer Rating</option>
            <option value="discount">Discount Percentage</option>
          </select>
        </div>
      </div>

      {/* Results Header */}
      <div className="flex items-center justify-between text-xs text-slate-500 font-medium px-1">
        <span>Showing <strong>{products.length}</strong> deals found</span>
      </div>

      {/* Products Grid */}
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
            <div key={n} className="bg-white rounded-2xl p-4 border border-slate-200 space-y-3 animate-pulse">
              <div className="w-full h-40 bg-slate-200 rounded-xl"></div>
              <div className="h-4 bg-slate-200 rounded w-3/4"></div>
              <div className="h-4 bg-slate-200 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 space-y-3">
          <Tag className="w-12 h-12 text-slate-300 mx-auto" />
          <h3 className="font-bold text-slate-800 text-base">No deals found matching your specific filter</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Try adjusting your discount or rating filters to view all available deals in this category.
          </p>
          <button
            onClick={handleResetFilters}
            className="px-4 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl shadow-xs hover:bg-[#1E3E62] transition-colors"
          >
            Show All Deals
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
};

function dRatingLabel(val: number | null, fallback: string) {
  if (val === 4.5) return '4.5★+';
  if (val === 4.0) return '4.0★+';
  return fallback;
}
