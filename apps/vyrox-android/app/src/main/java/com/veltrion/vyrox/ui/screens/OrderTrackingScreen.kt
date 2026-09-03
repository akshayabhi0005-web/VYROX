package com.veltrion.vyrox.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import kotlinx.coroutines.launch
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
import com.veltrion.vyrox.ui.components.OsmMapView
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
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(orderNumber) {
        loading = true
        tracking = CommerceRepository.getLiveTracking(orderNumber)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Order Radar & Map", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyroxNavy) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VyroxNavy)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            loading = true
                            tracking = CommerceRepository.getLiveTracking(orderNumber)
                            webViewRef?.reload()
                            loading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = VyroxNavy)
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

            // High-Performance Native OpenStreetMap Live Radar & Map
            OsmMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                riderLat = tracking?.driverLat ?: 12.9740,
                riderLng = tracking?.driverLng ?: 77.6380,
                destLat = tracking?.customerLat ?: 12.9784,
                destLng = tracking?.customerLng ?: 77.6408,
                darkstoreLat = tracking?.darkstoreLat ?: 12.9716,
                darkstoreLng = tracking?.darkstoreLng ?: 77.6412
            )

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
                            text = tracking?.driverName ?: "Ramesh Kumar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = VyroxNavy
                        )
                        Text(
                            text = "VYROX EV Express Partner (4.9 ★)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { /* Call Driver Intent */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECFDF5))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF047857), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Order Status Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Delivery Milestones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = VyroxNavy
                    )
                    TrackingStep("Order Confirmed & Payment Verified", "09:40 AM", true)
                    TrackingStep("Packed at Indiranagar Darkstore #101", "09:42 AM", true)
                    TrackingStep("Rider Picked Up & On the Way", "09:44 AM", true)
                    TrackingStep("Arriving at Your Doorstep", "ETA 09:47 AM", false)
                }
            }
        }
    }
}

@Composable
fun TrackingStep(title: String, time: String, completed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (completed) Color(0xFF10B981) else Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (completed) FontWeight.Bold else FontWeight.Medium,
                color = if (completed) VyroxNavy else Color.Gray
            )
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
