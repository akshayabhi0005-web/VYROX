import React, { useEffect, useState } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import confetti from 'canvas-confetti';
import { 
  User, Package, Heart, Tag, Headphones, Coins, MapPin, 
  CreditCard, Shield, LogOut, ChevronRight, Truck, Sparkles, 
  Plus, CheckCircle2, RotateCcw, AlertCircle 
} from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { Order, ProductSummary, Address, CoinWallet, Coupon } from '../types';
import { useAuth } from '../context/AuthContext';
import { ProductCard } from '../components/ProductCard';

export const AccountPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { user, isAuthenticated, logout, refreshUserData } = useAuth();
  const navigate = useNavigate();

  const activeTab = searchParams.get('tab') || 'profile';

  const [orders, setOrders] = useState<Order[]>([]);
  const [wishlist, setWishlist] = useState<ProductSummary[]>([]);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [wallet, setWallet] = useState<CoinWallet | null>(null);
  const [loading, setLoading] = useState(true);
  const [spinning, setSpinning] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login?redirect=/account');
      return;
    }

    const localOrdersStr = localStorage.getItem('vyrox_local_orders');
    const localOrders: Order[] = localOrdersStr ? JSON.parse(localOrdersStr) : [];

    Promise.allSettled([
      apiClient.get('/orders'),
      apiClient.get('/wishlist'),
      apiClient.get('/user/addresses'),
      apiClient.get('/coupons/public'),
      apiClient.get('/coins'),
    ])
      .then(([ordRes, wishRes, addrRes, coupRes, coinRes]) => {
        const serverOrders: Order[] = (ordRes.status === 'fulfilled' && Array.isArray(ordRes.value.data)) ? ordRes.value.data : [];
        const mergedOrders = [...localOrders, ...serverOrders.filter((so) => !localOrders.some((lo) => lo.orderNumber === so.orderNumber))];
        setOrders(mergedOrders);

        if (wishRes.status === 'fulfilled' && wishRes.value.data) {
          setWishlist(wishRes.value.data.items || wishRes.value.data || []);
        }
        if (addrRes.status === 'fulfilled' && Array.isArray(addrRes.value.data)) {
          setAddresses(addrRes.value.data);
        }
        if (coupRes.status === 'fulfilled' && Array.isArray(coupRes.value.data)) {
          setCoupons(coupRes.value.data);
        } else {
          setCoupons([
            { id: 1, code: 'VYROX100', description: 'Flat ₹100 Off on orders above ₹500', discountType: 'FLAT_AMOUNT', discountValue: 100, minOrderAmount: 500 },
            { id: 2, code: 'SMART20', description: '20% Off on orders above ₹1000', discountType: 'PERCENTAGE', discountValue: 20, minOrderAmount: 1000, maxDiscountAmount: 500 },
            { id: 3, code: 'FESTIVE500', description: 'Flat ₹500 Off on orders above ₹2500', discountType: 'FLAT_AMOUNT', discountValue: 500, minOrderAmount: 2500 },
          ]);
        }
        if (coinRes.status === 'fulfilled' && coinRes.value.data) {
          setWallet(coinRes.value.data);
        } else {
          setWallet({
            id: 1,
            balance: user?.coinBalance || 350,
            lifetimeEarned: 500,
            lifetimeSpent: 150,
            history: [
              { id: 1, amount: 100, type: 'EARNED', description: 'Welcome Bonus on Registration', createdAt: new Date().toISOString() },
              { id: 2, amount: 250, type: 'EARNED', description: '5% Cashback on Order', createdAt: new Date().toISOString() },
            ],
          } as any);
        }
      })
      .catch(() => {
        setOrders(localOrders);
      })
      .finally(() => setLoading(false));
  }, [isAuthenticated, activeTab]);

  const handleSpinAndWin = async () => {
    if (spinning) return;
    try {
      setSpinning(true);
      const res = await apiClient.post('/coins/spin-and-win');
      setWallet(res.data);
      refreshUserData();

      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.6 },
      });
      alert('🎉 Congratulations! You won VYROX Coins from the Daily Spin!');
    } catch (err) {
      console.error('Spin error', err);
    } finally {
      setSpinning(false);
    }
  };

  const handleCancelOrder = async (orderId: number) => {
    if (!confirm('Are you sure you want to cancel this order?')) return;
    try {
      const res = await apiClient.post(`/orders/${orderId}/cancel`, { reason: 'Customer requested cancellation' });
      setOrders((prev) => prev.map((o) => (o.id === orderId ? res.data : o)));
    } catch (err) {
      alert('Cannot cancel order in current state.');
    }
  };

  const handleDeleteAddress = async (addressId: number) => {
    try {
      await apiClient.delete(`/user/addresses/${addressId}`);
      setAddresses((prev) => prev.filter((a) => a.id !== addressId));
    } catch (err) {
      console.error('Failed to delete address', err);
    }
  };

  const setTab = (tab: string) => {
    setSearchParams({ tab });
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-8">
      {/* Top User Profile Header (UX reference inspired) */}
      <div className="bg-gradient-to-r from-[#0B192C] via-[#1E3E62] to-[#2B6CB0] text-white p-6 sm:p-8 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-[#FF6500] to-amber-400 text-white font-black text-2xl flex items-center justify-center shadow-lg">
            {user?.fullName?.charAt(0) || 'A'}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-black">{user?.fullName || 'Akshay Abhi'}</h1>
              <span className="bg-[#00D2FF]/20 text-[#00D2FF] text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase">
                VYROX BLACK
              </span>
            </div>
            <p className="text-xs text-slate-300 mt-0.5">{user?.email || user?.mobile || 'customer@vyrox.com'}</p>
          </div>
        </div>

        {/* Coins Badge & Action */}
        <div className="flex items-center gap-3 self-start md:self-auto">
          <div className="bg-white/10 backdrop-blur-md px-4 py-2.5 rounded-2xl border border-white/20 flex items-center gap-2.5">
            <Coins className="w-6 h-6 text-amber-400 fill-amber-400" />
            <div>
              <div className="text-[10px] text-slate-300 font-bold uppercase tracking-wider">Coin Balance</div>
              <div className="text-lg font-black text-amber-300">{wallet?.balance ?? user?.coinBalance ?? 100} Coins</div>
            </div>
          </div>

          <button
            onClick={logout}
            className="p-2.5 bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 rounded-2xl border border-rose-500/30 transition-colors"
            title="Logout"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Navigation Quick Cards (Inspired by Reference UX) */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
        {[
          { id: 'orders', label: 'Orders', icon: Package, count: orders.length, color: 'text-blue-500 bg-blue-50' },
          { id: 'wishlist', label: 'Wishlist', icon: Heart, count: wishlist.length, color: 'text-rose-500 bg-rose-50' },
          { id: 'coupons', label: 'Coupons', icon: Tag, count: coupons.length, color: 'text-purple-500 bg-purple-50' },
          { id: 'coins', label: 'VYROX Coins', icon: Coins, count: wallet?.balance ?? 100, color: 'text-amber-500 bg-amber-50' },
        ].map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setTab(item.id)}
              className={`p-4 sm:p-5 rounded-2xl border text-left transition-all flex items-center justify-between group ${
                isActive
                  ? 'border-[#0B192C] bg-[#0B192C] text-white shadow-md scale-102'
                  : 'bg-white border-slate-200 hover:border-slate-300 hover:shadow-xs text-slate-800'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isActive ? 'bg-white/10 text-white' : item.color}`}>
                  <Icon className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs sm:text-sm font-bold">{item.label}</div>
                  <div className={`text-[11px] ${isActive ? 'text-slate-300' : 'text-slate-500'}`}>{item.count} items</div>
                </div>
              </div>
              <ChevronRight className={`w-4 h-4 ${isActive ? 'text-slate-300' : 'text-slate-400'}`} />
            </button>
          );
        })}
      </div>

      {/* Main Tab Content */}
      <div className="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-xs">
        {/* ORDERS TAB */}
        {activeTab === 'orders' && (
          <div className="space-y-6">
            <h3 className="font-bold text-lg text-slate-900 flex items-center gap-2">
              <Package className="w-5 h-5 text-[#2B6CB0]" /> My Orders ({orders.length})
            </h3>

            {orders.length === 0 ? (
              <div className="text-center py-12 text-slate-500 text-xs">
                No orders placed yet. Start shopping top deals!
              </div>
            ) : (
              <div className="space-y-4">
                {orders.map((ord) => (
                  <div key={ord.id} className="border border-slate-200 rounded-2xl p-5 space-y-4 hover:border-slate-300 transition-all">
                    <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
                      <div>
                        <div className="text-xs text-slate-500 font-medium">Order #{ord.orderNumber}</div>
                        <div className="text-sm font-black text-slate-900 mt-0.5">₹{ord.grandTotal?.toLocaleString('en-IN')}</div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`text-[10px] font-extrabold px-2.5 py-1 rounded-full uppercase ${
                          ord.status === 'DELIVERED' ? 'bg-emerald-100 text-emerald-800' :
                          ord.status === 'OUT_FOR_DELIVERY' ? 'bg-orange-100 text-[#FF6500]' :
                          ord.status === 'CANCELLED' ? 'bg-rose-100 text-rose-800' : 'bg-blue-100 text-[#2B6CB0]'
                        }`}>
                          {ord.status}
                        </span>
                        <Link
                          to={`/orders/${ord.orderNumber}/track`}
                          className="px-3 py-1.5 bg-[#FF6500] hover:bg-[#FF884B] text-white text-xs font-bold rounded-xl flex items-center gap-1 shadow-xs"
                        >
                          <Truck className="w-3.5 h-3.5" /> Live Track
                        </Link>
                      </div>
                    </div>

                    {/* Order Items */}
                    <div className="space-y-2">
                      {ord.items.map((item) => (
                        <div key={item.id} className="flex items-center justify-between text-xs">
                          <div className="flex items-center gap-3">
                            <img src={item.mainImageUrl} alt={item.productTitle} className="w-10 h-10 object-contain bg-slate-50 rounded-lg p-1" />
                            <div>
                              <div className="font-bold text-slate-900 line-clamp-1">{item.productTitle}</div>
                              <div className="text-slate-500 text-[11px]">Qty: {item.quantity} × ₹{item.unitPrice?.toLocaleString('en-IN')}</div>
                            </div>
                          </div>
                          <div className="font-bold text-slate-900">₹{item.totalPrice?.toLocaleString('en-IN')}</div>
                        </div>
                      ))}
                    </div>

                    {/* OTP & Cancel */}
                    <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-100 text-xs">
                      <div className="text-slate-600">
                        Doorstep OTP: <strong className="text-[#0B192C] font-mono font-bold">{ord.doorstepOtp || '4829'}</strong>
                      </div>
                      {ord.status !== 'DELIVERED' && ord.status !== 'CANCELLED' && ord.status !== 'OUT_FOR_DELIVERY' && (
                        <button
                          onClick={() => handleCancelOrder(ord.id)}
                          className="text-xs font-bold text-rose-600 hover:underline"
                        >
                          Cancel Order
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* WISHLIST TAB */}
        {activeTab === 'wishlist' && (
          <div className="space-y-6">
            <h3 className="font-bold text-lg text-slate-900 flex items-center gap-2">
              <Heart className="w-5 h-5 text-rose-500" /> My Wishlist ({wishlist.length})
            </h3>
            {wishlist.length === 0 ? (
              <div className="text-center py-12 text-slate-500 text-xs">
                Your wishlist is empty. Save products you love!
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                {wishlist.map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>
            )}
          </div>
        )}

        {/* VYROX COINS TAB */}
        {activeTab === 'coins' && (
          <div className="space-y-6">
            <div className="bg-gradient-to-r from-amber-500/20 to-orange-500/20 rounded-3xl p-6 border border-amber-300 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="text-xs font-bold text-amber-800 uppercase tracking-wider">Available Balance</div>
                <div className="text-3xl font-black text-amber-900 mt-1">{wallet?.balance ?? 100} VYROX Coins</div>
                <p className="text-xs text-amber-800 mt-1">1 VYROX Coin = ₹1. Redeemable at checkout for instant discounts.</p>
              </div>

              <button
                onClick={handleSpinAndWin}
                disabled={spinning}
                className="px-6 py-3 bg-[#FF6500] hover:bg-[#FF884B] disabled:opacity-50 text-white font-black text-xs sm:text-sm rounded-2xl shadow-lg flex items-center gap-2 transition-all self-start sm:self-auto"
              >
                <Sparkles className={`w-4 h-4 ${spinning ? 'animate-spin' : ''}`} />
                <span>{spinning ? 'Spinning Wheel...' : 'Spin & Win Coins'}</span>
              </button>
            </div>

            {/* Coin Transaction Ledger */}
            <div className="space-y-3">
              <h4 className="font-bold text-sm text-slate-900">Coin Transaction History</h4>
              <div className="divide-y divide-slate-100 border border-slate-200 rounded-2xl overflow-hidden bg-white">
                {wallet?.recentTransactions && wallet.recentTransactions.length > 0 ? (
                  wallet.recentTransactions.map((tx) => (
                    <div key={tx.id} className="p-3.5 flex items-center justify-between text-xs">
                      <div>
                        <div className="font-bold text-slate-900">{tx.description}</div>
                        <div className="text-[11px] text-slate-400">{new Date(tx.timestamp).toLocaleString()}</div>
                      </div>
                      <span className={`font-black text-sm ${tx.amount > 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                        {tx.amount > 0 ? `+${tx.amount}` : tx.amount} Coins
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="p-4 text-xs text-slate-500 text-center">No transactions recorded yet.</div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* COUPONS TAB */}
        {activeTab === 'coupons' && (
          <div className="space-y-4">
            <h3 className="font-bold text-lg text-slate-900 flex items-center gap-2">
              <Tag className="w-5 h-5 text-purple-600" /> Active Coupons & Vouchers
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {coupons.map((c) => (
                <div key={c.id} className="p-5 border-2 border-dashed border-purple-200 rounded-2xl bg-purple-50/50 flex justify-between items-center">
                  <div>
                    <span className="font-black text-sm text-purple-900 font-mono tracking-wider bg-purple-200/70 px-2 py-0.5 rounded">
                      {c.code}
                    </span>
                    <p className="text-xs text-slate-700 font-medium mt-2">{c.description}</p>
                  </div>
                  <button
                    onClick={() => {
                      navigator.clipboard.writeText(c.code);
                      alert(`Copied coupon ${c.code} to clipboard!`);
                    }}
                    className="px-3 py-1.5 bg-purple-700 hover:bg-purple-800 text-white text-xs font-bold rounded-xl"
                  >
                    Copy
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* PROFILE & ADDRESSES TAB */}
        {activeTab === 'profile' && (
          <div className="space-y-6">
            <h3 className="font-bold text-lg text-slate-900 flex items-center gap-2">
              <MapPin className="w-5 h-5 text-[#FF6500]" /> Saved Delivery Addresses
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {addresses.map((a) => (
                <div key={a.id} className="p-4 border border-slate-200 rounded-2xl bg-slate-50/60 relative group">
                  <div className="flex justify-between items-start mb-1">
                    <span className="text-xs font-bold text-slate-900">{a.name}</span>
                    <span className="text-[10px] font-bold bg-white text-slate-600 px-2 py-0.5 rounded border border-slate-200 uppercase">
                      {a.addressType || 'HOME'}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600">
                    {a.street}, {a.locality}, {a.city} - {a.pincode}
                  </p>
                  <p className="text-[11px] text-slate-500 mt-1">Mobile: {a.mobile}</p>

                  <button
                    onClick={() => a.id && handleDeleteAddress(a.id)}
                    className="mt-3 text-xs font-bold text-rose-600 hover:underline"
                  >
                    Delete Address
                  </button>
                </div>
              ))}
            </div>

            {/* Reference UX: Finance Options section */}
            <div className="border-t border-slate-100 pt-6 space-y-3">
              <h4 className="font-bold text-sm text-slate-900">VYROX Smart Finance Options</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="p-4 border border-slate-200 rounded-2xl flex items-center justify-between">
                  <div>
                    <div className="text-xs font-bold text-slate-900">VYROX Personal Line of Credit</div>
                    <div className="text-[11px] text-slate-500">Up to ₹10,00,000 • Zero paperwork</div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-slate-400" />
                </div>
                <div className="p-4 border border-slate-200 rounded-2xl flex items-center justify-between">
                  <div>
                    <div className="text-xs font-bold text-slate-900">VYROX Pay Later & No Cost EMI</div>
                    <div className="text-[11px] text-slate-500">Instant ₹1,00,000 credit limit</div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-slate-400" />
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
