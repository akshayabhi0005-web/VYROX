import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  ShoppingBag, Trash2, Bookmark, ArrowRight, Tag, ShieldCheck, 
  Coins, Sparkles, AlertCircle, Check
} from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { CartResponse } from '../types';
import { useAuth } from '../context/AuthContext';

export const CartPage: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [couponCode, setCouponCode] = useState('');
  const [couponApplied, setCouponApplied] = useState(false);
  const [couponError, setCouponError] = useState('');
  const [redeemCoins, setRedeemCoins] = useState(false);

  const fetchCart = async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    try {
      const res = await apiClient.get('/cart');
      setCart(res.data);
    } catch (err) {
      console.error('Failed to load cart', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [isAuthenticated]);

  const handleUpdateQuantity = async (itemId: number, qty: number) => {
    try {
      const res = await apiClient.put(`/cart/items/${itemId}`, { quantity: qty });
      setCart(res.data);
    } catch (err) {
      console.error('Failed to update quantity', err);
    }
  };

  const handleSaveForLater = async (itemId: number, save: boolean) => {
    try {
      const res = await apiClient.post(`/cart/items/${itemId}/save-for-later?saveForLater=${save}`);
      setCart(res.data);
    } catch (err) {
      console.error('Failed to update save for later', err);
    }
  };

  const handleRemove = async (itemId: number) => {
    try {
      const res = await apiClient.delete(`/cart/items/${itemId}`);
      setCart(res.data);
    } catch (err) {
      console.error('Failed to remove item', err);
    }
  };

  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) return;
    setCouponError('');
    try {
      const res = await apiClient.post(`/coupons/validate?code=${couponCode.trim()}&cartTotal=${cart?.subtotal || 0}`);
      if (res.data.valid) {
        setCouponApplied(true);
      } else {
        setCouponError(res.data.message || 'Invalid coupon');
        setCouponApplied(false);
      }
    } catch (err) {
      setCouponError('Failed to validate coupon');
      setCouponApplied(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-16 text-center space-y-4">
        <div className="w-16 h-16 bg-blue-50 text-[#2B6CB0] rounded-3xl flex items-center justify-center mx-auto shadow-sm">
          <ShoppingBag className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-slate-900">Please Log In to View Your Cart</h2>
        <p className="text-xs sm:text-sm text-slate-500 max-w-sm mx-auto">
          Sign in to access your saved items, apply exclusive coupons, and redeem VYROX coins.
        </p>
        <Link
          to="/login?redirect=/cart"
          className="inline-flex px-6 py-3 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs rounded-xl shadow-md transition-all"
        >
          Login to Continue
        </Link>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 animate-pulse space-y-6">
        <div className="h-8 bg-slate-200 rounded-xl w-48"></div>
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div className="lg:col-span-8 bg-slate-200 rounded-3xl h-96"></div>
          <div className="lg:col-span-4 bg-slate-200 rounded-3xl h-80"></div>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-16 text-center space-y-4">
        <div className="w-16 h-16 bg-orange-50 text-[#FF6500] rounded-3xl flex items-center justify-center mx-auto shadow-sm">
          <ShoppingBag className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-slate-900">Your cart is waiting for something great</h2>
        <p className="text-xs sm:text-sm text-slate-500 max-w-sm mx-auto">
          Explore our top deals, trending gadgets, and 15-minute quick delivery items.
        </p>
        <Link
          to="/top-deals"
          className="inline-flex px-6 py-3 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs rounded-xl shadow-md transition-all"
        >
          Explore Top Deals
        </Link>
      </div>
    );
  }

  // Calculate local breakdown for coupons and coins
  let couponDiscount = 0;
  if (couponApplied) {
    if (couponCode.toUpperCase() === 'VYROX100') couponDiscount = 100;
    else if (couponCode.toUpperCase() === 'SMART20') couponDiscount = Math.min(500, Math.round(cart.subtotal * 0.2));
    else if (couponCode.toUpperCase() === 'FESTIVE500') couponDiscount = 500;
  }

  let coinsDiscount = 0;
  if (redeemCoins && user?.coinBalance) {
    coinsDiscount = Math.min(user.coinBalance, Math.round(cart.subtotal * 0.2));
  }

  const grandTotal = Math.max(0, cart.subtotal - couponDiscount - coinsDiscount + cart.deliveryFee);
  const totalSavings = cart.totalSavings + couponDiscount + coinsDiscount;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Title */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-black text-slate-900 flex items-center gap-2">
          <span>My Cart</span>
          <span className="text-xs font-bold text-slate-500 bg-slate-200 px-2.5 py-0.5 rounded-full">
            {cart.totalItems} Items
          </span>
        </h1>
        <div className="text-xs font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 px-3 py-1.5 rounded-xl">
          🎉 Total Savings: ₹{totalSavings.toLocaleString('en-IN')}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Cart Items List */}
        <div className="lg:col-span-8 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden">
            {cart.items.map((item) => (
              <div key={item.itemId} className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 justify-between items-start">
                <div className="flex gap-4">
                  <Link to={`/product/${item.productId}`}>
                    <img
                      src={item.mainImageUrl}
                      alt={item.productTitle}
                      className="w-20 h-20 sm:w-24 sm:h-24 object-contain bg-slate-50 rounded-2xl p-2 border border-slate-100 flex-shrink-0"
                    />
                  </Link>

                  <div className="space-y-1">
                    <div className="text-[11px] font-bold text-[#2B6CB0] uppercase">{item.brandName || 'VYROX'}</div>
                    <Link
                      to={`/product/${item.productId}`}
                      className="text-xs sm:text-sm font-bold text-slate-900 line-clamp-2 hover:text-[#2B6CB0] transition-colors"
                    >
                      {item.productTitle}
                    </Link>
                    <div className="text-[11px] text-slate-500 font-medium">
                      Delivery: <strong className="text-slate-700">{item.estimatedDelivery || 'Tomorrow'}</strong>
                    </div>

                    {/* Price */}
                    <div className="flex items-baseline gap-2 pt-1">
                      <span className="text-base font-black text-slate-900">
                        ₹{(item.sellingPrice * item.quantity).toLocaleString('en-IN')}
                      </span>
                      {item.mrp > item.sellingPrice && (
                        <span className="text-xs text-slate-400 line-through">
                          ₹{(item.mrp * item.quantity).toLocaleString('en-IN')}
                        </span>
                      )}
                      {item.discountPercentage > 0 && (
                        <span className="text-[10px] font-extrabold text-[#FF6500]">
                          {item.discountPercentage}% OFF
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Item Actions */}
                <div className="flex sm:flex-col items-center sm:items-end justify-between w-full sm:w-auto gap-3 pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-100">
                  {/* Quantity Selector */}
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-500 font-semibold">Qty:</span>
                    <select
                      value={item.quantity}
                      onChange={(e) => handleUpdateQuantity(item.itemId, Number(e.target.value))}
                      className="bg-slate-100 border border-slate-200 rounded-lg px-2 py-1 text-xs font-bold text-slate-800 outline-none"
                    >
                      {[1, 2, 3, 4, 5, 6].map((q) => (
                        <option key={q} value={q}>{q}</option>
                      ))}
                    </select>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleSaveForLater(item.itemId, true)}
                      className="text-xs font-semibold text-slate-600 hover:text-slate-900 flex items-center gap-1 hover:bg-slate-100 px-2 py-1 rounded-lg"
                    >
                      <Bookmark className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">Save for later</span>
                    </button>
                    <button
                      onClick={() => handleRemove(item.itemId)}
                      className="text-xs font-semibold text-rose-600 hover:text-rose-700 flex items-center gap-1 hover:bg-rose-50 px-2 py-1 rounded-lg"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">Remove</span>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Saved For Later Section */}
          {cart.savedForLaterItems && cart.savedForLaterItems.length > 0 && (
            <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
              <h3 className="font-bold text-sm text-slate-900">
                Saved for Later ({cart.savedForLaterItems.length})
              </h3>
              <div className="divide-y divide-slate-100">
                {cart.savedForLaterItems.map((item) => (
                  <div key={item.itemId} className="py-3 flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <img src={item.mainImageUrl} alt={item.productTitle} className="w-12 h-12 object-contain bg-slate-50 rounded-xl p-1" />
                      <div>
                        <div className="text-xs font-bold text-slate-900 line-clamp-1">{item.productTitle}</div>
                        <div className="text-xs font-black text-slate-900">₹{item.sellingPrice?.toLocaleString('en-IN')}</div>
                      </div>
                    </div>
                    <button
                      onClick={() => handleSaveForLater(item.itemId, false)}
                      className="px-3 py-1.5 bg-[#0B192C] text-white text-xs font-bold rounded-xl hover:bg-[#1E3E62]"
                    >
                      Move to Cart
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Order Price Summary */}
        <div className="lg:col-span-4 space-y-4 sticky top-24">
          {/* Coupon Code Box */}
          <div className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
            <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <Tag className="w-4 h-4 text-purple-600" /> Apply Coupon
            </h4>
            <div className="flex gap-2">
              <input
                type="text"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                placeholder="e.g. VYROX100"
                className="flex-1 px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold uppercase outline-none focus:border-purple-600"
              />
              <button
                onClick={handleApplyCoupon}
                className="px-4 py-2 bg-purple-700 hover:bg-purple-800 text-white text-xs font-bold rounded-xl transition-colors"
              >
                Apply
              </button>
            </div>
            {couponApplied && (
              <div className="text-xs font-semibold text-emerald-700 bg-emerald-50 p-2 rounded-lg flex items-center gap-1">
                <Check className="w-3.5 h-3.5" /> Coupon applied! ₹{couponDiscount} discount added.
              </div>
            )}
            {couponError && (
              <div className="text-xs font-semibold text-rose-600 bg-rose-50 p-2 rounded-lg flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5" /> {couponError}
              </div>
            )}
          </div>

          {/* VYROX Coins Toggle */}
          {user && user.coinBalance > 0 && (
            <div className="bg-amber-50/70 border border-amber-200 rounded-3xl p-5 shadow-xs space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Coins className="w-5 h-5 text-amber-500 fill-amber-500" />
                  <div>
                    <div className="text-xs font-bold text-amber-900">Redeem VYROX Coins</div>
                    <div className="text-[11px] text-amber-700">Available: {user.coinBalance} Coins (₹{user.coinBalance})</div>
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={redeemCoins}
                  onChange={(e) => setRedeemCoins(e.target.checked)}
                  className="w-4 h-4 text-amber-600 rounded cursor-pointer"
                />
              </div>
              {redeemCoins && (
                <p className="text-[11px] text-emerald-700 font-semibold pt-1">
                  ✓ Redeeming ₹{coinsDiscount} coins on this order
                </p>
              )}
            </div>
          )}

          {/* Price Breakdown */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500 border-b border-slate-100 pb-3">
              Price Summary
            </h4>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between text-slate-600">
                <span>Subtotal ({cart.totalItems} Items)</span>
                <span className="font-semibold text-slate-900">₹{cart.subtotal.toLocaleString('en-IN')}</span>
              </div>

              {couponDiscount > 0 && (
                <div className="flex justify-between text-emerald-600 font-semibold">
                  <span>Coupon Discount</span>
                  <span>-₹{couponDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}

              {coinsDiscount > 0 && (
                <div className="flex justify-between text-amber-600 font-semibold">
                  <span>Coins Redeemed</span>
                  <span>-₹{coinsDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}

              <div className="flex justify-between text-slate-600">
                <span>Delivery Fee</span>
                <span className="font-semibold text-slate-900">
                  {cart.deliveryFee === 0 ? <span className="text-emerald-600">Free</span> : `₹${cart.deliveryFee}`}
                </span>
              </div>

              <div className="border-t border-slate-100 pt-3 flex justify-between items-baseline text-sm font-black text-slate-900">
                <span>Grand Total</span>
                <span className="text-xl font-black text-[#0B192C]">
                  ₹{grandTotal.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            <button
              onClick={() => {
                navigate(`/checkout?coupon=${couponApplied ? couponCode : ''}&coins=${redeemCoins ? 'true' : 'false'}`);
              }}
              className="w-full py-4 bg-[#FF6500] hover:bg-[#FF884B] text-white font-black text-sm rounded-2xl shadow-lg transition-all flex items-center justify-center gap-2"
            >
              <span>Proceed to Checkout</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <div className="text-[11px] text-slate-400 text-center flex items-center justify-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
              <span>Safe and Secure Payments. 100% Authentic Guarantee.</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
