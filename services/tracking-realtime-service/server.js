const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 8091;

app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Order tracking state cache: orderNumber -> { driverLat, driverLng, customerLat, customerLng, etaMinutes, status, ... }
const trackingSessions = new Map();

// Helper to calculate distance
function getDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// REST endpoints
app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'vyrox-tracking-realtime-service', port: PORT, timestamp: new Date() });
});

app.get('/api/v1/tracking/live/:orderNumber', (req, res) => {
  const { orderNumber } = req.params;
  const session = trackingSessions.get(orderNumber) || initSession(orderNumber);
  res.json(session);
});

app.post('/api/v1/tracking/gps-update', (req, res) => {
  const { orderNumber, latitude, longitude, speed, heading, isRealGps } = req.body;
  if (!orderNumber || latitude === undefined || longitude === undefined) {
    return res.status(400).json({ error: 'orderNumber, latitude, longitude are required' });
  }

  let session = trackingSessions.get(orderNumber) || initSession(orderNumber);
  session.driverLat = latitude;
  session.driverLng = longitude;
  session.speed = speed || 24;
  session.heading = heading || 45;
  session.isRealGps = !!isRealGps;
  session.lastUpdated = new Date();

  // Recalculate distance & ETA
  session.distanceKm = Math.round(getDistanceKm(session.driverLat, session.driverLng, session.customerLat, session.customerLng) * 10) / 10;
  session.etaMinutes = Math.max(1, Math.round((session.distanceKm / (session.speed || 24)) * 60));

  trackingSessions.set(orderNumber, session);
  broadcastToOrderSubscribers(orderNumber, session);

  res.json({ success: true, session });
});

function initSession(orderNumber) {
  const session = {
    orderNumber,
    status: 'OUT_FOR_DELIVERY',
    darkstore: { name: 'VYROX Darkstore #101', lat: 12.9716, lng: 77.6412 },
    customerLat: 12.9784,
    customerLng: 77.6408,
    driverLat: 12.9735,
    driverLng: 77.6390,
    driverName: 'Ramesh Kumar (VYROX Express Rider)',
    driverPhone: '+91 98765 43212',
    driverVehicle: 'Ather 450X EV [KA-01-VY-4098]',
    doorstepOtp: '4829',
    distanceKm: 0.8,
    etaMinutes: 4,
    speed: 24,
    heading: 32,
    isRealGps: false,
    mode: 'SIMULATED_LIVE_GPS',
    lastUpdated: new Date()
  };
  trackingSessions.set(orderNumber, session);
  return session;
}

// WebSocket connections
const orderClients = new Map(); // orderNumber -> Set(ws)

wss.on('connection', (ws, req) => {
  let subscribedOrder = null;

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      if (data.action === 'subscribe' && data.orderNumber) {
        subscribedOrder = data.orderNumber;
        if (!orderClients.has(subscribedOrder)) {
          orderClients.set(subscribedOrder, new Set());
        }
        orderClients.get(subscribedOrder).add(ws);

        // Send immediate current state
        const session = trackingSessions.get(subscribedOrder) || initSession(subscribedOrder);
        ws.send(JSON.stringify({ type: 'TRACKING_UPDATE', data: session }));
      }
    } catch (e) {
      console.error('WS parse error:', e);
    }
  });

  ws.on('close', () => {
    if (subscribedOrder && orderClients.has(subscribedOrder)) {
      orderClients.get(subscribedOrder).delete(ws);
    }
  });
});

function broadcastToOrderSubscribers(orderNumber, data) {
  const clients = orderClients.get(orderNumber);
  if (clients) {
    const payload = JSON.stringify({ type: 'TRACKING_UPDATE', data });
    clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(payload);
      }
    });
  }
}

// Background simulation loop: gently progresses rider along route towards customer
setInterval(() => {
  for (const [orderNumber, session] of trackingSessions.entries()) {
    if (session.status === 'OUT_FOR_DELIVERY' && !session.isRealGps) {
      // Step 2% closer to customer
      const step = 0.02;
      const dLat = session.customerLat - session.driverLat;
      const dLng = session.customerLng - session.driverLng;

      if (Math.abs(dLat) > 0.0001 || Math.abs(dLng) > 0.0001) {
        session.driverLat += dLat * step;
        session.driverLng += dLng * step;
        session.distanceKm = Math.round(getDistanceKm(session.driverLat, session.driverLng, session.customerLat, session.customerLng) * 10) / 10;
        session.etaMinutes = Math.max(1, Math.round((session.distanceKm / (session.speed || 24)) * 60));
        session.lastUpdated = new Date();
        broadcastToOrderSubscribers(orderNumber, session);
      }
    }
  }
}, 3000);

server.listen(PORT, () => {
  console.log(`[VYROX] Realtime Tracking & WebSocket Service running on http://localhost:${PORT}`);
});
