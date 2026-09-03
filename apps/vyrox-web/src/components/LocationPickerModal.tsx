import React, { useState, useEffect, useRef, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import { 
  MapPin, Navigation, Search, CheckCircle2, X, AlertCircle, 
  Loader2, Compass, Home, Building2 
} from 'lucide-react';
import { reverseGeocode, GeocodeResult } from '../services/mapService';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

// Fix Leaflet default icon issues in React
const customPinIcon = L.divIcon({
  className: 'custom-location-picker-marker',
  html: `<div style="background-color: #FF6500; width: 38px; height: 38px; border-radius: 50% 50% 50% 0; transform: rotate(-45deg); border: 3px solid white; box-shadow: 0 4px 12px rgba(0,0,0,0.35); display: flex; items-center; justify-content: center;"><div style="transform: rotate(45deg); color: white; font-size: 16px; margin-top: 6px; margin-left: 6px;">📍</div></div>`,
  iconSize: [38, 38],
  iconAnchor: [19, 38],
  popupAnchor: [0, -38],
});

interface LocationPickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialLat?: number;
  initialLng?: number;
  onLocationSelected: (location: {
    lat: number;
    lng: number;
    address: string;
    city: string;
    pincode: string;
    locality: string;
  }) => void;
}

// Helper component to handle map click and update view
const MapController: React.FC<{
  position: [number, number];
  onPositionChange: (lat: number, lng: number) => void;
}> = ({ position, onPositionChange }) => {
  const map = useMap();

  useEffect(() => {
    map.setView(position, map.getZoom());
    // Invalidate map size after rendering to avoid gray tiles
    setTimeout(() => {
      map.invalidateSize();
    }, 200);
  }, [position, map]);

  useMapEvents({
    click(e) {
      onPositionChange(e.latlng.lat, e.latlng.lng);
    },
  });

  return null;
};

