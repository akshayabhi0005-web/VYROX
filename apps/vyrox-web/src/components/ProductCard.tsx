import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Star, ShoppingBag, Heart, Layers, Zap, Check } from 'lucide-react';
import { ProductSummary } from '../types';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/apiClient';

interface ProductCardProps {
  product: ProductSummary;
  onAddToCartSuccess?: () => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, onAddToCartSuccess }) => {
  const { isAuthenticated, setPendingAction } = useAuth();
  const navigate = useNavigate();
  const [isAdding, setIsAdding] = React.useState(false);
  const [added, setAdded] = React.useState(false);
  const [isWishlisted, setIsWishlisted] = React.useState(false);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      // Guest interception: save pending action and route to login
      setPendingAction({
        type: 'ADD_TO_CART',
        productId: product.id,
        quantity: 1,
      });
      navigate('/login?redirect=' + encodeURIComponent(window.location.pathname));
      return;
    }

    try {
      setIsAdding(true);
      await apiClient.post('/cart/add', { productId: product.id, quantity: 1 });
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
      if (onAddToCartSuccess) onAddToCartSuccess();
    } catch (err) {
      console.error('Failed to add to cart', err);
    } finally {
      setIsAdding(false);
    }
  };

  const handleWishlistToggle = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      setPendingAction({
        type: 'ADD_TO_WISHLIST',
        productId: product.id,
      });
      navigate('/login?redirect=' + encodeURIComponent(window.location.pathname));
      return;
    }

    try {
      if (isWishlisted) {
        await apiClient.delete(`/wishlist/remove/${product.id}`);
        setIsWishlisted(false);
      } else {
        await apiClient.post(`/wishlist/add/${product.id}`);
        setIsWishlisted(true);
      }
    } catch (err) {
      console.error('Failed to update wishlist', err);
    }
  };

  return (
    <div className="group bg-white rounded-2xl border border-slate-200/80 hover:border-slate-300 hover:shadow-xl transition-all duration-300 flex flex-col justify-between overflow-hidden relative">
      {/* Top Badges */}
      <div className="absolute top-2.5 left-2.5 z-10 flex flex-col gap-1 items-start">
        {product.discountPercentage > 0 && (
          <span className="bg-[#FF6500] text-white text-[10px] font-extrabold px-2 py-0.5 rounded-md shadow-sm">
            {product.discountPercentage}% OFF
          </span>
        )}
        {product.isQuickCommerceEligible && (
          <span className="bg-emerald-600 text-white text-[9px] font-bold px-1.5 py-0.5 rounded flex items-center gap-0.5 shadow-sm">
            <Zap className="w-2.5 h-2.5 fill-current" /> 15-MIN
          </span>
        )}
      </div>

      {/* Wishlist Button */}
      <button
        onClick={handleWishlistToggle}
        className="absolute top-2.5 right-2.5 z-10 p-2 bg-white/80 hover:bg-white text-slate-400 hover:text-[#FF6500] rounded-full shadow-sm backdrop-blur-sm transition-all"
        title="Save to Wishlist"
      >
        <Heart className={`w-4 h-4 ${isWishlisted ? 'fill-[#FF6500] text-[#FF6500]' : ''}`} />
      </button>

      {/* Product Image */}
      <Link to={`/product/${product.id}`} className="block p-4 bg-slate-50/50 aspect-square overflow-hidden relative">
        <img
          src={product.mainImageUrl}
          alt={product.title}
          className="w-full h-full object-contain mix-blend-multiply group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
        />
      </Link>

      {/* Content */}
      <div className="p-4 flex-1 flex flex-col justify-between">
        <div>
          {/* Brand & Category */}
          <div className="flex items-center justify-between text-[11px] text-slate-500 font-medium mb-1">
            <span className="text-[#2B6CB0] font-semibold uppercase tracking-wider">{product.brandName || 'VYROX'}</span>
            <span>{product.categoryName}</span>
          </div>

          {/* Title */}
          <Link to={`/product/${product.id}`}>
            <h3 className="text-xs sm:text-sm font-semibold text-slate-800 line-clamp-2 hover:text-[#2B6CB0] transition-colors leading-snug mb-2">
              {product.title}
            </h3>
          </Link>

          {/* Rating Badge */}
          <div className="flex items-center gap-2 mb-2.5">
            <div className="bg-emerald-700 text-white text-[11px] font-bold px-1.5 py-0.5 rounded flex items-center gap-0.5">
              <span>{product.averageRating.toFixed(1)}</span>
              <Star className="w-3 h-3 fill-current" />
            </div>
            <span className="text-[11px] text-slate-400 font-medium">({product.reviewCount?.toLocaleString()})</span>
          </div>

          {/* Price Section */}
          <div className="flex items-baseline gap-2 mb-2">
            <span className="text-base sm:text-lg font-black text-slate-900">
              ₹{product.sellingPrice?.toLocaleString('en-IN')}
            </span>
            {product.mrp > product.sellingPrice && (
              <span className="text-xs text-slate-400 line-through">
                ₹{product.mrp?.toLocaleString('en-IN')}
              </span>
            )}
          </div>

          {/* Delivery Tag */}
          <div className="text-[11px] text-slate-500 mb-3 flex items-center gap-1 font-medium">
            <span>Delivery:</span>
            <span className="font-semibold text-slate-700">{product.estimatedDeliveryDays || 'Tomorrow'}</span>
            {product.freeDelivery && <span className="text-emerald-600 font-bold ml-1">Free</span>}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100">
          <Link
            to={`/compare?p1=${product.id}`}
            className="flex items-center justify-center gap-1 text-[11px] font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 py-2 rounded-xl transition-colors"
          >
            <Layers className="w-3.5 h-3.5 text-slate-500" />
            <span>Compare</span>
          </Link>

          <button
            onClick={handleAddToCart}
            disabled={isAdding}
            className={`flex items-center justify-center gap-1 text-[11px] font-bold py-2 rounded-xl transition-all shadow-sm ${
              added
                ? 'bg-emerald-600 text-white'
                : 'bg-[#0B192C] hover:bg-[#1E3E62] text-white'
            }`}
          >
            {added ? (
              <>
                <Check className="w-3.5 h-3.5" /> Added
              </>
            ) : (
              <>
                <ShoppingBag className="w-3.5 h-3.5" /> Add to Cart
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
