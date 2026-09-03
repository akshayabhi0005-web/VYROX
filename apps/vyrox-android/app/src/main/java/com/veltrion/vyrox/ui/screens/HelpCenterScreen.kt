package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onBackClick: () -> Unit,
    onContactSupport: () -> Unit
) {
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    var supportTicketCreated by remember { mutableStateOf(false) }

    val faqs = listOf(
        "How do I track my order in real-time?" to "Go to Account > Orders or click on the Live Radar on your active delivery card. You can view rider GPS position and your doorstep OTP.",
        "What is VYROX 15-Minute Quick Commerce?" to "VYROX Minutes delivers groceries, snacks, and essentials directly from local dark stores within 10-15 minutes.",
        "How do I earn and redeem VYROX Coins?" to "You earn 5% Coins on every purchase. Apply them at checkout for instant cash discounts.",
        "What is the return and replacement policy?" to "Most electronics and devices enjoy a 7-day hassle-free doorstep replacement or refund.",
        "Which payment methods are supported?" to "UPI (Google Pay, PhonePe, Paytm), Credit/Debit Cards, Net Banking, and Cash on Delivery."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VYROX Help & Support", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FD))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VyroxNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("24/7 Dedicated Support", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("Instant assistance with orders, returns, and payments", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { supportTicketCreated = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (supportTicketCreated) "Ticket #VYR-SUP-891 Opened ✓" else "Chat with Support Specialist", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text("Frequently Asked Questions", fontSize = 15.sp, fontWeight = FontWeight.Black, color = VyroxNavy)
            }

            items(faqs.size) { index ->
                val (question, answer) = faqs[index]
                val isExpanded = expandedFaqIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaqIndex = if (isExpanded) null else index },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = question, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = answer, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
