import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import confetti from 'canvas-confetti';
import { 
  ShieldCheck, MapPin, Truck, CreditCard, Zap, CheckCircle2, 
  ArrowRight, Plus, Check, AlertTriangle 
} from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { Address, Order } from '../types';
import { useAuth } from '../context/AuthContext';

export const CheckoutPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, refreshUserData } = useAuth();

  const couponCode = searchParams.get('coupon') || '';
  const redeemCoinsParam = searchParams.get('coins') === 'true';

  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [isQuickCommerce, setIsQuickCommerce] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<'UPI' | 'CREDIT_DEBIT_CARD' | 'NET_BANKING' | 'COD'>('UPI');
  const [summary, setSummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [completedOrder, setCompletedOrder] = useState<Order | null>(null);

  // New Address Form Modal
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [newName, setNewName] = useState('');
  const [newMobile, setNewMobile] = useState('');
  const [newStreet, setNewStreet] = useState('');
  const [newLocality, setNewLocality] = useState('');
  const [newCity, setNewCity] = useState('Bengaluru');
  const [newState, setNewState] = useState('Karnataka');
  const [newPincode, setNewPincode] = useState('560038');

  useEffect(() => {
    fetchAddressesAndSummary();
  }, [selectedAddressId, isQuickCommerce]);

  const fetchAddressesAndSummary = async () => {
    try {
      const addrRes = await apiClient.get('/user/addresses');
      const addrs: Address[] = addrRes.data || [];
      setAddresses(addrs);

      const activeAddrId = selectedAddressId || (addrs.length > 0 ? addrs[0].id : null);
      if (activeAddrId && !selectedAddressId) {
        setSelectedAddressId(activeAddrId);
      }

      const sumRes = await apiClient.post('/checkout/calculate', {
        addressId: activeAddrId,
        couponCode,
        redeemCoins: redeemCoinsParam,
        quickCommerce: isQuickCommerce,
      });
      setSummary(sumRes.data);
    } catch (err) {
      console.error('Failed to calculate checkout summary', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await apiClient.post('/user/addresses', {
        name: newName,
        mobile: newMobile,
        street: newStreet,
        locality: newLocality,
        city: newCity,
        state: newState,
        pincode: newPincode,
        isDefault: true,
      });
      setAddresses((prev) => [res.data, ...prev]);
      setSelectedAddressId(res.data.id);
      setShowAddressModal(false);
    } catch (err) {
      console.error('Failed to save address', err);
    }
  };

  const handlePlaceOrder = async () => {
    try {
      setPlacingOrder(true);
      const res = await apiClient.post('/checkout/place-order', {
        addressId: selectedAddressId,
        couponCode,
        redeemCoins: redeemCoinsParam,
        paymentMethod,
        quickCommerce: isQuickCommerce,
      });

      setCompletedOrder(res.data);
      refreshUserData();

      // Trigger celebration confetti
      confetti({
        particleCount: 120,
        spread: 80,
        origin: { y: 0.6 },
      });
    } catch (err) {
      console.error('Failed to place order', err);
      alert('Order placement failed. Please ensure cart is not empty and address is selected.');
    } finally {
      setPlacingOrder(false);
    }
  };

  if (completedOrder) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center space-y-6 animate-in zoom-in-95">
        <div className="w-20 h-20 bg-emerald-100 text-emerald-600 rounded-3xl flex items-center justify-center mx-auto shadow-md">
          <CheckCircle2 className="w-10 h-10" />
        </div>

        <div>
          <span className="bg-emerald-600 text-white text-[10px] font-extrabold px-3 py-1 rounded-full uppercase tracking-wider">
            ORDER CONFIRMED
          </span>
          <h1 className="text-3xl font-black text-slate-900 mt-3">Thank You for Your Order!</h1>
          <p className="text-xs text-slate-500 mt-1">
            Order ID: <strong className="text-slate-900 font-mono text-sm">{completedOrder.orderNumber}</strong>
          </p>
        </div>

        {/* Doorstep OTP Card */}
        <div className="bg-slate-900 text-white rounded-3xl p-6 shadow-xl max-w-md mx-auto space-y-2 border border-slate-700">
          <div className="text-xs text-slate-400 font-medium">Doorstep Delivery Confirmation OTP:</div>
          <div className="text-4xl font-black tracking-widest text-[#00D2FF] font-mono">
            {completedOrder.doorstepOtp || '4829'}
          </div>
          <p className="text-[11px] text-slate-300">
            Share this 4-digit OTP with your VYROX rider only upon receiving the package.
          </p>
        </div>

        {/* Order Info */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 max-w-md mx-auto text-left space-y-3 shadow-xs">
          <div className="flex justify-between text-xs">
            <span className="text-slate-500">Amount Paid</span>
            <span className="font-bold text-slate-900">₹{completedOrder.grandTotal?.toLocaleString('en-IN')}</span>
          </div>
          <div className="flex justify-between text-xs">
            <span className="text-slate-500">Payment Method</span>
            <span className="font-bold text-slate-900">{completedOrder.paymentMethod} (Sandbox Verified)</span>
          </div>
          <div className="flex justify-between text-xs">
            <span className="text-slate-500">Coins Earned</span>
            <span className="font-bold text-amber-600">+{completedOrder.coinsEarned} VYROX Coins</span>
          </div>
          <div className="flex justify-between text-xs">
            <span className="text-slate-500">Delivery ETA</span>
            <span className="font-bold text-emerald-700">{completedOrder.estimatedDeliveryTime || 'Tomorrow 11 AM'}</span>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row justify-center gap-3 max-w-md mx-auto">
          <Link
            to={`/orders/${completedOrder.orderNumber}/track`}
            className="flex-1 py-3.5 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs rounded-xl shadow-md transition-all flex items-center justify-center gap-2"
          >
            <Truck className="w-4 h-4" />
            <span>Track Live on Map</span>
          </Link>
          <Link
            to="/account?tab=orders"
            className="flex-1 py-3.5 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs rounded-xl transition-all flex items-center justify-center gap-2"
          >
            <span>View All Orders</span>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-8">
      <h1 className="text-2xl font-black text-slate-900">Checkout</h1>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Column: Multi-step Options */}
        <div className="lg:col-span-8 space-y-6">
          {/* Step 1: Delivery Address */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                <MapPin className="w-4 h-4 text-[#FF6500]" /> 1. Delivery Address
              </h3>
              <button
                onClick={() => setShowAddressModal(true)}
                className="text-xs font-bold text-[#2B6CB0] hover:underline flex items-center gap-1"
              >
                <Plus className="w-3.5 h-3.5" /> Add New Address
              </button>
            </div>

            {addresses.length === 0 ? (
              <div className="text-center py-6">
                <p className="text-xs text-slate-500 mb-3">No delivery address saved yet.</p>
                <button
                  onClick={() => setShowAddressModal(true)}
                  className="px-4 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl"
                >
                  Add Delivery Address
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {addresses.map((addr) => {
                  const isSelected = selectedAddressId === addr.id;
                  return (
                    <div
                      key={addr.id}
                      onClick={() => setSelectedAddressId(addr.id || null)}
                      className={`p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                        isSelected
                          ? 'border-[#2B6CB0] bg-blue-50/40 shadow-xs'
                          : 'border-slate-200 hover:border-slate-300 bg-white'
                      }`}
                    >
                      <div className="flex justify-between items-start mb-1">
                        <span className="text-xs font-bold text-slate-900">{addr.name}</span>
                        <span className="text-[10px] font-bold bg-slate-100 text-slate-600 px-2 py-0.5 rounded uppercase">
                          {addr.addressType || 'HOME'}
                        </span>
                      </div>
                      <p className="text-xs text-slate-600 line-clamp-2">
                        {addr.street}, {addr.locality}, {addr.city} - {addr.pincode}
                      </p>
                      <p className="text-[11px] text-slate-500 font-medium mt-1">Phone: {addr.mobile}</p>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Step 2: Delivery Speed Option */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2 border-b border-slate-100 pb-3">
              <Truck className="w-4 h-4 text-[#2B6CB0]" /> 2. Delivery Speed & Slot
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div
                onClick={() => setIsQuickCommerce(false)}
                className={`p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                  !isQuickCommerce
                    ? 'border-[#2B6CB0] bg-blue-50/40'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="font-bold text-xs text-slate-900">Standard Delivery</div>
                <p className="text-xs text-slate-500 mt-0.5">Delivering by Tomorrow 11:00 AM</p>
                <span className="text-[11px] font-bold text-emerald-600 mt-2 block">FREE Delivery</span>
              </div>

              <div
                onClick={() => setIsQuickCommerce(true)}
                className={`p-4 rounded-2xl border-2 cursor-pointer transition-all relative overflow-hidden ${
                  isQuickCommerce
                    ? 'border-[#FF6500] bg-orange-50/40 shadow-xs'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-center gap-1 font-bold text-xs text-[#FF6500]">
                  <Zap className="w-3.5 h-3.5 fill-current" /> 15-Minute Instant Delivery
                </div>
                <p className="text-xs text-slate-500 mt-0.5">Dispatched from Indiranagar Darkstore #101</p>
                <span className="text-[11px] font-bold text-slate-700 mt-2 block">₹15 Express Fee</span>
              </div>
            </div>
          </div>

          {/* Step 3: Payment Method */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                <CreditCard className="w-4 h-4 text-emerald-600" /> 3. Payment Method
              </h3>
              <span className="text-[10px] font-bold bg-amber-100 text-amber-800 px-2 py-0.5 rounded-full flex items-center gap-1">
                <ShieldCheck className="w-3 h-3" /> Sandbox Test Mode
              </span>
            </div>

            <div className="space-y-2.5">
              {[
                { id: 'UPI', label: 'UPI (Google Pay, PhonePe, Paytm, BHIM)', desc: 'Instant UPI QR & App checkout' },
                { id: 'CREDIT_DEBIT_CARD', label: 'Credit / Debit Cards (Visa, Mastercard, RuPay)', desc: 'Encrypted tokenized transactions' },
                { id: 'NET_BANKING', label: 'Net Banking (All Indian Major Banks)', desc: 'HDFC, ICICI, SBI, Axis & 50+ Banks' },
                { id: 'COD', label: 'Cash on Delivery (COD)', desc: 'Pay at your doorstep with Cash or UPI QR' },
              ].map((pm) => (
                <label
                  key={pm.id}
                  className={`flex items-start gap-3 p-3.5 rounded-2xl border-2 cursor-pointer transition-all ${
                    paymentMethod === pm.id
                      ? 'border-[#0B192C] bg-slate-50'
                      : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={pm.id}
                    checked={paymentMethod === pm.id}
                    onChange={() => setPaymentMethod(pm.id as any)}
                    className="mt-1 text-[#0B192C] focus:ring-0"
                  />
                  <div>
                    <div className="text-xs font-bold text-slate-900">{pm.label}</div>
                    <div className="text-[11px] text-slate-500">{pm.desc}</div>
                  </div>
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Checkout Summary Box */}
        <div className="lg:col-span-4 sticky top-24 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500 border-b border-slate-100 pb-3">
              Order Summary
            </h4>

            {summary && (
              <div className="space-y-2.5 text-xs">
                <div className="flex justify-between text-slate-600">
                  <span>Items Subtotal</span>
                  <span className="font-semibold text-slate-900">₹{summary.subtotal?.toLocaleString('en-IN')}</span>
                </div>

                {summary.couponDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>Coupon Discount</span>
                    <span>-₹{summary.couponDiscount?.toLocaleString('en-IN')}</span>
                  </div>
                )}

                {summary.coinsDiscount > 0 && (
                  <div className="flex justify-between text-amber-600 font-semibold">
                    <span>Coins Redeemed</span>
                    <span>-₹{summary.coinsDiscount?.toLocaleString('en-IN')}</span>
                  </div>
                )}

                <div className="flex justify-between text-slate-600">
                  <span>Delivery Fee</span>
                  <span className="font-semibold text-slate-900">
                    {summary.deliveryFee === 0 ? <span className="text-emerald-600">Free</span> : `₹${summary.deliveryFee}`}
                  </span>
                </div>

                <div className="border-t border-slate-100 pt-3 flex justify-between items-baseline text-sm font-black text-slate-900">
                  <span>Total Amount</span>
                  <span className="text-xl font-black text-[#0B192C]">
                    ₹{summary.grandTotal?.toLocaleString('en-IN')}
                  </span>
                </div>
              </div>
            )}

            <button
              onClick={handlePlaceOrder}
              disabled={placingOrder || !selectedAddressId}
              className="w-full py-4 bg-[#0B192C] hover:bg-[#1E3E62] disabled:opacity-50 text-white font-black text-sm rounded-2xl shadow-xl transition-all flex items-center justify-center gap-2"
            >
              {placingOrder ? (
                <span>Placing Order...</span>
              ) : (
                <>
                  <span>Place Order & Pay</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Add Address Modal */}
      {showAddressModal && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
            <h3 className="font-bold text-base text-slate-900">Add New Delivery Address</h3>
            <form onSubmit={handleCreateAddress} className="space-y-3">
              <input
                type="text"
                required
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                placeholder="Full Name"
                className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <input
                type="tel"
                required
                value={newMobile}
                onChange={(e) => setNewMobile(e.target.value)}
                placeholder="10-Digit Mobile Number"
                className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <input
                type="text"
                required
                value={newStreet}
                onChange={(e) => setNewStreet(e.target.value)}
                placeholder="Flat / House / Building"
                className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <input
                type="text"
                required
                value={newLocality}
                onChange={(e) => setNewLocality(e.target.value)}
                placeholder="Locality / Area"
                className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="text"
                  required
                  value={newCity}
                  onChange={(e) => setNewCity(e.target.value)}
                  placeholder="City"
                  className="px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
                <input
                  type="text"
                  required
                  value={newPincode}
                  onChange={(e) => setNewPincode(e.target.value)}
                  placeholder="Pincode"
                  className="px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddressModal(false)}
                  className="px-4 py-2 bg-slate-100 text-slate-700 text-xs font-bold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl"
                >
                  Save Address
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
