import React from 'react';
import { Link } from 'react-router-dom';
import { Shield, Truck, Zap, Headphones, Heart } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-[#0B192C] text-slate-400 text-xs border-t border-slate-800 mt-20">
      {/* Features Bar */}
      <div className="border-b border-slate-800 py-8 px-4">
        <div className="max-w-7xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-6 text-center md:text-left">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-800 text-[#00D2FF] flex items-center justify-center flex-shrink-0">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-white text-sm">15-Min Quick Commerce</h4>
              <p className="text-[11px] text-slate-400">Darkstore instant delivery</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-800 text-[#FF6500] flex items-center justify-center flex-shrink-0">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-white text-sm">100% Genuine Products</h4>
              <p className="text-[11px] text-slate-400">Brand certified warranty</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-800 text-emerald-400 flex items-center justify-center flex-shrink-0">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-white text-sm">Live GPS Tracking</h4>
              <p className="text-[11px] text-slate-400">Realtime doorstep visibility</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-800 text-amber-400 flex items-center justify-center flex-shrink-0">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-white text-sm">24x7 Smart Support</h4>
              <p className="text-[11px] text-slate-400">Instant AI & human assistance</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 grid grid-cols-2 md:grid-cols-5 gap-8">
        <div className="col-span-2 space-y-3">
          <div className="flex items-center gap-2.5">
            <img src="/vyrox-logo.png" alt="VYROX Logo" className="h-8 w-auto" />
            <span className="text-xl font-black text-white tracking-tight">VYROX</span>
          </div>
          <p className="text-slate-400 text-xs max-w-sm leading-relaxed">
            SHOP SMART. COMPARE BETTER. LIVE BETTER.
            <br />
            An advanced next-generation intelligent commerce platform built by <strong>Team VELTRION</strong>.
          </p>
          <div className="text-[11px] text-slate-400 pt-2">
            Android Application: <code className="text-[#00D2FF] font-mono">com.veltrion.vyrox</code>
          </div>
        </div>

        <div>
          <h4 className="font-bold text-white uppercase tracking-wider text-[11px] mb-3">Shop Categories</h4>
          <ul className="space-y-2">
            <li><Link to="/top-deals?category=mobiles" className="hover:text-white">Mobiles & Tablets</Link></li>
            <li><Link to="/top-deals?category=electronics" className="hover:text-white">Laptops & Audio</Link></li>
            <li><Link to="/top-deals?category=fashion" className="hover:text-white">Fashion & Footwear</Link></li>
            <li><Link to="/top-deals?category=appliances" className="hover:text-white">Home Appliances</Link></li>
            <li><Link to="/top-deals?category=quick-commerce" className="text-[#FF6500] font-semibold hover:underline">15-Min Quick Commerce</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="font-bold text-white uppercase tracking-wider text-[11px] mb-3">Customer Care</h4>
          <ul className="space-y-2">
            <li><Link to="/account?tab=orders" className="hover:text-white">Track Your Order</Link></li>
            <li><Link to="/account?tab=coins" className="hover:text-white">VYROX Coins Ledger</Link></li>
            <li><Link to="/compare" className="hover:text-white">Product Comparison</Link></li>
            <li><Link to="/account" className="hover:text-white">Help Center</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="font-bold text-white uppercase tracking-wider text-[11px] mb-3">Portals & Partner</h4>
          <ul className="space-y-2">
            <li><Link to="/seller" className="hover:text-white">Seller Dashboard</Link></li>
            <li><Link to="/delivery" className="hover:text-white">Delivery Partner App</Link></li>
            <li><Link to="/admin" className="hover:text-white">Admin Control Center</Link></li>
            <li><a href="http://localhost:8080/swagger-ui/index.html" target="_blank" rel="noreferrer" className="text-[#00D2FF] hover:underline">Swagger REST API</a></li>
          </ul>
        </div>
      </div>

      {/* Copyright */}
      <div className="border-t border-slate-850 py-4 px-4 bg-slate-950/40 text-center text-[11px] text-slate-400">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>© 2026 VYROX — Built by Team VELTRION. All rights reserved.</div>
          <div className="flex items-center gap-1 text-slate-400">
            <span>Crafted with</span>
            <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500" />
            <span>for intelligent commerce</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
