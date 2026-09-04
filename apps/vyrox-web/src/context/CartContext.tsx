import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { CartResponse, CartItem, ProductSummary, ProductDetail } from '../types';
import { apiClient } from '../api/apiClient';
import { useAuth } from './AuthContext';

export interface CartContextType {
  cart: CartResponse;
  cartCount: number;
  isLoading: boolean;
  addToCart: (product: ProductSummary | ProductDetail, quantity?: number) => Promise<boolean>;
  updateQuantity: (itemId: number, quantity: number) => Promise<void>;
  removeFromCart: (itemId: number) => Promise<void>;
  saveForLater: (itemId: number, save: boolean) => Promise<void>;
  clearCart: () => void;
  refreshCart: () => Promise<void>;
}

const defaultDemoCart: CartResponse = {
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
    },
    {
      itemId: 2,
      productId: 203,
      productTitle: 'Sony WH-1000XM5 Wireless Industry Leading Noise Canceling Headphones',
      productSku: 'VYR-AUD-001',
      categoryName: 'Electronics',
      brandName: 'Sony',
      mainImageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80',
      mrp: 34990,
      sellingPrice: 26990,
      discountPercentage: 23,
      quantity: 1,
      savedForLater: false,
      estimatedDelivery: '⁚ 15-Minute Instant Delivery',
      inStock: true,
    }
  ],
  savedForLaterItems: [],
  totalItems: 2,
  subtotal: 175890,
  totalSavings: 19000,
  deliveryFee: 0,
  grandTotal: 175890,
  potentialCoinsEarned: 8794,
};

function calculateCartTotals(items: CartItem[], savedItems: CartItem[] = []): CartResponse {
  const subtotal = items.reduce((sum, item) => sum + (item.sellingPrice || 0) * (item.quantity || 1), 0);
  const totalMrp = items.reduce((sum, item) => sum + (item.mrp || item.sellingPrice || 0) * (item.quantity || 1), 0);
  const totalSavings = Math.max(0, totalMrp - subtotal);
  const totalItems = items.reduce((sum, item) => sum + (item.quantity || 1), 0);
  const deliveryFee = subtotal > 500 || items.length === 0 ? 0 : 40;

  return {
    cartId: 1,
    items,
    savedForLaterItems: savedItems,
    totalItems,
    subtotal,
    totalSavings,
    deliveryFee,
    grandTotal: subtotal + deliveryFee,
    potentialCoinsEarned: Math.round(subtotal * 0.05),
  };
}

