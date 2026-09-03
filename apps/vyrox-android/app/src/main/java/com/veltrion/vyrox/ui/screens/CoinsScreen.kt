package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinsScreen(
    onBackClick: () -> Unit
) {
    val coins by CommerceRepository.coinBalanceFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VYROX SuperCoins",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = VyroxNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VyroxNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FD))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VyroxNavy),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AVAILABLE COIN BALANCE",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$coins Coins",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "1 Coin = ₹1 Discount on all orders & electronics",
                            color = Color(0xFF00D2FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Benefits Grid
            item {
                Text(
                    text = "How to Earn & Redeem",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = VyroxNavy,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CoinBenefitRow("⚡ 5% Cashback", "Earn 5% worth of coins automatically on every completed order.")
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        CoinBenefitRow("🎁 100 Welcome Coins", "Credited immediately upon account registration.")
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        CoinBenefitRow("🏷️ Instant Checkout Discounts", "Apply up to 5% of order subtotal directly at payment step.")
                    }
                }
            }
        }
    }
}

@Composable
fun CoinBenefitRow(title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(text = desc, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
