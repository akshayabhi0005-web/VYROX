export interface User {
  id: number;
  fullName: string;
  email?: string;
  mobile?: string;
  profilePictureUrl?: string;
  roles: string[];
  coinBalance: number;
}

export interface ProductSummary {
  id: number;
  title: string;
  sku: string;
  categoryName?: string;
  categoryId?: number;
  brandName?: string;
  mrp: number;
  sellingPrice: number;
  discountPercentage: number;
  averageRating: number;
  reviewCount: number;
  mainImageUrl: string;
  inStock: boolean;
  isTopDeal: boolean;
  isTrending: boolean;
  isBestSeller: boolean;
  isQuickCommerceEligible: boolean;
  estimatedDeliveryDays: string;
  freeDelivery: boolean;
  bankOffers?: string[];
}

export interface SpecItem {
  group: string;
  name: string;
  value: string;
}

export interface ProductDetail extends ProductSummary {
  description: string;
  stockQuantity: number;
  images: string[];
  highlights: string[];
  specifications: SpecItem[];
  sellerName?: string;
  sellerRating?: number;
  warrantyInfo?: string;
}

export interface CategorySummary {
  id: number;
  name: string;
  slug: string;
  iconUrl?: string;
  bannerUrl?: string;
  description?: string;
  subCategoryCount: number;
}

export interface CartItem {
  itemId: number;
  productId: number;
  productTitle: string;
  productSku: string;
  categoryName?: string;
  brandName?: string;
  mainImageUrl: string;
  mrp: number;
  sellingPrice: number;
  discountPercentage: number;
  quantity: number;
  savedForLater: boolean;
  estimatedDelivery: string;
  inStock: boolean;
}

export interface CartResponse {
  cartId: number;
  items: CartItem[];
  savedForLaterItems: CartItem[];
  totalItems: number;
  subtotal: number;
  totalSavings: number;
  deliveryFee: number;
  grandTotal: number;
  potentialCoinsEarned: number;
}

export interface Address {
  id?: number;
  name: string;
  mobile: string;
  street: string;
  locality: string;
  city: string;
  state: string;
  pincode: string;
  landmark?: string;
  addressType?: string;
  isDefault?: boolean;
  latitude?: number;
  longitude?: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productTitle: string;
  productSku: string;
  mainImageUrl: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: 'PLACED' | 'CONFIRMED' | 'PACKED' | 'SHIPPED' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED' | 'RETURN_REQUESTED' | 'RETURNED';
  subtotal: number;
  discountAmount: number;
  couponDiscount: number;
  coinsDiscount: number;
  deliveryFee: number;
  grandTotal: number;
  coinsEarned: number;
  coinsRedeemed: number;
  couponCodeApplied?: string;
  paymentMethod: string;
  paymentStatus: string;
  doorstepOtp?: string;
  quickCommerce: boolean;
  estimatedDeliveryTime?: string;
  deliveredAt?: string;
  createdAt: string;
  shippingAddress?: Address;
  items: OrderItem[];
}

export interface LiveTracking {
  orderNumber: string;
  status: string;
  estimatedDeliveryTime: string;
  doorstepOtp: string;
  customerLat: number;
  customerLng: number;
  darkstoreLat: number;
  darkstoreLng: number;
  darkstoreName: string;
  driverLat: number;
  driverLng: number;
  driverName: string;
  driverPhone: string;
  driverVehicle: string;
  currentStatusDescription: string;
  distanceKm: number;
  etaMinutes: number;
  isSimulatedGps: boolean;
  logs: {
    status: string;
    description: string;
    locationName: string;
    timestamp: string;
  }[];
}

export interface Coupon {
  id: number;
  code: string;
  description: string;
  discountType: 'PERCENTAGE' | 'FLAT_AMOUNT';
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  validUntil?: string;
}

export interface CoinWallet {
  balance: number;
  lifetimeEarned: number;
  lifetimeSpent: number;
  recentTransactions: {
    id: number;
    type: string;
    amount: number;
    description: string;
    referenceId?: string;
    timestamp: string;
  }[];
}

export interface ProductReview {
  id: number;
  reviewerName: string;
  rating: number;
  title: string;
  comment: string;
  verifiedPurchase: boolean;
  helpfulCount: number;
  createdAt: string;
}
