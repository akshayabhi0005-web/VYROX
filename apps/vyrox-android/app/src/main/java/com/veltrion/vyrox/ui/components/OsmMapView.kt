package com.veltrion.vyrox.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange
import kotlin.math.*

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    riderLat: Double = 12.9740,
    riderLng: Double = 77.6380,
    destLat: Double = 12.9784,
    destLng: Double = 77.6408,
    darkstoreLat: Double = 12.9716,
    darkstoreLng: Double = 77.6412
) {
    val context = LocalContext.current
    var zoom by remember { mutableIntStateOf(15) }
    var centerLat by remember { mutableDoubleStateOf((riderLat + destLat) / 2.0) }
    var centerLng by remember { mutableDoubleStateOf((riderLng + destLng) / 2.0) }

    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for rider marker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Helper math to project Lat/Lng to World Pixel coordinates
    fun latLngToWorldPixel(lat: Double, lng: Double, z: Int): Pair<Double, Double> {
        val n = 2.0.pow(z.toDouble())
        val x = (lng + 180.0) / 360.0 * n * 256.0
        val latRad = Math.toRadians(lat)
        val y = (1.0 - asinh(tan(latRad)) / Math.PI) / 2.0 * n * 256.0
        return Pair(x, y)
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE2E8F0))
            .pointerInput(zoom) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffsetX += dragAmount.x
                    panOffsetY += dragAmount.y
                }
            }
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        val centerWorld = latLngToWorldPixel(centerLat, centerLng, zoom)
        val adjustedCenterWorldX = centerWorld.first - panOffsetX
        val adjustedCenterWorldY = centerWorld.second - panOffsetY

        val centerTileX = (adjustedCenterWorldX / 256.0).toInt()
        val centerTileY = (adjustedCenterWorldY / 256.0).toInt()

        val n = 2.0.pow(zoom.toDouble()).toInt()

        // 1. Render 3x3 OpenStreetMap Tile Grid
        for (dx in -2..2) {
            for (dy in -2..2) {
                val tileX = (centerTileX + dx).mod(n)
                val tileY = centerTileY + dy
                if (tileY in 0 until n) {
                    val tileWorldX = (centerTileX + dx) * 256.0
                    val tileWorldY = (centerTileY + dy) * 256.0

                    val screenX = (tileWorldX - adjustedCenterWorldX + (containerWidthPx / 2.0)).toFloat()
                    val screenY = (tileWorldY - adjustedCenterWorldY + (containerHeightPx / 2.0)).toFloat()

                    val tileUrl = "https://tile.openstreetmap.org/$zoom/$tileX/$tileY.png"

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(tileUrl)
                            .setHeader("User-Agent", "VyroxApp/1.0 (com.veltrion.vyrox)")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Map Tile",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(256.dp)
                            .offset { IntOffset(screenX.roundToInt(), screenY.roundToInt()) }
                    )
                }
            }
        }

        // Helper to convert LatLng to Container Screen Pixels
        fun projectToScreen(lat: Double, lng: Double): Offset {
            val p = latLngToWorldPixel(lat, lng, zoom)
            val sx = (p.first - adjustedCenterWorldX + (containerWidthPx / 2.0)).toFloat()
            val sy = (p.second - adjustedCenterWorldY + (containerHeightPx / 2.0)).toFloat()
            return Offset(sx, sy)
        }

        val darkstorePos = projectToScreen(darkstoreLat, darkstoreLng)
        val riderPos = projectToScreen(riderLat, riderLng)
        val destPos = projectToScreen(destLat, destLng)

        // 2. Draw Polyline Route on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            // Darkstore to Rider
            drawLine(
                color = Color(0xFF94A3B8),
                start = darkstorePos,
                end = riderPos,
                strokeWidth = 6f,
                cap = StrokeCap.Round,
                pathEffect = pathEffect
            )
            // Rider to Destination (Active Orange Route)
            drawLine(
                color = Color(0xFFFF6500),
                start = riderPos,
                end = destPos,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }

        // 3. Render High-Contrast Native Markers

        // Darkstore Hub Marker 🏬
        Box(
            modifier = Modifier
                .offset { IntOffset((darkstorePos.x - 20).roundToInt(), (darkstorePos.y - 20).roundToInt()) }
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .background(VyroxNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🏬", fontSize = 18.sp)
        }

        // Customer Destination Marker 📍
        Box(
            modifier = Modifier
                .offset { IntOffset((destPos.x - 20).roundToInt(), (destPos.y - 20).roundToInt()) }
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .background(Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📍", fontSize = 18.sp)
        }

        // Active Rider Courier Marker with Pulse Effect 🛵
        Box(
            modifier = Modifier
                .offset { IntOffset((riderPos.x - 30).roundToInt(), (riderPos.y - 30).roundToInt()) }
                .size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Ripple
            Box(
                modifier = Modifier
                    .size((44 * pulseScale).dp)
                    .background(VyroxOrange.copy(alpha = pulseAlpha), CircleShape)
            )
            // Main Rider Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape)
                    .background(VyroxOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛵", fontSize = 20.sp)
            }
        }

        // 4. Interactive Map Controls (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FloatingActionButton(
                onClick = { if (zoom < 18) zoom += 1 },
                modifier = Modifier.size(36.dp),
                containerColor = Color.White,
                contentColor = VyroxNavy,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }
            FloatingActionButton(
                onClick = { if (zoom > 12) zoom -= 1 },
                modifier = Modifier.size(36.dp),
                containerColor = Color.White,
                contentColor = VyroxNavy,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }
            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    centerLat = (riderLat + destLat) / 2.0
                    centerLng = (riderLng + destLng) / 2.0
                },
                modifier = Modifier.size(36.dp),
                containerColor = Color.White,
                contentColor = VyroxOrange,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", modifier = Modifier.size(18.dp))
            }
        }

        // 5. OpenStreetMap Required Legal Attribution
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color.White.copy(alpha = 0.85f)
        ) {
            Text(
                text = "© OpenStreetMap contributors",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
