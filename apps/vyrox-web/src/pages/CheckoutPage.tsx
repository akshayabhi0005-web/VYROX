import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import confetti from 'canvas-confetti';
import { 
  ShieldCheck, MapPin, Truck, CreditCard, Zap, CheckCircle2, 
  ArrowRight, Plus, Check, AlertTriangle, Building, Home, Briefcase
} from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { Address, Order, CartResponse } from '../types';
import { useAuth } from '../context/AuthContext';

const defaultDemoAddresses: Address[] = [
  {
    id: 1,
    name: 'Akshay Kumar',
    mobile: '9876543210',
    street: 'Flat 402, Green Glen Heights, 12th Main Road',
    locality: 'Indiranagar',
    city: 'Bengaluru',
    state: 'Karnataka',
    pincode: '560038',
    landmark: 'Near Indiranagar Metro Station',
    addressType: 'HOME',
    isDefault: true,
  },
  {
    id: 2,
    name: 'Akshay (Office)',
    mobile: '9876543210',
    street: 'Building 4, Tech Park, Outer Ring Road',
    locality: 'Bellandur',
    city: 'Bengaluru',
    state: 'Karnataka',
    pincode: '560103',
    landmark: 'EcoSpace Gate 2',
    addressType: 'WORK',
    isDefault: false,
  }
];