export const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const [cart, setCart] = useState<CartResponse>(() => {
    const saved = localStorage.getItem('vyrox_local_cart') || localStorage.getItem('vyrox_guest_cart');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && Array.isArray(parsed.items)) {
          return calculateCartTotals(parsed.items, parsed.savedForLaterItems || []);
        }
      } catch (e) {
        console.warn('Error reading saved cart from localStorage', e);
      }
    }
    return defaultDemoCart;
  });

  const saveCart = useCallback((newCart: CartResponse) => {
    setCart(newCart);
    try {
      localStorage.setItem('vyrox_local_cart', JSON.stringify(newCart));
      localStorage.setItem('vyrox_guest_cart', JSON.stringify(newCart));
    } catch (e) {
      console.warn('Error saving cart to localStorage', e);
    }
  }, []);

  // Intelligent Guest -> Authenticated Cart Merge
  const mergeAndRefreshCart = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setIsLoading(true);
      const res = await apiClient.get('/cart');
      const serverItems: CartItem[] = (res.data && Array.isArray(res.data.items)) ? res.data.items : [];
      const guestItems = [...cart.items];

      // Merge guest items into server items
      const mergedItems = [...serverItems];
      for (const gItem of guestItems) {
        const existingIndex = mergedItems.findIndex((it) => it.productId === gItem.productId);
        if (existingIndex >= 0) {
          // Combine quantities
          mergedItems[existingIndex] = {
            ...mergedItems[existingIndex],
            quantity: mergedItems[existingIndex].quantity + gItem.quantity,
          };
        } else {
          mergedItems.push(gItem);
          // Sync to backend if possible
          apiClient.post('/cart/add', {
            productId: gItem.productId,
            quantity: gItem.quantity,
          }).catch(() => {});
        }
      }

      const fullCart = calculateCartTotals(
        mergedItems.length > 0 ? mergedItems : guestItems,
        (cart.savedForLaterItems || [])
      );

      saveCart(fullCart);
    } catch (err) {
      // Keep current local cart
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, cart.items, cart.savedForLaterItems, saveCart]);

  useEffect(() => {
    if (isAuthenticated) {
      mergeAndRefreshCart();
    }
  }, [isAuthenticated]);

  const refreshCart = async () => {
    await mergeAndRefreshCart();
  };

  const addToCart = async (product: ProductSummary | ProductDetail, quantity = 1): Promise<boolean> => {
    try {
      const currentItems = [...cart.items];
      const existingIdx = currentItems.findIndex((it) => it.productId === product.id);

      if (existingIdx >= 0) {
        currentItems[existingIdx] = {
          ...currentItems[existingIdx],
          quantity: currentItems[existingIdx].quantity + quantity,
        };
      } else {
        const newItem: CartItem = {
          itemId: Date.now() + Math.floor(Math.random() * 1000),
          productId: product.id,
          productTitle: product.title,
          productSku: product.sku || ('VYR-' + product.id),
          categoryName: product.categoryName || 'General',
          brandName: product.brandName || 'VYROX',
          mainImageUrl: product.mainImageUrl || '',
          mrp: product.mrp || product.sellingPrice,
          sellingPrice: product.sellingPrice,
          discountPercentage: product.discountPercentage || 0,
          quantity: quantity,
          savedForLater: false,
          estimatedDelivery: product.estimatedDeliveryDays || 'Tomorrow, by 11 AM',
          inStock: true,
        };
        currentItems.push(newItem);
      }

      const updatedCart = calculateCartTotals(currentItems, cart.savedForLaterItems);
      saveCart(updatedCart);

      if (isAuthenticated) {
        apiClient.post('/cart/add', { productId: product.id, quantity }).catch(() => {});
      }

      return true;
    } catch (err) {
      console.error('Failed to add product to cart', err);
      return false;
    }
  };

  const updateQuantity = async (itemId: number, quantity: number) => {
    if (quantity < 1) {
      await removeFromCart(itemId);
      return;
    }


    const updatedItems = cart.items.map((it) => (it.itemId === itemId ? { ...it, quantity } : it));
    const newCart = calculateCartTotals(updatedItems, cart.savedForLaterItems);
    saveCart(newCart);

    if (isAuthenticated) {
      try {
        await apiClient.put('/cart/items/' + itemId, { quantity });
      } catch (e) {}
    }
  };


  const removeFromCart = async (itemId: number) => {
    const updatedItems = cart.items.filter((it) => it.itemId !== itemId);
    const newCart = calculateCartTotals(updatedItems, cart.savedForLaterItems);
    saveCart(newCart);

    if (isAuthenticated) {
      try {
        await apiClient.delete('/cart/items/' + itemId);
      } catch (e) {}
    }
  };

  const saveForLater = async (itemId: number, save: boolean) => {
    if (save) {
      const itemToSave = cart.items.find((it) => it.itemId === itemId);
      if (!itemToSave) return;
      const updatedItems = cart.items.filter((it) => it.itemId !== itemId);
      const updatedSaved = [...cart.savedForLaterItems, { ...itemToSave, savedForLater: true }];
      const newCart = calculateCartTotals(updatedItems, updatedSaved);
      saveCart(newCart);
    } else {
      const itemToMove = cart.savedForLaterItems.find((it) => it.itemId === itemId);
      if (!itemToMove) return;
      const updatedSaved = cart.savedForLaterItems.filter((it) => it.itemId !== itemId);
      const updatedItems = [...cart.items, { ...itemToMove, savedForLater: false }];
      const newCart = calculateCartTotals(updatedItems, updatedSaved);
      saveCart(newCart);
    }
  };

  const clearCart = () => {
    const emptyCart: CartResponse = {
      cartId: 1,
      items: [],
      savedForLaterItems: [],
      totalItems: 0,
      subtotal: 0,
      totalSavings: 0,
      deliveryFee: 0,
      grandTotal: 0,
      potentialCoinsEarned: 0,
    };
    saveCart(emptyCart);
  };

  const cartCount = cart.items.reduce((sum, item) => sum + (item.quantity || 1), 0);


  return (
    <CartContext.Provider
      value={{
        cart,
        cartCount,
        isLoading,
        addToCart,
        updateQuantity,
        removeFromCart,
        saveForLater,
        clearCart,
        refreshCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};