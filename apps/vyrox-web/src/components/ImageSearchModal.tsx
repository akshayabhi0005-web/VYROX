import React, { useState } from 'react';
import { Camera, Upload, X, Search, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface ImageSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ImageSearchModal: React.FC<ImageSearchModalProps> = ({ isOpen, onClose }) => {
  const [preview, setPreview] = useState<string | null>(null);
  const [analyzing, setAnalyzing] = useState(false);
  const navigate = useNavigate();

  if (!isOpen) return null;

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setPreview(reader.result as string);
        runVisualSearch();
      };
      reader.readAsDataURL(file);
    }
  };

  const runVisualSearch = () => {
    setAnalyzing(true);
    // Simulate AI feature extraction from image and match catalog
    setTimeout(() => {
      setAnalyzing(false);
      onClose();
      navigate('/top-deals?query=electronics');
    }, 1800);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-md w-full shadow-2xl border border-slate-200 text-center relative animate-in zoom-in-95">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="font-bold text-lg text-slate-900 mb-1">VYROX Visual Lens</h3>
        <p className="text-xs text-slate-500 mb-6">Upload a photo or screenshot to find matching products from our catalog.</p>

        {analyzing ? (
          <div className="py-8 flex flex-col items-center gap-3">
            <div className="relative">
              {preview && <img src={preview} alt="Upload" className="w-28 h-28 object-cover rounded-2xl opacity-50 blur-xs" />}
              <div className="absolute inset-0 flex items-center justify-center">
                <Sparkles className="w-8 h-8 text-[#FF6500] animate-spin" />
              </div>
            </div>
            <p className="text-sm font-bold text-slate-800">Analyzing visual features...</p>
            <p className="text-xs text-slate-400">Matching against 10,000+ catalog SKUs</p>
          </div>
        ) : (
          <div className="space-y-4">
            <label className="border-2 border-dashed border-slate-300 hover:border-[#2B6CB0] rounded-2xl p-6 flex flex-col items-center justify-center gap-2 cursor-pointer bg-slate-50 hover:bg-blue-50/50 transition-colors">
              <Upload className="w-8 h-8 text-slate-400" />
              <span className="text-xs font-semibold text-slate-700">Click to upload product image</span>
              <span className="text-[10px] text-slate-400">PNG, JPG, WEBP up to 5MB</span>
              <input type="file" accept="image/*" onChange={handleFileSelect} className="hidden" />
            </label>

            <div className="text-xs text-slate-400 font-medium">OR TRY POPULAR VISUAL SAMPLES</div>
            <div className="flex justify-center gap-3">
              {[
                { name: 'MacBook', query: 'laptop', img: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=200&auto=format&fit=crop&q=80' },
                { name: 'Air Max', query: 'shoes', img: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=200&auto=format&fit=crop&q=80' },
                { name: 'Sony ANC', query: 'headphone', img: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&auto=format&fit=crop&q=80' }
              ].map((sample, i) => (
                <button
                  key={i}
                  onClick={() => {
                    onClose();
                    navigate(`/top-deals?query=${sample.query}`);
                  }}
                  className="p-1 border border-slate-200 rounded-xl hover:border-[#2B6CB0] bg-white group flex flex-col items-center text-[10px] font-medium text-slate-600"
                >
                  <img src={sample.img} alt={sample.name} className="w-14 h-14 object-cover rounded-lg mb-1" />
                  <span>{sample.name}</span>
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
