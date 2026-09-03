package com.veltrion.vyrox.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.veltrion.vyrox.data.model.LiveTrackingDto
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderNumber: String,
    onBackClick: () -> Unit
) {
    var tracking by remember { mutableStateOf<LiveTrackingDto?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(orderNumber) {
        loading = true
        tracking = CommerceRepository.getLiveTracking(orderNumber)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Order Radar & Map", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        // Refresh tracking
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FD))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroxNavy),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORDER #${tracking?.orderNumber ?: orderNumber}",
                            color = Color(0xFF00D2FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VyroxOrange)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "OPENSTREETMAP LIVE GPS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Arriving in ${tracking?.etaMinutes ?: 3} Mins",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = tracking?.currentStatusDescription ?: "Rider is en route with your package!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Doorstep OTP Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Doorstep Delivery OTP: ",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = tracking?.doorstepOtp ?: "4829",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Real Native OpenStreetMap Tile Viewer using Leaflet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                val lat = tracking?.driverLat ?: 12.9730
                val lng = tracking?.driverLng ?: 77.6010
                val custLat = tracking?.customerLat ?: 12.9716
                val custLng = tracking?.customerLng ?: 77.5946
                val darkLat = tracking?.darkstoreLat ?: 12.9780
                val darkLng = tracking?.darkstoreLng ?: 77.6400

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true

                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                                    <style>
                                        body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #e2e8f0; }
                                    </style>
                                </head>
                                <body>
                                    <div id="map"></div>
                                    <script>
                                        var map = L.map('map', { zoomControl: false }).setView([$lat, $lng], 14);
                                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                            maxZoom: 19,
                                            attribution: '© OpenStreetMap'
                                        }).addTo(map);

                                        L.marker([$darkLat, $darkLng]).addTo(map).bindPopup('🏬 Indiranagar Hub');
                                        L.marker([$lat, $lng]).addTo(map).bindPopup('🛵 Rider: Ramesh').openPopup();
                                        L.marker([$custLat, $custLng]).addTo(map).bindPopup('📍 Your Delivery Address');

                                        var latlngs = [
                                            [$darkLat, $darkLng],
                                            [$lat, $lng],
                                            [$custLat, $custLng]
                                        ];
                                        var polyline = L.polyline(latlngs, {color: '#FF6500', weight: 4, opacity: 0.85, dashArray: '6, 6'}).addTo(map);
                                        map.fitBounds(polyline.getBounds(), { padding: [25, 25] });
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
                        }
                    }
                )
            }

            // Rider Contact Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ElectricScooter, contentDescription = null, tint = VyroxOrange, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tracking?.driverName ?: "Ramesh Kumar (VYROX Rider)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = tracking?.driverVehicle ?: "Ather 450X EV [KA-01-VY-4098]",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { /* Handle call */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECFDF5))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Live Order Stages Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Order Status Timeline", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                    HorizontalDivider()
                    TimelineItem("Order Confirmed", "Payment verified via UPI", true)
                    TimelineItem("Packed at Darkstore", "Indiranagar Hub #04", true)
                    TimelineItem("Out for Delivery", "Ramesh Kumar picked up order", true, isCurrent = true)
                    TimelineItem("Delivered", "Estimated in 3 mins", false)
                }
            }
        }
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, isDone: Boolean, isCurrent: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isDone) Color(0xFF059669) else Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (isCurrent) VyroxOrange else Color(0xFF1E293B)
            )
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