export const CheckoutPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, refreshUserData } = useAuth();

  const couponCode = searchParams.get('coupon') || '';
  const redeemCoinsParam = searchParams.get('coins') === 'true';

  const [addresses, setAddresses] = useState<Address[]>(() => {
    const saved = localStorage.getItem('vyrox_local_addresses');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      } catch (e) {}
    }
    return defaultDemoAddresses;
  });

  const [selectedAddressId, setSelectedAddressId] = useState<number>(1);
  const [isQuickCommerce, setIsQuickCommerce] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<'UPI' | 'CREDIT_DEBIT_CARD' | 'NET_BANKING' | 'COD'>('UPI');
  const [loading, setLoading] = useState(false);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [completedOrder, setCompletedOrder] = useState<Order | null>(null);

  // Cart state
  const [cart, setCart] = useState<CartResponse>(() => {
    const saved = localStorage.getItem('vyrox_local_cart');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {}
    }
    return {
      cartId: 1,
      items: [
        {
          itemId: 1,
          productId: 101,
          productTitle: 'Apple iPhone 15 Pro Max (256 GB) - Natural Titanium',
          productSku: 'VYR-PHN-001',
          categoryName: 'Mobiles',
          brandName: 'Apple',
          mainImageUrl: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=800&q=80',
          mrp: 159900,
          sellingPrice: 148900,
          discountPercentage: 7,
          quantity: 1,
          savedForLater: false,
          estimatedDelivery: 'Tomorrow, by 10 AM',
          inStock: true,
        }
      ],
      savedForLaterItems: [],
      totalItems: 1,
      subtotal: 148900,
      totalSavings: 11000,
      deliveryFee: 0,
      grandTotal: 148900,
      potentialCoinsEarned: 7445,
    };
  });

  // New Address Form Modal
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [newName, setNewName] = useState('Akshay N');
  const [newMobile, setNewMobile] = useState('9876543210');
  const [newStreet, setNewStreet] = useState('45, 100ft Road');
  const [newLocality, setNewLocality] = useState('Indiranagar');
  const [newCity, setNewCity] = useState('Bengaluru');
  const [newState, setNewState] = useState('Karnataka');
  const [newPincode, setNewPincode] = useState('560038');
  const [newType, setNewType] = useState('HOME');

  useEffect(() => {
    fetchAddresses();
  }, []);

  const fetchAddresses = async () => {
    try {
      const addrRes = await apiClient.get('/user/addresses');
      const addrs: Address[] = addrRes.data || [];
      if (Array.isArray(addrs) && addrs.length > 0) {
        setAddresses(addrs);
        localStorage.setItem('vyrox_local_addresses', JSON.stringify(addrs));
        if (addrs[0].id) setSelectedAddressId(addrs[0].id);
      }
    } catch (err) {
      // keep defaultDemoAddresses
    }
  };

  const handleCreateAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    const newAddr: Address = {
      id: Date.now(),
      name: newName,
      mobile: newMobile,
      street: newStreet,
      locality: newLocality,
      city: newCity,
      state: newState,
      pincode: newPincode,
      addressType: newType,
      isDefault: true,
    };

    const updated = [newAddr, ...addresses];
    setAddresses(updated);
    setSelectedAddressId(newAddr.id!);
    localStorage.setItem('vyrox_local_addresses', JSON.stringify(updated));
    setShowAddressModal(false);

    try {
      await apiClient.post('/user/addresses', newAddr);
    } catch (err) {}
  };

  // Calculations
  let couponDiscount = 0;
  if (couponCode.toUpperCase() === 'VYROX100') couponDiscount = 100;
  else if (couponCode.toUpperCase() === 'SMART20') couponDiscount = Math.min(500, Math.round(cart.subtotal * 0.2));
  else if (couponCode.toUpperCase() === 'FESTIVE500') couponDiscount = 500;

  let coinsDiscount = 0;
  const userCoinBal = user?.coinBalance || 350;
  if (redeemCoinsParam) {
    coinsDiscount = Math.min(userCoinBal, Math.round(cart.subtotal * 0.2));
  }

  const deliveryFee = isQuickCommerce ? 0 : cart.deliveryFee;
  const grandTotal = Math.max(0, cart.subtotal - couponDiscount - coinsDiscount + deliveryFee);
  const totalSavings = cart.totalSavings + couponDiscount + coinsDiscount;

  const handlePlaceOrder = async () => {
    setPlacingOrder(true);

    const selectedAddr = addresses.find((a) => a.id === selectedAddressId) || addresses[0];
    const orderNum = `VYR-ORD-2026-${Math.floor(1000 + Math.random() * 9000)}`;
    const otp = `${Math.floor(1000 + Math.random() * 9000)}`;

    const orderPayload: Order = {
      id: Date.now(),
      orderNumber: orderNum,
      status: 'CONFIRMED',
      subtotal: cart.subtotal,
      discountAmount: cart.totalSavings,
      couponDiscount,
      coinsDiscount,
      deliveryFee,
      grandTotal,
      coinsEarned: Math.round(grandTotal * 0.05),
      coinsRedeemed: coinsDiscount,
      couponCodeApplied: couponCode || undefined,
      paymentMethod,
      paymentStatus: 'PAID',
      doorstepOtp: otp,
      quickCommerce: isQuickCommerce,
      estimatedDeliveryTime: isQuickCommerce ? '⚡ In 15 Minutes' : 'Tomorrow, by 11 AM',
      createdAt: new Date().toISOString(),
      shippingAddress: selectedAddr,
      items: cart.items.map((it) => ({
        id: it.itemId,
        productId: it.productId,
        productTitle: it.productTitle,
        productSku: it.productSku,
        mainImageUrl: it.mainImageUrl,
        unitPrice: it.sellingPrice,
        quantity: it.quantity,
        totalPrice: it.sellingPrice * it.quantity,
      })),
    };

    try {
      // Try backend place order
      const res = await apiClient.post('/checkout/place-order', {
        addressId: selectedAddressId,
        couponCode,
        redeemCoins: redeemCoinsParam,
        paymentMethod,
        quickCommerce: isQuickCommerce,
      });

      if (res.data && res.data.orderNumber) {
        setCompletedOrder(res.data);
      } else {
        setCompletedOrder(orderPayload);
      }
    } catch (err) {
      console.warn('Backend order placed locally with demo fallback');
      setCompletedOrder(orderPayload);
    } finally {
      // 1. Save in local orders list
      try {
        const existingOrdersStr = localStorage.getItem('vyrox_local_orders');
        const existingOrders: Order[] = existingOrdersStr ? JSON.parse(existingOrdersStr) : [];
        const updatedOrders = [orderPayload, ...existingOrders];
        localStorage.setItem('vyrox_local_orders', JSON.stringify(updatedOrders));
      } catch (e) {}

      // 2. Clear Cart
      const emptyCart: CartResponse = {
        cartId: 1,
        items: [],
        savedForLaterItems: cart.savedForLaterItems,
        totalItems: 0,
        subtotal: 0,
        totalSavings: 0,
        deliveryFee: 0,
        grandTotal: 0,
        potentialCoinsEarned: 0,
      };
      setCart(emptyCart);
      localStorage.setItem('vyrox_local_cart', JSON.stringify(emptyCart));

      // 3. Trigger celebration confetti
      try {
        confetti({
          particleCount: 120,
          spread: 80,
          origin: { y: 0.6 },
        });
      } catch (e) {}

      refreshUserData();
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
            Share this 4-digit OTP with your VYROX delivery rider only upon receiving the package.
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
            <span className="text-slate-500">Delivery Address</span>
            <span className="font-semibold text-slate-800 text-right line-clamp-1">
              {completedOrder.shippingAddress?.street}, {completedOrder.shippingAddress?.locality}
            </span>
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
                <MapPin className="w-4 h-4 text-[#FF6500]" /> 1. Select Delivery Address
              </h3>
              <button
                onClick={() => setShowAddressModal(true)}
                className="text-xs font-bold text-[#2B6CB0] hover:underline flex items-center gap-1"
              >
                <Plus className="w-3.5 h-3.5" /> Add New Address
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {addresses.map((addr) => (
                <div
                  key={addr.id}
                  onClick={() => setSelectedAddressId(addr.id!)}
                  className={`p-4 rounded-2xl border-2 transition-all cursor-pointer relative space-y-1 ${
                    selectedAddressId === addr.id
                      ? 'border-[#0B192C] bg-slate-50/70 shadow-xs'
                      : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  {selectedAddressId === addr.id && (
                    <div className="absolute top-3 right-3 w-5 h-5 bg-[#0B192C] text-white rounded-full flex items-center justify-center">
                      <Check className="w-3 h-3" />
                    </div>
                  )}
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900">{addr.name}</span>
                    <span className="text-[10px] font-bold uppercase bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded">
                      {addr.addressType || 'HOME'}
                    </span>
                  </div>
                  <div className="text-xs text-slate-600 leading-relaxed">
                    {addr.street}, {addr.locality}, {addr.city}, {addr.state} - {addr.pincode}
                  </div>
                  <div className="text-[11px] text-slate-500 font-medium">Phone: {addr.mobile}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Step 2: Delivery Speed Option */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2 border-b border-slate-100 pb-3">
              <Truck className="w-4 h-4 text-[#FF6500]" /> 2. Delivery Speed
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div
                onClick={() => setIsQuickCommerce(false)}
                className={`p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                  !isQuickCommerce
                    ? 'border-[#0B192C] bg-slate-50/70 shadow-xs'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <div className="text-xs font-bold text-slate-900">📦 Standard Delivery</div>
                  <span className="text-xs font-bold text-emerald-700">FREE</span>
                </div>
                <div className="text-[11px] text-slate-500">Delivered Tomorrow by 11 AM via VYROX Express</div>
              </div>

              <div
                onClick={() => setIsQuickCommerce(true)}
                className={`p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                  isQuickCommerce
                    ? 'border-[#FF6500] bg-orange-50/40 shadow-xs'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <div className="text-xs font-bold text-[#FF6500] flex items-center gap-1">
                    <Zap className="w-3.5 h-3.5 fill-current" /> 15-Minute Instant Darkstore
                  </div>
                  <span className="text-xs font-bold text-emerald-700">FREE</span>
                </div>
                <div className="text-[11px] text-slate-500">Directly dispatched from Indiranagar Hub to doorstep</div>
              </div>
            </div>
          </div>

          {/* Step 3: Payment Method */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2 border-b border-slate-100 pb-3">
              <CreditCard className="w-4 h-4 text-[#FF6500]" /> 3. Select Payment Method
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {[
                { id: 'UPI', label: '⚡ UPI / QR / Google Pay / PhonePe', desc: 'Instant 0% transaction fee' },
                { id: 'CREDIT_DEBIT_CARD', label: '💳 Credit / Debit Card', desc: 'Visa, MasterCard, RuPay' },
                { id: 'NET_BANKING', label: '🏦 Net Banking', desc: 'All major Indian banks supported' },
                { id: 'COD', label: '💵 Cash on Delivery', desc: 'Pay with Cash or UPI at doorstep' },
              ].map((m) => (
                <div
                  key={m.id}
                  onClick={() => setPaymentMethod(m.id as any)}
                  className={`p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                    paymentMethod === m.id
                      ? 'border-[#0B192C] bg-slate-50/70 shadow-xs'
                      : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <div className="text-xs font-bold text-slate-900">{m.label}</div>
                  <div className="text-[11px] text-slate-500 mt-0.5">{m.desc}</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Order Summary Card */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-5">
            <h3 className="font-bold text-base text-slate-900 border-b border-slate-100 pb-3">
              Order Summary ({cart.items.reduce((s, it) => s + it.quantity, 0)} Items)
            </h3>

            {/* Items Summary Preview */}
            <div className="max-h-48 overflow-y-auto space-y-2 pr-1 divide-y divide-slate-100">
              {cart.items.map((it) => (
                <div key={it.itemId} className="pt-2 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2 line-clamp-1 pr-2">
                    <img src={it.mainImageUrl} alt={it.productTitle} className="w-8 h-8 object-contain rounded-md" />
                    <span className="text-slate-800 font-medium truncate">{it.productTitle} (x{it.quantity})</span>
                  </div>
                  <span className="font-bold text-slate-900 flex-shrink-0">₹{(it.sellingPrice * it.quantity).toLocaleString('en-IN')}</span>
                </div>
              ))}
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
              {couponDiscount > 0 && (
                <div className="flex justify-between text-emerald-700">
                  <span>Coupon Discount ({couponCode.toUpperCase()})</span>
                  <span className="font-semibold">-₹{couponDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}
              {coinsDiscount > 0 && (
                <div className="flex justify-between text-amber-700">
                  <span>Coins Redeemed</span>
                  <span className="font-semibold">-₹{coinsDiscount.toLocaleString('en-IN')}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span>Delivery Fee</span>
                <span className="font-semibold text-emerald-700">
                  {deliveryFee === 0 ? 'FREE' : `₹${deliveryFee}`}
                </span>
              </div>

              <div className="border-t border-slate-200 pt-3 flex justify-between items-baseline">
                <span className="text-sm font-bold text-slate-900">Grand Total</span>
                <span className="text-xl font-black text-slate-900">₹{grandTotal.toLocaleString('en-IN')}</span>
              </div>
            </div>

            {/* Place Order CTA Button */}
            <button
              onClick={handlePlaceOrder}
              disabled={placingOrder}
              className="w-full py-4 bg-[#FF6500] hover:bg-[#FF884B] disabled:opacity-75 text-white font-black text-sm rounded-2xl shadow-lg transition-all flex items-center justify-center gap-2"
            >
              <span>{placingOrder ? 'Processing Order...' : `Place Order & Pay ₹${grandTotal.toLocaleString('en-IN')}`}</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <div className="flex items-center justify-center gap-1.5 text-[11px] text-slate-500 font-medium text-center">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>100% Safe & Secure Sandbox Transaction</span>
            </div>
          </div>
        </div>
      </div>

      {/* Add Address Modal */}
      {showAddressModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
            <h3 className="font-bold text-base text-slate-900">Add New Delivery Address</h3>
            <form onSubmit={handleCreateAddress} className="space-y-3">
              <div>
                <label className="text-[11px] font-bold text-slate-700">Full Name</label>
                <input
                  type="text"
                  required
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                />
              </div>
              <div>
                <label className="text-[11px] font-bold text-slate-700">Mobile Number</label>
                <input
                  type="tel"
                  required
                  value={newMobile}
                  onChange={(e) => setNewMobile(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                />
              </div>
              <div>
                <label className="text-[11px] font-bold text-slate-700">Flat / House / Street Address</label>
                <input
                  type="text"
                  required
                  value={newStreet}
                  onChange={(e) => setNewStreet(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-[11px] font-bold text-slate-700">Locality</label>
                  <input
                    type="text"
                    required
                    value={newLocality}
                    onChange={(e) => setNewLocality(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                  />
                </div>
                <div>
                  <label className="text-[11px] font-bold text-slate-700">Pincode</label>
                  <input
                    type="text"
                    required
                    value={newPincode}
                    onChange={(e) => setNewPincode(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                  />
                </div>
              </div>
              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddressModal(false)}
                  className="flex-1 py-2.5 bg-slate-100 text-slate-700 font-bold text-xs rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2.5 bg-[#0B192C] text-white font-bold text-xs rounded-xl"
                >
                  Save & Deliver Here
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
