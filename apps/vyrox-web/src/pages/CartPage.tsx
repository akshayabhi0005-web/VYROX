import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  ShoppingBag, Trash2, Bookmark, ArrowRight, Tag, ShieldCheck, 
  Coins, Sparkles, AlertCircle, Check, Plus, Minus
} from 'lucide-react';
import { CartResponse, CartItem } from '../types';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const CartPage: React.FC = () => {
  const { user } = useAuth();
  const { cart, updateQuantity, removeFromCart, saveForLater } = useCart();
  const navigate = useNavigate();

  const [couponCode, setCouponCode] = useState('');
  const [couponApplied, setCouponApplied] = useState(false);
  const [couponError, setCouponError] = useState('');
  const [redeemCoins, setRedeemCoins] = useState(false);

  const handleUpdateQuantity = (itemId: number, qty: number) => {
    updateQuantity(itemId, qty);
  };

  const handleSaveForLater = (itemId: number, save: boolean) => {
    saveForLater(itemId, save);
  };

  const handleRemove = (itemId: number) => {
    removeFromCart(itemId);
  };

  const handleApplyCoupon = () => {
    const code = couponCode.trim().toUpperCase();
    if (!code) return;
    setCouponError('');

    if (code === 'VYROX100' || code === 'SMART20' || code === 'FESTIVE500') {
      setCouponApplied(true);
    } else {
      setCouponError('Invalid coupon code. Try VYROX100, SMART20, or FESTIVE500.');
      setCouponApplied(false);
    }
  };

  if (!cart || (cart.items.length === 0 && cart.savedForLaterItems.length === 0)) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-16 text-center space-y-4">
        <div className="w-16 h-16 bg-orange-50 text-[#FF6500] rounded-3xl flex items-center justify-center mx-auto shadow-sm">
          <ShoppingBag className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-slate-900">Your cart is currently empty</h2>
        <p className="text-xs sm:text-sm text-slate-500 max-w-sm mx-auto">
          Explore our top deals, trending electronics, groceries, and fashion.
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
  const coinBal = user?.coinBalance || 350;
  if (redeemCoins) {
    coinsDiscount = Math.min(coinBal, Math.round(cart.subtotal * 0.2));
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
                        <span className="text-xs font-bold text-[#FF6500]">
                          {item.discountPercentage}% Off
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Quantity & Actions */}
                <div className="flex sm:flex-col items-center sm:items-end justify-between w-full sm:w-auto gap-3">
                  {/* Quantity Stepper */}
                  <div className="flex items-center border border-slate-200 rounded-xl bg-slate-50 p-1">
                    <button
                      onClick={() => handleUpdateQuantity(item.itemId, item.quantity - 1)}
                      className="w-7 h-7 flex items-center justify-center text-slate-600 hover:bg-white rounded-lg transition-colors font-bold text-sm"
                    >
                      <Minus className="w-3.5 h-3.5" />
                    </button>
                    <span className="w-8 text-center text-xs font-bold text-slate-900">
                      {item.quantity}
                    </span>
                    <button
                      onClick={() => handleUpdateQuantity(item.itemId, item.quantity + 1)}
                      className="w-7 h-7 flex items-center justify-center text-slate-600 hover:bg-white rounded-lg transition-colors font-bold text-sm"
                    >
                      <Plus className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <div className="flex items-center gap-3 text-xs">
                    <button
                      onClick={() => handleSaveForLater(item.itemId, true)}
                      className="text-slate-500 hover:text-[#2B6CB0] font-semibold flex items-center gap-1 transition-colors"
                    >
                      <Bookmark className="w-3.5 h-3.5" /> Save
                    </button>
                    <button
                      onClick={() => handleRemove(item.itemId)}
                      className="text-rose-500 hover:text-rose-700 font-semibold flex items-center gap-1 transition-colors"
                    >
                      <Trash2 className="w-3.5 h-3.5" /> Remove
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Saved For Later Section */}
          {cart.savedForLaterItems.length > 0 && (
            <div className="bg-white rounded-3xl border border-slate-200 p-6 space-y-4">
              <h3 className="font-bold text-sm text-slate-900">Saved For Later ({cart.savedForLaterItems.length})</h3>
              <div className="divide-y divide-slate-100">
                {cart.savedForLaterItems.map((sItem) => (
                  <div key={sItem.itemId} className="py-3 flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <img src={sItem.mainImageUrl} alt={sItem.productTitle} className="w-12 h-12 object-contain bg-slate-50 rounded-xl p-1" />
                      <div>
                        <div className="text-xs font-bold text-slate-900 line-clamp-1">{sItem.productTitle}</div>
                        <div className="text-xs text-[#2B6CB0] font-semibold">₹{sItem.sellingPrice.toLocaleString('en-IN')}</div>
                      </div>
                    </div>
                    <button
                      onClick={() => handleSaveForLater(sItem.itemId, false)}
                      className="px-3 py-1.5 bg-[#0B192C] text-white text-xs font-bold rounded-xl hover:bg-[#1E3E62] transition-colors"
                    >
                      Move to Cart
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Order Summary & Checkout Card */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-5">
            <h3 className="font-bold text-base text-slate-900 border-b border-slate-100 pb-3">
              Order Summary
            </h3>

            {/* Coupons Section */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-700">Apply Coupon Code</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value)}
                  placeholder="e.g. VYROX100"
                  className="flex-1 px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs uppercase font-bold text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#2B6CB0]"
                />
                <button
                  onClick={handleApplyCoupon}
                  className="px-4 py-2 bg-[#0B192C] hover:bg-[#1E3E62] text-white text-xs font-bold rounded-xl transition-all shadow-xs"
                >
                  Apply
                </button>
              </div>
              {couponApplied && (
                <div className="text-[11px] font-bold text-emerald-700 bg-emerald-50 p-2 rounded-lg flex items-center gap-1.5">
                  <Check className="w-3.5 h-3.5" /> Coupon Applied! Saved ₹{couponDiscount}
                </div>
              )}
              {couponError && (
                <div className="text-[11px] font-semibold text-rose-600 bg-rose-50 p-2 rounded-lg">
                  {couponError}
                </div>
              )}
            </div>

            {/* VYROX Coins Toggle */}
            <div className="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200/80 rounded-2xl p-4 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-full bg-amber-400 text-amber-900 font-black text-xs flex items-center justify-center">
                  🪙
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">Redeem VYROX Coins</div>
                  <div className="text-[10px] text-slate-600">
                    Balance: <strong>{coinBal} Coins</strong> (Worth ₹{coinBal})
                  </div>
                </div>
              </div>

              <input
                type="checkbox"
                checked={redeemCoins}
                onChange={(e) => setRedeemCoins(e.target.checked)}
                className="w-5 h-5 text-[#FF6500] rounded focus:ring-orange-500 cursor-pointer accent-[#FF6500]"
              />
            </div>

            {/* Price Calculations */}
            <div className="space-y-2.5 text-xs text-slate-600 border-t border-slate-100 pt-3">
              <div className="flex justify-between">
                <span>Items Subtotal</span>
                <span className="font-semibold text-slate-900">₹{cart.subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-emerald-700">
                <span>Catalog Discounts</span>
                <span className="font-semibold">-₹{cart.totalSavings.toLocaleString('en-IN')}</span>
              </div>
              {couponApplied && (
                <div className="flex justify-between text-emerald-700">
                  <span>Coupon Discount ({couponCode.toUpperCase()})</span>
                  <span className="font-semibold">-₹{couponDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}
              {redeemCoins && (
                <div className="flex justify-between text-amber-700">
                  <span>Coins Redeemed</span>
                  <span className="font-semibold">-₹{coinsDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span>Delivery Fee</span>
                <span className="font-semibold text-emerald-700">
                  {cart.deliveryFee === 0 ? 'FREE' : `₹${cart.deliveryFee}`}
                </span>
              </div>

              <div className="border-t border-slate-200 pt-3 flex justify-between items-baseline">
                <span className="text-sm font-bold text-slate-900">Grand Total</span>
                <span className="text-xl font-black text-slate-900">₹{grandTotal.toLocaleString('en-IN')}</span>
              </div>
            </div>

            {/* Proceed to Checkout CTA */}
            <button
              onClick={() => navigate('/checkout')}
              className="w-full py-3.5 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs sm:text-sm rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
            >
              <span>Proceed to Checkout</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <div className="flex items-center justify-center gap-1.5 text-[11px] text-slate-500 font-medium text-center">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>100% Safe & Secure Checkout Guarantee</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
