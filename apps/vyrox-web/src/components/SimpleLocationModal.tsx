import React, { useState, useEffect } from 'react';
import { MapPin, Navigation, Search, CheckCircle2, X, AlertCircle, Loader2, Home, Building2 } from 'lucide-react';
import { reverseGeocode, getCurrentGpsCoordinates } from '../services/mapService';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

interface SimpleLocationModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentCity: string;
  currentPincode: string;
  onLocationSelected: (location: {
    lat: number;
    lng: number;
    address: string;
    city: string;
    pincode: string;
    locality: string;
  }) => void;
}

export const SimpleLocationModal: React.FC<SimpleLocationModalProps> = ({
  isOpen,
  onClose,
  currentCity,
  currentPincode,
  onLocationSelected,
}) => {
  const { isAuthenticated } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isLocating, setIsLocating] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [manualPincode, setManualPincode] = useState(currentPincode);
  const [manualCity, setManualCity] = useState(currentCity.split(',')[0] || 'Indiranagar');

  const handleUseCurrentGPS = async () => {
    setIsLocating(true);
    setErrorMsg(null);

    if (!('geolocation' in navigator)) {
      setErrorMsg('Geolocation is not supported by your browser.');
      setIsLocating(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          const details = await reverseGeocode(lat, lng);
          
          const payload = {
            lat,
            lng,
            address: details.formattedAddress,
            city: details.city || 'Bengaluru',
            pincode: details.postalCode || '560038',
            locality: details.locality || 'Indiranagar',
          };

          localStorage.setItem('vyrox_saved_location', JSON.stringify(payload));
          onLocationSelected(payload);
          setIsLocating(false);
          onClose();
        } catch (err) {
          setIsLocating(false);
          setErrorMsg('Failed to determine location name from coordinates.');
        }
      },
      (error) => {
        setIsLocating(false);
        if (error.code === error.PERMISSION_DENIED) {
          setErrorMsg('Location permission was denied. Please enter your pincode or search below.');
        } else if (error.code === error.TIMEOUT) {
          setErrorMsg('GPS request timed out. Please enter your address or pincode manually.');
        } else {
          setErrorMsg('GPS location unavailable. Please search your area or enter pincode.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  const handleSearchPlaces = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setErrorMsg(null);
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(
          searchQuery.trim()
        )}&limit=5`,
        {
          headers: {
            'Accept-Language': 'en',
          },
        }
      );
      if (!res.ok) throw new Error('Search failed');
      const data = await res.json();
      setSearchResults(data || []);
      if (data.length === 0) {
        setErrorMsg(`No locations found for "${searchQuery}". Try searching by area name or pincode.`);
      }
    } catch (err) {
      setErrorMsg('Search failed. Please enter your pincode directly.');
    } finally {
      setIsSearching(false);
    }
  };

  const handleSelectSearchResult = async (item: any) => {
    const lat = parseFloat(item.lat);
    const lng = parseFloat(item.lon);
    try {
      const details = await reverseGeocode(lat, lng);
      const payload = {
        lat,
        lng,
        address: item.display_name,
        city: details.city || 'Bengaluru',
        pincode: details.postalCode || '560038',
        locality: details.locality || item.name || 'Indiranagar',
      };
      localStorage.setItem('vyrox_saved_location', JSON.stringify(payload));
      onLocationSelected(payload);
      onClose();
    } catch (err) {
      const payload = {
        lat,
        lng,
        address: item.display_name,
        city: 'Bengaluru',
        pincode: '560038',
        locality: item.name || 'Indiranagar',
      };
      localStorage.setItem('vyrox_saved_location', JSON.stringify(payload));
      onLocationSelected(payload);
      onClose();
    }
  };

  const handleApplyManualPincode = () => {
    const cleanPin = manualPincode.trim();
    if (cleanPin.length < 5) {
      setErrorMsg('Please enter a valid pincode.');
      return;
    }
    const payload = {
      lat: 12.9716,
      lng: 77.5946,
      address: `${manualCity.trim()}, Pincode: ${cleanPin}`,
      city: manualCity.trim() || 'Bengaluru',
      pincode: cleanPin,
      locality: manualCity.trim() || 'Indiranagar',
    };
    localStorage.setItem('vyrox_saved_location', JSON.stringify(payload));
    onLocationSelected(payload);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 relative animate-in zoom-in-95 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between pb-2 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-orange-100 text-[#FF6500] flex items-center justify-center font-bold">
              <MapPin className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-extrabold text-base text-slate-900">Select Delivery Location</h3>
              <p className="text-xs text-slate-500">Check 15-Min Quick Commerce & delivery availability</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* GPS Instant Detection Button */}
        <button
          type="button"
          onClick={handleUseCurrentGPS}
          disabled={isLocating}
          className="w-full py-3 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs sm:text-sm rounded-2xl shadow-md transition-all flex items-center justify-center gap-2"
        >
          {isLocating ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Detecting Current Location via GPS...</span>
            </>
          ) : (
            <>
              <Navigation className="w-4 h-4" />
              <span>Use Current GPS Location</span>
            </>
          )}
        </button>

        {errorMsg && (
          <div className="p-2.5 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700 flex items-start gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        {/* Area / Landmark Search */}
        <form onSubmit={handleSearchPlaces} className="relative">
          <label className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider mb-1">
            Search Area or Landmark
          </label>
          <div className="relative flex items-center">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="e.g. Indiranagar, Koramangala, Bandra..."
              className="w-full pl-9 pr-16 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0] focus:bg-white"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3" />
            <button
              type="submit"
              disabled={isSearching}
              className="absolute right-1.5 px-3 py-1.5 bg-[#0B192C] text-white text-xs font-bold rounded-lg hover:bg-[#1E3E62]"
            >
              {isSearching ? <Loader2 className="w-3 h-3 animate-spin" /> : 'Find'}
            </button>
          </div>
        </form>

        {/* Search Results List */}
        {searchResults.length > 0 && (
          <div className="bg-slate-50 rounded-2xl border border-slate-200 divide-y divide-slate-100 max-h-40 overflow-y-auto">
            {searchResults.map((item, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => handleSelectSearchResult(item)}
                className="w-full text-left p-2.5 text-xs hover:bg-white flex items-start gap-2 text-slate-700 transition-colors"
              >
                <MapPin className="w-3.5 h-3.5 text-[#FF6500] flex-shrink-0 mt-0.5" />
                <span className="line-clamp-2">{item.display_name}</span>
              </button>
            ))}
          </div>
        )}

        {/* Manual Pincode & City Option */}
        <div className="pt-2 border-t border-slate-100 space-y-2">
          <label className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider">
            Or Enter Pincode Manually
          </label>
          <div className="flex gap-2">
            <input
              type="text"
              value={manualPincode}
              onChange={(e) => setManualPincode(e.target.value)}
              placeholder="6-digit Pincode"
              maxLength={6}
              className="w-1/2 px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
            />
            <button
              type="button"
              onClick={handleApplyManualPincode}
              className="flex-1 py-2 bg-[#0B192C] hover:bg-[#1E3E62] text-white font-bold text-xs rounded-xl transition-all"
            >
              Apply Pincode
            </button>
          </div>
        </div>

        <div className="p-3 bg-amber-50 border border-amber-200 rounded-2xl text-[11px] text-amber-800">
          ⚡ <strong>15-Min Quick Commerce</strong> is live in active darkstore zones (560038, 560001, 400001).
        </div>
      </div>
    </div>
  );
};
