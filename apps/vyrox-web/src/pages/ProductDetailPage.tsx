import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { 
  Star, ShoppingBag, Zap, Heart, Layers, ShieldCheck, Truck, RotateCcw, 
  Tag, Check, MapPin, MessageSquare, Plus, ChevronRight
} from 'lucide-react';
import { ProductDetail, ProductSummary, ProductReview } from '../types';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import { ProductCard } from '../components/ProductCard';

import { fallbackProducts, fallbackSummaryList } from '../data/fallbackCatalog';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated, setPendingAction } = useAuth();

  const numId = parseInt(id || '1', 10);
  const initialProduct = fallbackProducts.find(p => p.id === numId) || fallbackProducts[0];

  const [product, setProduct] = useState<ProductDetail | null>(initialProduct);
  const [similar, setSimilar] = useState<ProductSummary[]>(fallbackSummaryList.filter(p => p.id !== initialProduct.id).slice(0, 4));
  const [reviews, setReviews] = useState<ProductReview[]>([]);
  const [selectedImage, setSelectedImage] = useState<string>(initialProduct.mainImageUrl);
  const [pincode, setPincode] = useState('560038');
  const [deliveryMessage, setDeliveryMessage] = useState('Delivery by Tomorrow 11 AM | Free');
  const [loading, setLoading] = useState(false);
  const [addingToCart, setAddingToCart] = useState(false);
  const [addedSuccess, setAddedSuccess] = useState(false);

  // Review modal
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewTitle, setReviewTitle] = useState('');
  const [reviewComment, setReviewComment] = useState('');

  useEffect(() => {
    if (!id) return;

    Promise.allSettled([
      apiClient.get(`/products/${id}`),
      apiClient.get(`/products/${id}/similar`),
      apiClient.get(`/reviews/product/${id}`),
    ])
      .then(([prodRes, simRes, revRes]) => {
        if (prodRes.status === 'fulfilled' && prodRes.value.data) {
          setProduct(prodRes.value.data);
          setSelectedImage(prodRes.value.data.mainImageUrl);
        }
        if (simRes.status === 'fulfilled' && simRes.value.data) {
          setSimilar(simRes.value.data);
        }
        if (revRes.status === 'fulfilled' && revRes.value.data) {
          setReviews(revRes.value.data);
        }
      })
      .catch(() => {
        console.warn('Using built-in product details');
      })
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;

    if (!isAuthenticated) {
      setPendingAction({
        type: 'ADD_TO_CART',
        productId: product.id,
        quantity: 1,
      });
      navigate(`/login?redirect=${encodeURIComponent(window.location.pathname)}`);
      return;
    }

    try {
      setAddingToCart(true);
      await apiClient.post('/cart/add', { productId: product.id, quantity: 1 });
      setAddedSuccess(true);
      setTimeout(() => setAddedSuccess(false), 2500);
    } catch (err) {
      console.error('Failed to add to cart', err);
    } finally {
      setAddingToCart(false);
    }
  };

  const handleBuyNow = async () => {
    if (!product) return;

    if (!isAuthenticated) {
      setPendingAction({
        type: 'BUY_NOW',
        productId: product.id,
        quantity: 1,
        redirectUrl: '/checkout',
      });
      navigate(`/login?redirect=${encodeURIComponent('/checkout')}`);
      return;
    }

    try {
      setAddingToCart(true);
      await apiClient.post('/cart/add', { productId: product.id, quantity: 1 });
      navigate('/checkout');
    } catch (err) {
      console.error('Failed to proceed to buy now', err);
    } finally {
      setAddingToCart(false);
    }
  };

  const handlePincodeCheck = () => {
    if (product?.isQuickCommerceEligible && (pincode === '560038' || pincode === '560001')) {
      setDeliveryMessage('⚡ 15-Minute Instant Darkstore Delivery Available!');
    } else {
      setDeliveryMessage('Standard Delivery by Tomorrow, 11 AM | Free');
    }
  };

  const handleAddReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate(`/login?redirect=${encodeURIComponent(window.location.pathname)}`);
      return;
    }

    try {
      const res = await apiClient.post(`/reviews/product/${product?.id}`, {
        rating: reviewRating,
        title: reviewTitle,
        comment: reviewComment,
      });
      setReviews((prev) => [res.data, ...prev]);
      setShowReviewModal(false);
      setReviewTitle('');
      setReviewComment('');
    } catch (err) {
      console.error('Failed to submit review', err);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 animate-pulse space-y-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-slate-200 rounded-3xl aspect-square"></div>
          <div className="space-y-4">
            <div className="h-8 bg-slate-200 rounded-xl w-3/4"></div>
            <div className="h-6 bg-slate-200 rounded-xl w-1/4"></div>
            <div className="h-24 bg-slate-200 rounded-2xl"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center space-y-3">
        <h2 className="text-xl font-bold text-slate-800">Product Not Found</h2>
        <Link to="/" className="text-xs font-bold text-[#2B6CB0] hover:underline">
          Return to VYROX Home
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-10">
      {/* Breadcrumbs */}
      <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium">
        <Link to="/" className="hover:text-slate-900">Home</Link>
        <ChevronRight className="w-3.5 h-3.5" />
        <Link to={`/top-deals?category=${product.categoryName?.toLowerCase()}`} className="hover:text-slate-900">
          {product.categoryName}
        </Link>
        <ChevronRight className="w-3.5 h-3.5" />
        <span className="text-slate-800 font-bold truncate max-w-xs">{product.title}</span>
      </div>

      {/* Main Product Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Gallery */}
        <div className="lg:col-span-5 space-y-4 sticky top-24">
          <div className="bg-white rounded-3xl border border-slate-200 p-6 aspect-square flex items-center justify-center relative overflow-hidden shadow-xs">
            {product.discountPercentage > 0 && (
              <span className="absolute top-4 left-4 bg-[#FF6500] text-white text-xs font-black px-2.5 py-1 rounded-lg shadow-sm">
                {product.discountPercentage}% OFF
              </span>
            )}
            <img
              src={selectedImage || product.mainImageUrl}
              alt={product.title}
              className="w-full h-full object-contain mix-blend-multiply"
            />
          </div>

          {/* Thumbnails */}
          {product.images && product.images.length > 1 && (
            <div className="flex gap-3 overflow-x-auto pb-2">
              {product.images.map((img, idx) => (
                <button
                  key={idx}
                  onClick={() => setSelectedImage(img)}
                  className={`w-16 h-16 rounded-xl border-2 p-1.5 bg-white flex-shrink-0 transition-all ${
                    selectedImage === img ? 'border-[#2B6CB0] shadow-md scale-105' : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <img src={img} alt="Thumbnail" className="w-full h-full object-contain" />
                </button>
              ))}
            </div>
          )}

          {/* Action Buttons */}
          <div className="grid grid-cols-2 gap-3 pt-2">
            <button
              onClick={handleAddToCart}
              disabled={addingToCart}
              className={`py-3.5 px-4 rounded-2xl font-bold text-xs sm:text-sm flex items-center justify-center gap-2 transition-all shadow-md ${
                addedSuccess ? 'bg-emerald-600 text-white' : 'bg-[#0B192C] hover:bg-[#1E3E62] text-white'
              }`}
            >
              {addedSuccess ? <Check className="w-4 h-4" /> : <ShoppingBag className="w-4 h-4" />}
              <span>{addedSuccess ? 'Added to Cart' : 'Add to Cart'}</span>
            </button>

            <button
              onClick={handleBuyNow}
              disabled={addingToCart}
              className="py-3.5 px-4 rounded-2xl font-bold text-xs sm:text-sm bg-[#FF6500] hover:bg-[#FF884B] text-white flex items-center justify-center gap-2 transition-all shadow-md"
            >
              <Zap className="w-4 h-4 fill-current" />
              <span>Buy Now</span>
            </button>
          </div>
        </div>

        {/* Product Details & Specs */}
        <div className="lg:col-span-7 space-y-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-semibold text-[#2B6CB0] uppercase tracking-wider mb-1">
              <span>{product.brandName}</span>
              <span>•</span>
              <span className="text-slate-500">{product.categoryName}</span>
            </div>
            <h1 className="text-xl sm:text-2xl font-black text-slate-900 leading-tight mb-2">
              {product.title}
            </h1>

            {/* Rating & Reviews */}
            <div className="flex items-center gap-3">
              <div className="bg-emerald-700 text-white text-xs font-bold px-2 py-1 rounded-md flex items-center gap-1 shadow-xs">
                <span>{product.averageRating?.toFixed(1)}</span>
                <Star className="w-3.5 h-3.5 fill-current" />
              </div>
              <span className="text-xs text-slate-500 font-medium">
                {product.reviewCount?.toLocaleString()} Ratings & {reviews.length} Verified Reviews
              </span>
              <Link
                to={`/compare?p1=${product.id}`}
                className="ml-auto flex items-center gap-1 text-xs font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-lg transition-colors"
              >
                <Layers className="w-3.5 h-3.5" /> Compare Specs
              </Link>
            </div>
          </div>

          {/* Price Box */}
          <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-4 sm:p-5 flex flex-wrap items-baseline gap-3">
            <span className="text-3xl font-black text-slate-900">
              ₹{product.sellingPrice?.toLocaleString('en-IN')}
            </span>
            {product.mrp > product.sellingPrice && (
              <span className="text-base text-slate-400 line-through">
                ₹{product.mrp?.toLocaleString('en-IN')}
              </span>
            )}
            {product.discountPercentage > 0 && (
              <span className="text-sm font-extrabold text-[#FF6500]">
                {product.discountPercentage}% Off
              </span>
            )}
            <div className="w-full text-xs text-slate-500 mt-1 flex items-center gap-1 font-medium">
              <span>Inclusive of all taxes. Earn</span>
              <span className="font-bold text-amber-600">
                {Math.round(product.sellingPrice * 0.05)} VYROX Coins
              </span>
              <span>with this purchase.</span>
            </div>
          </div>

          {/* Bank Offers & Coupons */}
          <div className="space-y-2">
            <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500">Available Offers</h4>
            <div className="space-y-2">
              {product.bankOffers && product.bankOffers.map((offer, idx) => (
                <div key={idx} className="flex items-start gap-2 text-xs text-slate-700 bg-emerald-50/70 border border-emerald-200/70 p-2.5 rounded-xl">
                  <Tag className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
                  <span><strong>Bank Offer:</strong> {offer}</span>
                </div>
              ))}
              <div className="flex items-start gap-2 text-xs text-slate-700 bg-purple-50/70 border border-purple-200/70 p-2.5 rounded-xl">
                <Tag className="w-4 h-4 text-purple-600 flex-shrink-0 mt-0.5" />
                <span><strong>Coupon:</strong> Apply code <strong className="text-purple-900">VYROX100</strong> for flat ₹100 instant discount.</span>
              </div>
            </div>
          </div>

          {/* Delivery Checker */}
          <div className="bg-white border border-slate-200 rounded-2xl p-4 space-y-2">
            <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <Truck className="w-4 h-4 text-slate-700" /> Delivery Options
            </h4>
            <div className="flex gap-2 max-w-sm">
              <input
                type="text"
                value={pincode}
                onChange={(e) => setPincode(e.target.value)}
                placeholder="Enter Pincode"
                className="flex-1 px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold outline-none focus:border-[#2B6CB0]"
              />
              <button
                onClick={handlePincodeCheck}
                className="px-4 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl hover:bg-[#1E3E62]"
              >
                Check
              </button>
            </div>
            <p className="text-xs font-semibold text-emerald-700 mt-1">{deliveryMessage}</p>
          </div>

          {/* Product Highlights */}
          {product.highlights && product.highlights.length > 0 && (
            <div className="space-y-2">
              <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500">Key Highlights</h4>
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-700">
                {product.highlights.map((hl, i) => (
                  <li key={i} className="flex items-center gap-2 p-2 bg-slate-50 rounded-xl border border-slate-100">
                    <Check className="w-3.5 h-3.5 text-[#FF6500] flex-shrink-0" />
                    <span>{hl}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Technical Specifications Table */}
          {product.specifications && product.specifications.length > 0 && (
            <div className="space-y-3 pt-2">
              <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500">Product Specifications</h4>
              <div className="border border-slate-200 rounded-2xl overflow-hidden divide-y divide-slate-100 bg-white">
                {product.specifications.map((spec, i) => (
                  <div key={i} className="grid grid-cols-3 p-3 text-xs hover:bg-slate-50 transition-colors">
                    <span className="text-slate-500 font-medium">{spec.name}</span>
                    <span className="col-span-2 text-slate-900 font-semibold">{spec.value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Seller & Warranty Info */}
          <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4 text-xs">
            <div>
              <div className="text-slate-500">Sold by:</div>
              <div className="font-bold text-slate-800">{product.sellerName || 'VYROX Retail'} ({product.sellerRating || 4.9}★)</div>
            </div>
            <div>
              <div className="text-slate-500">Warranty:</div>
              <div className="font-bold text-slate-800">{product.warrantyInfo || '1 Year Standard Warranty'}</div>
            </div>
            <div>
              <div className="text-slate-500">Returns:</div>
              <div className="font-bold text-emerald-700">7 Days Replacement Guarantee</div>
            </div>
          </div>
        </div>
      </div>

      {/* Customer Reviews Section */}
      <section className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-6">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Ratings & Reviews</h2>
            <div className="flex items-center gap-2 mt-1">
              <div className="bg-emerald-700 text-white text-base font-black px-2.5 py-1 rounded-lg flex items-center gap-1">
                <span>{product.averageRating?.toFixed(1)}</span>
                <Star className="w-4 h-4 fill-current" />
              </div>
              <span className="text-xs text-slate-500">Based on {product.reviewCount?.toLocaleString()} verified customer purchases</span>
            </div>
          </div>

          <button
            onClick={() => setShowReviewModal(true)}
            className="px-4 py-2.5 bg-[#0B192C] hover:bg-[#1E3E62] text-white text-xs font-bold rounded-xl transition-colors flex items-center gap-1.5 self-start sm:self-auto"
          >
            <Plus className="w-4 h-4" /> Rate Product
          </button>
        </div>

        {/* Reviews List */}
        <div className="space-y-4 divide-y divide-slate-100">
          {reviews.map((rev) => (
            <div key={rev.id} className="pt-4 space-y-2">
              <div className="flex items-center gap-2">
                <div className="bg-emerald-700 text-white text-[10px] font-bold px-1.5 py-0.5 rounded flex items-center gap-0.5">
                  <span>{rev.rating}</span>
                  <Star className="w-2.5 h-2.5 fill-current" />
                </div>
                <h4 className="font-bold text-xs sm:text-sm text-slate-900">{rev.title}</h4>
              </div>
              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{rev.comment}</p>
              <div className="flex items-center gap-3 text-[11px] text-slate-400 font-medium pt-1">
                <span>{rev.reviewerName}</span>
                <span>•</span>
                <span className="text-emerald-600 font-semibold">✓ Verified Buyer</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Similar Products */}
      {similar.length > 0 && (
        <section className="space-y-4">
          <h2 className="text-lg sm:text-xl font-bold text-slate-900">Similar Products You May Like</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {similar.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}

      {/* Write Review Modal */}
      {showReviewModal && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
            <h3 className="font-bold text-base text-slate-900">Write a Review</h3>
            <form onSubmit={handleAddReview} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Your Rating</label>
                <div className="flex gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      onClick={() => setReviewRating(star)}
                      className="p-1 text-slate-300 hover:text-amber-400"
                    >
                      <Star className={`w-6 h-6 ${star <= reviewRating ? 'fill-amber-400 text-amber-400' : ''}`} />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Review Title</label>
                <input
                  type="text"
                  required
                  value={reviewTitle}
                  onChange={(e) => setReviewTitle(e.target.value)}
                  placeholder="e.g. Excellent build and battery life"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">Your Detailed Experience</label>
                <textarea
                  required
                  rows={3}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="What did you like or dislike about this product?"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowReviewModal(false)}
                  className="px-4 py-2 bg-slate-100 text-slate-700 text-xs font-bold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-[#0B192C] text-white text-xs font-bold rounded-xl hover:bg-[#1E3E62]"
                >
                  Submit Review
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