export const LocationPickerModal: React.FC<LocationPickerModalProps> = ({
  isOpen,
  onClose,
  initialLat = 12.9716,
  initialLng = 77.5946,
  onLocationSelected,
}) => {
  const { isAuthenticated } = useAuth();
  const [lat, setLat] = useState<number>(initialLat);
  const [lng, setLng] = useState<number>(initialLng);
  const [addressDetails, setAddressDetails] = useState<GeocodeResult | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isLocating, setIsLocating] = useState(false);
  const [isGeocoding, setIsGeocoding] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [manualHouseNumber, setManualHouseNumber] = useState('');
  const [manualLandmark, setManualLandmark] = useState('');
  const [addressType, setAddressType] = useState<'HOME' | 'WORK' | 'OTHER'>('HOME');

  // Reverse geocode when lat/lng changes
  const fetchAddress = async (targetLat: number, targetLng: number) => {
    setIsGeocoding(true);
    setErrorMsg(null);
    try {
      const details = await reverseGeocode(targetLat, targetLng);
      setAddressDetails(details);
    } catch (err) {
      setErrorMsg('Failed to reverse geocode location. You can enter details manually.');
    } finally {
      setIsGeocoding(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setLat(initialLat);
      setLng(initialLng);
      fetchAddress(initialLat, initialLng);
    }
  }, [isOpen, initialLat, initialLng]);

  // Handle GPS button click
  const handleDetectGPS = () => {
    setIsLocating(true);
    setErrorMsg(null);
    if (!('geolocation' in navigator)) {
      setErrorMsg('Geolocation is not supported by your browser.');
      setIsLocating(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const newLat = position.coords.latitude;
        const newLng = position.coords.longitude;
        setLat(newLat);
        setLng(newLng);
        fetchAddress(newLat, newLng);
        setIsLocating(false);
      },
      (error) => {
        setIsLocating(false);
        if (error.code === error.PERMISSION_DENIED) {
          setErrorMsg('Location permission was denied. Please select your location on the map or search above.');
        } else if (error.code === error.TIMEOUT) {
          setErrorMsg('GPS request timed out. Please click on the map to place your pin.');
        } else {
          setErrorMsg('GPS location unavailable. Please pick a location manually on the map.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  // Search places via OpenStreetMap Nominatim
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setErrorMsg(null);
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(
          searchQuery
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
        setErrorMsg(`No locations found for "${searchQuery}".`);
      }
    } catch (err) {
      setErrorMsg('Location search failed. Please try again.');
    } finally {
      setIsSearching(false);
    }
  };

  const handleSelectSearchResult = (item: any) => {
    const newLat = parseFloat(item.lat);
    const newLng = parseFloat(item.lon);
    setLat(newLat);
    setLng(newLng);
    setSearchResults([]);
    setSearchQuery(item.display_name);
    fetchAddress(newLat, newLng);
  };

  const handleMarkerDragEnd = (event: any) => {
    const marker = event.target;
    const position = marker.getLatLng();
    setLat(position.lat);
    setLng(position.lng);
    fetchAddress(position.lat, position.lng);
  };

  const handleConfirmLocation = async () => {
    const formatted = [
      manualHouseNumber.trim(),
      manualLandmark.trim() ? `Near ${manualLandmark.trim()}` : '',
      addressDetails?.formattedAddress || `${lat.toFixed(4)}, ${lng.toFixed(4)}`,
    ]
      .filter(Boolean)
      .join(', ');

    const payload = {
      lat,
      lng,
      address: formatted,
      city: addressDetails?.city || 'Bengaluru',
      pincode: addressDetails?.postalCode || '560038',
      locality: addressDetails?.locality || 'Indiranagar',
    };

    // Save locally
    localStorage.setItem('vyrox_saved_location', JSON.stringify(payload));

    // If authenticated, also save to backend /addresses
    if (isAuthenticated) {
      try {
        await apiClient.post('/addresses', {
          recipientName: 'My Delivery Address',
          mobileNumber: '9876543210',
          addressLine1: manualHouseNumber.trim() || addressDetails?.locality || 'Main Road',
          addressLine2: manualLandmark.trim() || '',
          city: payload.city,
          state: addressDetails?.state || 'Karnataka',
          postalCode: payload.pincode,
          addressType: addressType,
          isDefault: true,
          latitude: lat,
          longitude: lng,
        });
      } catch (e) {
        console.warn('Could not persist address to backend, saved locally', e);
      }
    }

    onLocationSelected(payload);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-white rounded-3xl max-w-2xl w-full shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[92vh] animate-in zoom-in-95">
        {/* Header */}
        <div className="p-4 sm:p-5 bg-[#0B192C] text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#FF6500] text-white flex items-center justify-center font-bold">
              <MapPin className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-extrabold text-base sm:text-lg text-white">Choose Delivery Location</h3>
              <p className="text-xs text-slate-300">OpenStreetMap Interactive Precision Location Picker</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-300 hover:text-white rounded-full hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Search Bar & GPS Trigger */}
        <div className="p-3 sm:p-4 bg-slate-50 border-b border-slate-200 space-y-2">
          <div className="flex gap-2">
            <form onSubmit={handleSearch} className="flex-1 relative">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search area, landmark, street, or city (e.g. Koramangala 4th Block)..."
                className="w-full pl-9 pr-10 py-2.5 bg-white border border-slate-300 rounded-xl text-xs outline-none focus:border-[#2B6CB0] focus:ring-1 focus:ring-[#2B6CB0]"
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-3 text-slate-400 hover:text-slate-600"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </form>

            <button
              type="button"
              onClick={handleDetectGPS}
              disabled={isLocating}
              className="px-3.5 py-2.5 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs rounded-xl flex items-center gap-1.5 shadow-sm transition-all flex-shrink-0"
              title="Use Device GPS"
            >
              {isLocating ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Navigation className="w-4 h-4" />
              )}
              <span className="hidden sm:inline">Use GPS</span>
            </button>
          </div>

          {/* Search suggestions dropdown */}
          {searchResults.length > 0 && (
            <div className="bg-white rounded-xl border border-slate-200 shadow-lg divide-y divide-slate-100 max-h-40 overflow-y-auto">
              {searchResults.map((item, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handleSelectSearchResult(item)}
                  className="w-full text-left px-3 py-2 text-xs hover:bg-slate-50 flex items-start gap-2 text-slate-700"
                >
                  <MapPin className="w-3.5 h-3.5 text-[#FF6500] flex-shrink-0 mt-0.5" />
                  <span className="line-clamp-2">{item.display_name}</span>
                </button>
              ))}
            </div>
          )}

          {errorMsg && (
            <div className="p-2.5 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}
        </div>

        {/* Leaflet OpenStreetMap Container */}
        <div className="relative h-64 sm:h-72 w-full bg-slate-100 border-b border-slate-200">
          <MapContainer
            center={[lat, lng]}
            zoom={16}
            scrollWheelZoom={true}
            style={{ height: '100%', width: '100%', zIndex: 10 }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <Marker
              position={[lat, lng]}
              icon={customPinIcon}
              draggable={true}
              eventHandlers={{
                dragend: handleMarkerDragEnd,
              }}
            />
            <MapController
              position={[lat, lng]}
              onPositionChange={(newLat, newLng) => {
                setLat(newLat);
                setLng(newLng);
                fetchAddress(newLat, newLng);
              }}
            />
          </MapContainer>

          <div className="absolute bottom-2 left-2 z-20 bg-white/90 backdrop-blur-xs px-2.5 py-1 rounded-lg text-[10px] font-semibold text-slate-700 border border-slate-200 shadow-sm flex items-center gap-1">
            <Compass className="w-3 h-3 text-[#FF6500]" />
            <span>Click map or drag pin to adjust exact doorstep location</span>
          </div>
        </div>

        {/* Selected Location Details & Additional Inputs */}
        <div className="p-4 sm:p-5 space-y-3.5 overflow-y-auto">
          <div className="p-3 bg-slate-50 border border-slate-200 rounded-2xl flex items-start gap-3">
            <div className="w-8 h-8 rounded-xl bg-orange-100 text-[#FF6500] flex items-center justify-center flex-shrink-0 mt-0.5">
              {isGeocoding ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <MapPin className="w-4 h-4" />
              )}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Detected Address ({lat.toFixed(5)}, {lng.toFixed(5)})
              </div>
              <p className="text-xs font-bold text-slate-900 mt-0.5 leading-snug line-clamp-2">
                {addressDetails?.formattedAddress || 'Fetching street details...'}
              </p>
              <div className="text-[11px] text-slate-500 mt-1 flex flex-wrap gap-2">
                <span>City: <strong>{addressDetails?.city || 'Bengaluru'}</strong></span>
                <span>•</span>
                <span>Pincode: <strong>{addressDetails?.postalCode || '560038'}</strong></span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                House / Flat / Floor No. (Optional)
              </label>
              <input
                type="text"
                value={manualHouseNumber}
                onChange={(e) => setManualHouseNumber(e.target.value)}
                placeholder="e.g. Flat 402, Sunshine Towers"
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                Nearby Landmark (Optional)
              </label>
              <input
                type="text"
                value={manualLandmark}
                onChange={(e) => setManualLandmark(e.target.value)}
                placeholder="e.g. Opposite Metro Station"
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs outline-none focus:border-[#2B6CB0]"
              />
            </div>
          </div>

          <div className="flex items-center justify-between pt-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-slate-600">Save as:</span>
              {(['HOME', 'WORK', 'OTHER'] as const).map((type) => (
                <button
                  key={type}
                  type="button"
                  onClick={() => setAddressType(type)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-all ${
                    addressType === type
                      ? 'bg-[#0B192C] text-white shadow-xs'
                      : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                  }`}
                >
                  {type === 'HOME' ? '🏠 Home' : type === 'WORK' ? '🏢 Work' : '📍 Other'}
                </button>
              ))}
            </div>

            <button
              type="button"
              onClick={handleConfirmLocation}
              disabled={isGeocoding}
              className="px-5 py-2.5 bg-[#FF6500] hover:bg-[#FF884B] text-white font-bold text-xs sm:text-sm rounded-xl shadow-md transition-all flex items-center gap-2 flex-shrink-0"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Confirm & Deliver Here</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
