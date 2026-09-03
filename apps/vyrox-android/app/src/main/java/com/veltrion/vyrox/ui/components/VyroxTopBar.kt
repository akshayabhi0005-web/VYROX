package com.veltrion.vyrox.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@Composable
fun VyroxTopBar(
    coinBalance: Int = 100,
    locationText: String = "Indiranagar, Bengaluru",
    onLocationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onVoiceSearchResult: (String) -> Unit = {},
    onImageSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showImageSearchDialog by remember { mutableStateOf(false) }
    var detectedVoiceQuery by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            detectedVoiceQuery = spokenText
            showVoiceDialog = true
            onVoiceSearchResult(spokenText)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search on VYROX (e.g., 'MacBook', 'iPhone', 'Shoes')")
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                showVoiceDialog = true
            }
        } else {
            showVoiceDialog = true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Image selected: Scanning catalog for similar items...", Toast.LENGTH_SHORT).show()
            onImageSearchClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VyroxNavy)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Logo, Location, and Coins Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VYROX",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "SHOP SMART. LIVE BETTER.",
                    color = VyroxOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 1.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Location badge (Clickable)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onLocationClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = VyroxOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = locationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Coins chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$coinBalance",
                        color = Color(0xFFFDE68A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar with Voice & Camera Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onSearchClick() }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search products, brands and more",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Search",
                tint = VyroxOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (hasMic) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to search on VYROX")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                showVoiceDialog = true
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Image Search",
                tint = VyroxNavy,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        showImageSearchDialog = true
                    }
            )
        }
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("VYROX Voice Search", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        if (detectedVoiceQuery.isNotBlank())
                            "Detected Query: \"$detectedVoiceQuery\""
                        else
                            "Voice recognition activated. Try speaking or choose a quick search term below:"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf("MacBook Pro M3", "Samsung S24 Ultra", "Nike Air Jordan", "Quick Groceries").forEach { query ->
                        TextButton(
                            onClick = {
                                showVoiceDialog = false
                                onVoiceSearchResult(query)
                            }
                        ) {
                            Text("🔍 \"$query\"", fontWeight = FontWeight.Bold, color = VyroxNavy)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showImageSearchDialog) {
        AlertDialog(
            onDismissRequest = { showImageSearchDialog = false },
            title = { Text("VYROX Visual Image Search", fontWeight = FontWeight.Bold) },
            text = {
                Text("Search products by photo. Choose an image from your device gallery or capture with camera:")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImageSearchDialog = false
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange)
                ) {
                    Text("Choose Photo / Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
