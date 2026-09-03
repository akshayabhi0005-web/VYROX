import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Sparkles, Smartphone, Laptop, Shirt, Home, Tv, Heart, Zap, Award
} from 'lucide-react';

const categories = [
  { name: 'For You', slug: '', icon: Sparkles, color: 'text-amber-500 bg-amber-50' },
  { name: 'Mobiles', slug: 'mobiles', icon: Smartphone, color: 'text-blue-500 bg-blue-50' },
  { name: 'Electronics', slug: 'electronics', icon: Laptop, color: 'text-indigo-500 bg-indigo-50' },
  { name: 'Fashion', slug: 'fashion', icon: Shirt, color: 'text-pink-500 bg-pink-50' },
  { name: 'Home', slug: 'home-living', icon: Home, color: 'text-emerald-500 bg-emerald-50' },
  { name: 'Appliances', slug: 'appliances', icon: Tv, color: 'text-purple-500 bg-purple-50' },
  { name: 'Beauty', slug: 'beauty', icon: Heart, color: 'text-rose-500 bg-rose-50' },
  { name: 'Quick 15-Min', slug: 'quick-commerce', icon: Zap, color: 'text-orange-500 bg-orange-50' },
  { name: 'Top Deals', slug: 'top-deals', icon: Award, color: 'text-[#FF6500] bg-orange-100' },
];

export const CategoryBar: React.FC = () => {
  return (
    <div className="bg-white border-b border-slate-200 py-2.5 px-4 overflow-x-auto shadow-sm no-scrollbar">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-6 min-w-max">
        {categories.map((cat, idx) => {
          const Icon = cat.icon;
          const targetUrl = cat.slug === 'top-deals' ? '/top-deals' : (cat.slug ? `/top-deals?category=${cat.slug}` : '/');
          return (
            <Link
              key={idx}
              to={targetUrl}
              className="flex flex-col items-center gap-1.5 group cursor-pointer px-2 py-1 rounded-xl transition-transform hover:-translate-y-0.5"
            >
              <div className={`w-11 h-11 rounded-2xl flex items-center justify-center ${cat.color} group-hover:scale-105 transition-all shadow-sm border border-slate-100`}>
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] font-semibold text-slate-700 group-hover:text-[#0B192C] tracking-tight">
                {cat.name}
              </span>
            </Link>
          );
        })}
      </div>
    </div>
  );
};
