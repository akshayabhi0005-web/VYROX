package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.veltrion.vyrox.data.repository.AuthRepository
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToCoins: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {}
) {
    val currentUser by AuthRepository.currentUser.collectAsState()
    val orders by CommerceRepository.ordersFlow.collectAsState()
    val coins by CommerceRepository.coinBalanceFlow.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FD))) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (currentUser != null) {
                    // Logged in state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(VyroxNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser!!.fullName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser!!.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyroxNavy
                                )
                                Text(
                                    text = currentUser!!.email ?: currentUser!!.mobile ?: "customer@vyrox.com",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Coins Chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToCoins() },
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coins",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF92400E),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // VIP Promo banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B192C)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "VYROX BLACK VIP",
                                color = Color(0xFF00D2FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Unlimited Free 15-Min Delivery & 5% SuperCoins Cashback",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Logged out state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "VYROX Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = VyroxNavy
                        )
                        Text(
                            text = "Log in to track orders, earn 5% SuperCoins & save addresses",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VyroxNavy,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Login / Sign Up", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Account Content
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Core Account Navigation Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountNavTile("My Orders (${orders.size})", Icons.Default.ShoppingBag, Modifier.weight(1f)) {
                        onNavigateToOrders()
                    }
                    AccountNavTile("SuperCoins ($coins)", Icons.Default.MonetizationOn, Modifier.weight(1f)) {
                        onNavigateToCoins()
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountNavTile("Wishlist", Icons.Default.Favorite, Modifier.weight(1f)) {
                        onNavigateToWishlist()
                    }
                    AccountNavTile("Coupons", Icons.Default.LocalOffer, Modifier.weight(1f)) {
                        onNavigateToCoupons()
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountNavTile("Saved Addresses", Icons.Default.LocationOn, Modifier.weight(1f)) {
                        onNavigateToAddress()
                    }
                    AccountNavTile("Help Center", Icons.Default.Headphones, Modifier.weight(1f)) {
                        onNavigateToHelpCenter()
                    }
                }
            }

            // Recent Orders Quick View
            if (orders.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Order",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = VyroxNavy
                        )
                        Text(
                            text = "View All →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyroxOrange,
                            modifier = Modifier.clickable { onNavigateToOrders() }
                        )
                    }
                }

                item {
                    val latestOrder = orders.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order #${latestOrder.orderNumber}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = VyroxNavy
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (latestOrder.status) {
                                                "DELIVERED" -> Color(0xFFECFDF5)
                                                "OUT_FOR_DELIVERY" -> Color(0xFFFFF7ED)
                                                else -> Color(0xFFEFF6FF)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = latestOrder.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (latestOrder.status) {
                                            "DELIVERED" -> Color(0xFF047857)
                                            "OUT_FOR_DELIVERY" -> VyroxOrange
                                            else -> Color(0xFF1D4ED8)
                                        }
                                    )
                                }
                            }

                            latestOrder.items.forEach { itm ->
                                Text(
                                    text = "• ${itm.productTitle.take(45)} (Qty: ${itm.quantity})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    maxLines = 1
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total: ₹${latestOrder.grandTotal.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = VyroxNavy
                                )
                                Button(
                                    onClick = { onNavigateToTracking(latestOrder.orderNumber) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VyroxNavy,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Track on Map →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Finance Options Section
            item {
                Text(
                    text = "Finance & Payment Services",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyroxNavy,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinanceRow("VYROX Personal Loan", "₹10,00,000 | Instant pre-approval")
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        FinanceRow("VYROX Pay Later", "Up to ₹1,00,000 credit line at 0% interest")
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        FinanceRow("VYROX Co-branded Credit Card", "5% Unlimited SuperCoins Cashback")
                    }
                }
            }

            // High-Contrast Clear Logout Button
            if (currentUser != null) {
                item {
                    Button(
                        onClick = { AuthRepository.setGuestMode() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out from VYROX", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountNavTile(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2B6CB0), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun FinanceRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(text = desc, fontSize = 10.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
