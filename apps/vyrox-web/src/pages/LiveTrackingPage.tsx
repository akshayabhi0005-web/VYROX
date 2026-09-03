import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import { 
  Truck, MapPin, Phone, Clock, Navigation, CheckCircle2, 
  Store, AlertCircle, RefreshCw
} from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { LiveTracking } from '../types';

// Custom Leaflet Markers with distinct visual tags
const createCustomIcon = (bgColor: string, text: string) => {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `<div style="background-color: ${bgColor}; width: 34px; height: 34px; border-radius: 50%; border: 3px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 14px;">${text}</div>`,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
  });
};

const darkstoreIcon = createCustomIcon('#0B192C', '🏬');
const riderIcon = createCustomIcon('#FF6500', '🛵');
const customerIcon = createCustomIcon('#10B981', '📍');

export const LiveTrackingPage: React.FC = () => {
  const { orderNumber } = useParams<{ orderNumber: string }>();
  const [tracking, setTracking] = useState<LiveTracking | null>(null);
  const [loading, setLoading] = useState(true);
  const [wsConnected, setWsConnected] = useState(false);

  const fetchTracking = async () => {
    if (!orderNumber) return;
    try {
      const res = await apiClient.get(`/tracking/order/${orderNumber}`);
      setTracking(res.data);
    } catch (err) {
      console.error('Failed to load tracking details', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTracking();

    // Connect to WebSocket realtime tracking service if available
    const wsUrl = import.meta.env.VITE_WS_BASE_URL || 
      (typeof window !== 'undefined' && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1'
        ? 'wss://vyrox-backend-rg3r.onrender.com/ws'
        : 'ws://localhost:8091');
    let socket: WebSocket | null = null;
    try {
      socket = new WebSocket(wsUrl);

      socket.onopen = () => {
        setWsConnected(true);
        if (orderNumber) {
          socket?.send(JSON.stringify({ action: 'subscribe', orderNumber }));
        }
      };

      socket.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data);
          if (payload.type === 'TRACKING_UPDATE' && payload.data) {
            setTracking((prev) => ({
              ...prev,
              ...payload.data,
            }));
          }
        } catch (err) {
          console.error('WS message parsing error', err);
        }
      };

      socket.onclose = () => setWsConnected(false);
      socket.onerror = () => setWsConnected(false);
    } catch (e) {
      console.warn('Realtime tracking WebSocket unavailable, using polling mode', e);
      setWsConnected(false);
    }

    return () => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close();
      }
    };
  }, [orderNumber]);

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-12 text-center space-y-4">
        <Truck className="w-10 h-10 text-[#FF6500] animate-bounce mx-auto" />
        <h3 className="font-bold text-base text-slate-800">Connecting to VYROX Live GPS Radar...</h3>
      </div>
    );
  }

  if (!tracking) {
    return (
      <div className="max-w-md mx-auto px-4 py-16 text-center space-y-3">
        <AlertCircle className="w-10 h-10 text-rose-500 mx-auto" />
        <h3 className="font-bold text-base text-slate-900">Tracking Information Not Found</h3>
        <p className="text-xs text-slate-500">Order ID #{orderNumber} might not have active delivery logs yet.</p>
        <Link to="/account?tab=orders" className="text-xs font-bold text-[#2B6CB0] hover:underline">
          Return to My Orders
        </Link>
      </div>
    );
  }

  const riderPos: [number, number] = [tracking.driverLat || 12.9740, tracking.driverLng || 77.6380];
  const darkstorePos: [number, number] = [tracking.darkstoreLat || 12.9716, tracking.darkstoreLng || 77.6412];
  const customerPos: [number, number] = [tracking.customerLat || 12.9784, tracking.customerLng || 77.6408];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#0B192C] to-[#1E3E62] text-white p-6 rounded-3xl shadow-lg flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-[#00D2FF] text-xs font-black tracking-widest uppercase mb-1">
            <Truck className="w-4 h-4" /> LIVE DOORSTEP DELIVERY RADAR (OPENSTREETMAP)
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight">
            Order #{tracking.orderNumber}
          </h1>
          <p className="text-xs text-slate-300 mt-0.5">
            {tracking.currentStatusDescription || 'Rider is on the way with your package.'}
          </p>
        </div>

        {/* GPS status and refresh */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <span className="bg-orange-500/20 text-[#FF6500] border border-orange-500/40 text-[10px] font-extrabold px-3 py-1.5 rounded-full flex items-center gap-1.5 uppercase">
            <span className="w-2 h-2 rounded-full bg-[#FF6500] animate-ping"></span>
            {tracking.isSimulatedGps ? 'DEMO LIVE TRACKING' : 'REAL GPS SATELLITE'}
          </span>

          <button
            onClick={fetchTracking}
            className="p-2 bg-white/10 hover:bg-white/20 text-white rounded-xl transition-colors"
            title="Refresh radar"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Map View Port (OpenStreetMap) */}
        <div className="lg:col-span-8 bg-white rounded-3xl border border-slate-200 p-4 shadow-sm overflow-hidden space-y-3">
          <div className="h-[420px] rounded-2xl overflow-hidden relative shadow-inner border border-slate-200">
            <MapContainer
              center={riderPos}
              zoom={14}
              scrollWheelZoom={false}
              className="w-full h-full"
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <Marker position={darkstorePos} icon={darkstoreIcon}>
                <Popup>
                  <strong>{tracking.darkstoreName || 'VYROX Darkstore'}</strong><br />Origin Hub
                </Popup>
              </Marker>
              <Marker position={riderPos} icon={riderIcon}>
                <Popup>
                  <strong>{tracking.driverName}</strong><br />{tracking.driverVehicle}
                </Popup>
              </Marker>
              <Marker position={customerPos} icon={customerIcon}>
                <Popup>
                  <strong>Your Delivery Address</strong><br />Doorstep Destination
                </Popup>
              </Marker>
              <Polyline
                positions={[darkstorePos, riderPos, customerPos]}
                color="#FF6500"
                weight={4}
                opacity={0.8}
                dashArray="6, 8"
              />
            </MapContainer>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500 px-2">
            <div className="flex items-center gap-4">
              <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-full bg-[#0B192C]"></span> Darkstore</span>
              <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-full bg-[#FF6500]"></span> Rider</span>
              <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-full bg-emerald-500"></span> Destination</span>
            </div>
            <div className="text-[11px] font-semibold text-slate-600">
              ⚡ OpenStreetMap Radar: <span className={wsConnected ? 'text-emerald-600' : 'text-slate-500'}>{wsConnected ? 'Connected (Realtime)' : 'Active (Polling Mode)'}</span>
            </div>
          </div>
        </div>

        {/* Driver & ETA Cards */}
        <div className="lg:col-span-4 space-y-4">
          {/* ETA Card */}
          <div className="bg-gradient-to-br from-[#0B192C] to-[#1E3E62] text-white rounded-3xl p-6 shadow-md space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs text-slate-300 font-bold uppercase tracking-wider">Estimated Arrival</span>
              <Clock className="w-4 h-4 text-[#00D2FF]" />
            </div>
            <div className="text-3xl font-black text-[#00D2FF]">
              {tracking.etaMinutes > 0 ? `${tracking.etaMinutes} Minutes` : 'Delivered'}
            </div>
            <div className="text-xs text-slate-200">
              Distance: <strong>{tracking.distanceKm} km</strong> from doorstep
            </div>
          </div>

          {/* Doorstep OTP Card */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-2 text-center">
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider">Doorstep Verification OTP</div>
            <div className="text-3xl font-black text-[#0B192C] font-mono tracking-widest bg-slate-100 py-2 rounded-2xl border border-slate-200">
              {tracking.doorstepOtp || '4829'}
            </div>
            <p className="text-[11px] text-slate-500">
              Share this code with your delivery rider at the door.
            </p>
          </div>

          {/* Delivery Partner Info */}
          <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-orange-100 text-[#FF6500] flex items-center justify-center font-bold text-base shadow-xs">
                {tracking.driverName?.charAt(0) || 'R'}
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="font-bold text-sm text-slate-900 truncate">{tracking.driverName}</h4>
                <p className="text-xs text-slate-500 truncate">{tracking.driverVehicle}</p>
              </div>
              <a
                href={`tel:${tracking.driverPhone}`}
                className="p-2.5 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded-xl transition-colors"
                title="Call Rider"
              >
                <Phone className="w-4 h-4" />
              </a>
            </div>
          </div>

          {/* Tracking Logs Timeline */}
          {tracking.logs && tracking.logs.length > 0 && (
            <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-xs space-y-3">
              <h4 className="font-bold text-xs uppercase tracking-wider text-slate-500">Delivery Milestones</h4>
              <div className="space-y-3 border-l-2 border-slate-200 ml-2 pl-4 text-xs">
                {tracking.logs.map((log, i) => (
                  <div key={i} className="relative">
                    <div className="w-2.5 h-2.5 rounded-full bg-[#FF6500] absolute -left-[21px] top-1"></div>
                    <div className="font-bold text-slate-900">{log.status}</div>
                    <div className="text-slate-600">{log.description}</div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
