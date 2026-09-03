import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { CategoryBar } from './components/CategoryBar';
import { Footer } from './components/Footer';
import { AiChatAssistant } from './components/AiChatAssistant';
import { VoiceSearchModal } from './components/VoiceSearchModal';
import { ImageSearchModal } from './components/ImageSearchModal';

import { HomePage } from './pages/HomePage';
import { TopDealsPage } from './pages/TopDealsPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { ComparePage } from './pages/ComparePage';
import { CartPage } from './pages/CartPage';
import { CheckoutPage } from './pages/CheckoutPage';
import { LiveTrackingPage } from './pages/LiveTrackingPage';
import { AccountPage } from './pages/AccountPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { SellerPage } from './pages/SellerPage';
import { AdminPage } from './pages/AdminPage';
import { DeliveryPage } from './pages/DeliveryPage';

export const App: React.FC = () => {
  const [voiceSearchOpen, setVoiceSearchOpen] = useState(false);
  const [imageSearchOpen, setImageSearchOpen] = useState(false);

  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col justify-between bg-[#F8F9FD]">
          <div>
            <Navbar
              onOpenVoiceSearch={() => setVoiceSearchOpen(true)}
              onOpenImageSearch={() => setImageSearchOpen(true)}
            />
            <CategoryBar />

            <main>
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/top-deals" element={<TopDealsPage />} />
                <Route path="/product/:id" element={<ProductDetailPage />} />
                <Route path="/compare" element={<ComparePage />} />
                <Route path="/cart" element={<CartPage />} />
                <Route path="/checkout" element={<CheckoutPage />} />
                <Route path="/orders/:orderNumber/track" element={<LiveTrackingPage />} />
                <Route path="/account" element={<AccountPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/seller" element={<SellerPage />} />
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/delivery" element={<DeliveryPage />} />
              </Routes>
            </main>
          </div>

          <Footer />

          {/* Floating Features */}
          <AiChatAssistant />
          <VoiceSearchModal isOpen={voiceSearchOpen} onClose={() => setVoiceSearchOpen(false)} />
          <ImageSearchModal isOpen={imageSearchOpen} onClose={() => setImageSearchOpen(false)} />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
};
