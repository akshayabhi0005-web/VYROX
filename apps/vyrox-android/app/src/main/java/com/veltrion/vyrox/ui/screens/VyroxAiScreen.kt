package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val time: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyroxAiScreen(
    onBackClick: () -> Unit,
    onProductClick: (Long) -> Unit
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("ai", "Hello! I am VYROX AI, your intelligent shopping assistant. How can I help you discover deals, compare gadgets, or track your orders today?")
            )
        )
    }

    var inputText by remember { mutableStateOf("") }

    val promptSuggestions = listOf(
        "Best laptop under 70000",
        "Best phone",
        "Compare iPhone and Samsung",
        "Top 15-Min grocery deals"
    )

    fun sendUserMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage("user", query)
        val aiReply = when {
            query.contains("laptop", ignoreCase = true) ->
                "For laptops under ₹70,000, consider our top recommendations:\n1. Dell XPS 16 (Performance King with RTX graphics)\n2. Apple MacBook Pro 16\" (Best battery life & M3 Pro chip)\nWould you like me to add one to your cart?"
            query.contains("phone", ignoreCase = true) || query.contains("samsung", ignoreCase = true) || query.contains("iphone", ignoreCase = true) ->
                "Top flagship smartphones compared:\n• Samsung Galaxy S24 Ultra 5G: Titanium build, Galaxy AI, 200MP Quad Camera (₹1,19,999)\n• Apple iPhone 15 Pro Max: A17 Pro 3nm chip, 5x Telephoto Zoom (₹1,48,900)\nBoth qualify for Free 2-Day Delivery."
            query.contains("compare", ignoreCase = true) ->
                "Specification Comparison Matrix:\n- Display: S24 Ultra (6.8\" AMOLED 120Hz) vs iPhone 15 Pro Max (6.7\" OLED)\n- Battery: 5000 mAh vs 4422 mAh\n- Value: S24 Ultra offers 11% instant discount today."
            query.contains("grocery", ignoreCase = true) || query.contains("quick", ignoreCase = true) ->
                "⚡ VYROX Minutes delivers instant dark store items in 10-15 mins! Try our Organic Valencia Orange Juice (₹179) and Roasted Almonds Trail Mix (₹399)."
            else ->
                "I found matching items across VYROX Smart Commerce. You can check our Top Deals section for up to 33% off on verified genuine products."
        }
        messages = messages + userMsg + ChatMessage("ai", aiReply)
        inputText = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VyroxOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("VYROX AI Assistant", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text("Deterministic Demo AI Engine Active", fontSize = 10.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            // Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                promptSuggestions.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF7ED))
                            .clickable { sendUserMessage(prompt) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "✨ $prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyroxOrange)
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isAi = msg.sender == "ai"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            modifier = Modifier.widthIn(max = 300.dp),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isAi) 2.dp else 16.dp,
                                bottomEnd = if (isAi) 16.dp else 2.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAi) Color.White else VyroxNavy
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 12.sp,
                                    color = if (isAi) Color(0xFF1E293B) else Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask VYROX AI anything...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendUserMessage(inputText) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(VyroxOrange)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
