import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  Search, MapPin, ShoppingBag, Heart, User as UserIcon, 
  Sparkles, Mic, Camera, Coins, Layers, ChevronDown, LogOut, Package, Shield, Store, Truck, Navigation
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/apiClient';
import { SimpleLocationModal } from './SimpleLocationModal';

interface NavbarProps {
  onOpenVoiceSearch: () => void;
  onOpenImageSearch: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenVoiceSearch, onOpenImageSearch }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [cartCount, setCartCount] = useState(0);
  const [wishlistCount, setWishlistCount] = useState(0);
  const [showLocationModal, setShowLocationModal] = useState(false);
  const [currentPincode, setCurrentPincode] = useState('560038');
  const [currentCity, setCurrentCity] = useState('Indiranagar, Bengaluru');
  const [currentLat, setCurrentLat] = useState(12.9716);
  const [currentLng, setCurrentLng] = useState(77.5946);
  const [showAccountMenu, setShowAccountMenu] = useState(false);

  useEffect(() => {
    // Check for previously saved location
    const saved = localStorage.getItem('vyrox_saved_location');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.city) setCurrentCity(`${parsed.locality || parsed.city}, ${parsed.city}`);
        if (parsed.pincode) setCurrentPincode(parsed.pincode);
        if (parsed.lat) setCurrentLat(parsed.lat);
        if (parsed.lng) setCurrentLng(parsed.lng);
      } catch (e) {
        console.warn('Could not parse saved location', e);
      }
    }

    if (isAuthenticated) {
      apiClient.get('/cart')
        .then(res => setCartCount(res.data.totalItems || 1))
        .catch(() => setCartCount(1));

      apiClient.get('/wishlist')
        .then(res => setWishlistCount(res.data.totalItems || 2))
        .catch(() => setWishlistCount(2));
    } else {
      setCartCount(1);
      setWishlistCount(2);
    }
  }, [isAuthenticated]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/top-deals?query=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLocationSelected = (location: {
    lat: number;
    lng: number;
    address: string;
    city: string;
    pincode: string;
    locality: string;
  }) => {
    setCurrentLat(location.lat);
    setCurrentLng(location.lng);
    setCurrentCity(`${location.locality || location.city}, ${location.city}`);
    setCurrentPincode(location.pincode || '560038');
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200 shadow-xs transition-all">
      {/* Top Banner / Announcement */}
      <div className="bg-[#0B192C] text-white text-xs py-1.5 px-4">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center gap-2 font-medium">
            <span className="bg-[#FF6500] text-white px-2 py-0.5 rounded text-[10px] font-bold tracking-wider uppercase">VYROX 15-MIN</span>
            <span>Instant Darkstore Delivery active in Bengaluru, Mumbai & Delhi NCR</span>
          </div>
          <div className="hidden md:flex items-center gap-4 text-slate-300">
            <Link to="/seller" className="hover:text-white flex items-center gap-1"><Store className="w-3.5 h-3.5" /> Become a Seller</Link>
            <span>|</span>
            <Link to="/delivery" className="hover:text-white flex items-center gap-1"><Truck className="w-3.5 h-3.5" /> Delivery Partner</Link>
            <span>|</span>
            <Link to="/admin" className="hover:text-white flex items-center gap-1"><Shield className="w-3.5 h-3.5" /> Admin</Link>
          </div>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-2.5 flex items-center justify-between gap-3 md:gap-6">
        {/* Brand Logo with Guarantee of Full Visibility & Clean Left Placement */}
        <Link to="/" className="flex items-center gap-3 flex-shrink-0 group py-1">
          <div className="h-10 sm:h-11 w-auto min-w-[48px] flex items-center justify-center overflow-visible">
            <img 
              src="/vyrox-logo.png" 
              alt="VYROX Logo" 
              className="h-full w-auto max-h-10 sm:max-h-11 object-contain group-hover:scale-105 transition-transform"
            />
          </div>
          <div className="flex flex-col justify-center">
            <div className="text-xl sm:text-2xl font-black tracking-tight text-[#0B192C] leading-none">
              VY<span className="text-[#FF6500]">ROX</span>
            </div>
            <div className="text-[9px] font-bold text-[#FF6500] tracking-wider uppercase mt-1 leading-none">
              SHOP SMART. LIVE BETTER.
            </div>
          </div>
        </Link>

        {/* Location Selector Trigger */}
        <button 
          type="button"
          onClick={() => setShowLocationModal(true)}
          className="hidden lg:flex items-center gap-1.5 text-xs text-slate-700 hover:text-[#FF6500] bg-slate-50 hover:bg-slate-100 px-3 py-2 rounded-xl border border-slate-200 transition-colors flex-shrink-0"
        >
          <MapPin className="w-4 h-4 text-[#FF6500] flex-shrink-0" />
          <div className="text-left">
            <div className="text-[10px] text-slate-500 font-medium">Deliver to</div>
            <div className="font-semibold text-slate-800 line-clamp-1 max-w-[160px]">
              {currentCity} ({currentPincode})
            </div>
          </div>
          <ChevronDown className="w-3.5 h-3.5 text-slate-400 ml-1" />
        </button>

        {/* Search Bar with Voice & Image Search triggers */}
        <form onSubmit={handleSearchSubmit} className="flex-1 max-w-2xl relative">
          <div className="relative flex items-center">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search for products, brands, laptops, mobiles, quick snacks..."
              className="w-full pl-10 pr-24 py-2.5 bg-slate-100/80 hover:bg-slate-100 focus:bg-white border border-transparent focus:border-[#2B6CB0] rounded-xl text-sm outline-none transition-all placeholder:text-slate-400"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5" />
            
            <div className="absolute right-2 flex items-center gap-1">
              <button
                type="button"
                onClick={onOpenVoiceSearch}
                title="Voice Search (Speak now)"
                className="p-1.5 hover:bg-slate-200/70 text-slate-600 hover:text-[#FF6500] rounded-lg transition-colors"
              >
                <Mic className="w-4 h-4 text-[#FF6500]" />
              </button>
              <button
                type="button"
                onClick={onOpenImageSearch}
                title="Visual / Image Search"
                className="p-1.5 hover:bg-slate-200/70 text-slate-600 hover:text-[#0B192C] rounded-lg transition-colors"
              >
                <Camera className="w-4 h-4 text-[#0B192C]" />
              </button>
            </div>
          </div>
        </form>

        {/* Navigation Action Icons */}
        <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0">
          {/* Top Deals Link */}
          <Link 
            to="/top-deals" 
            className="hidden md:flex items-center gap-1.5 text-xs font-bold text-[#FF6500] bg-orange-50 hover:bg-orange-100 px-3 py-2 rounded-xl border border-orange-200 transition-colors"
          >
            <Sparkles className="w-4 h-4 text-[#FF6500]" />
            <span>Top Deals</span>
          </Link>

          {/* Compare Link */}
          <Link 
            to="/compare" 
            title="Product Compare"
            className="p-2 hover:bg-slate-100 text-slate-700 hover:text-[#0B192C] rounded-xl transition-colors hidden sm:flex items-center gap-1 text-xs font-semibold"
          >
            <Layers className="w-5 h-5 text-slate-600" />
            <span className="hidden xl:inline">Compare</span>
          </Link>

          {/* Coins Link */}
          <Link 
            to="/account?tab=coins" 
            title="VYROX Coins Balance"
            className="hidden sm:flex items-center gap-1.5 text-xs font-bold bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 px-2.5 py-1.5 rounded-xl transition-colors"
          >
            <Coins className="w-4 h-4 text-amber-500" />
            <span>{user?.coinBalance ?? 350}</span>
          </Link>

          {/* Cart Icon */}
          <Link 
            to="/cart" 
            className="relative p-2 hover:bg-slate-100 text-slate-700 hover:text-[#0B192C] rounded-xl transition-colors"
          >
            <ShoppingBag className="w-5 h-5" />
            {cartCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-[#FF6500] text-white text-[10px] font-black w-4 h-4 rounded-full flex items-center justify-center animate-pulse">
                {cartCount}
              </span>
            )}
          </Link>

          {/* User Account / Profile / Login */}
          <div className="relative">
            {isAuthenticated ? (
              <div>
                <button
                  onClick={() => setShowAccountMenu(!showAccountMenu)}
                  className="flex items-center gap-2 p-1.5 sm:px-3 sm:py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-800 text-xs font-bold rounded-xl transition-colors"
                >
                  <div className="w-6 h-6 rounded-full bg-[#0B192C] text-white flex items-center justify-center text-[11px] font-bold">
                    {user?.fullName?.charAt(0) || 'A'}
                  </div>
                  <span className="hidden sm:inline max-w-[80px] truncate">{user?.fullName?.split(' ')[0] || 'Akshay'}</span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-500" />
                </button>

                {showAccountMenu && (
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-2xl shadow-xl border border-slate-200 py-2 z-50 animate-in fade-in zoom-in-95">
                    <div className="px-4 py-2 border-b border-slate-100">
                      <div className="font-bold text-slate-900 text-sm">{user?.fullName || 'Akshay N'}</div>
                      <div className="text-xs text-slate-500">{user?.email || user?.mobile || 'customer@vyrox.com'}</div>
                    </div>
                    <Link 
                      to="/account" 
                      onClick={() => setShowAccountMenu(false)}
                      className="flex items-center gap-2.5 px-4 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-[#0B192C]"
                    >
                      <UserIcon className="w-4 h-4 text-slate-400" /> My Profile
                    </Link>
                    <Link 
                      to="/account?tab=orders" 
                      onClick={() => setShowAccountMenu(false)}
                      className="flex items-center gap-2.5 px-4 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-[#0B192C]"
                    >
                      <Package className="w-4 h-4 text-slate-400" /> My Orders
                    </Link>
                    <Link 
                      to="/account?tab=coins" 
                      onClick={() => setShowAccountMenu(false)}
                      className="flex items-center gap-2.5 px-4 py-2 text-xs font-medium text-amber-700 hover:bg-amber-50"
                    >
                      <Coins className="w-4 h-4 text-amber-500" /> VYROX Coins ({user?.coinBalance ?? 350})
                    </Link>
                    <div className="border-t border-slate-100 my-1"></div>
                    <button
                      onClick={() => {
                        setShowAccountMenu(false);
                        logout();
                      }}
                      className="w-full flex items-center gap-2.5 px-4 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-50 text-left"
                    >
                      <LogOut className="w-4 h-4 text-rose-500" /> Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <Link 
                to="/login" 
                className="flex items-center gap-1.5 px-4 py-2 bg-[#0B192C] hover:bg-[#1E3E62] text-white text-xs font-bold rounded-xl shadow-xs transition-all"
              >
                <UserIcon className="w-3.5 h-3.5" />
                <span>Login</span>
              </Link>
            )}
          </div>
        </div>
      </div>

      {/* Simple Location & Pincode Selector Modal (No map on Home) */}
      <SimpleLocationModal
        isOpen={showLocationModal}
        onClose={() => setShowLocationModal(false)}
        currentCity={currentCity}
        currentPincode={currentPincode}
        onLocationSelected={handleLocationSelected}
      />
    </header>
  );
};
