package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {}
) {
    val currentUser by AuthRepository.currentUser.collectAsState()

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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(VyroxNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser!!.fullName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
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
                                    text = currentUser!!.email ?: currentUser!!.mobile ?: "",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Coins Chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentUser!!.coinBalance ?: 350}",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Unlimited Free 15-Min Delivery & 5% Cashback",
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = VyroxNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Login / Sign Up", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Account Quick Nav Grid (Orders, Wishlist, Coupons, Help Center)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountNavTile("Orders", Icons.Default.Inventory2, Modifier.weight(1f)) {
                        onNavigateToTracking("VYR-2026-90412")
                    }
                    AccountNavTile("Wishlist", Icons.Default.Favorite, Modifier.weight(1f)) {
                        onNavigateToWishlist()
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountNavTile("Coupons", Icons.Default.LocalOffer, Modifier.weight(1f)) {
                        onNavigateToCoupons()
                    }
                    AccountNavTile("Help Center", Icons.Default.Headphones, Modifier.weight(1f)) {
                        onNavigateToHelpCenter()
                    }
                }
            }

            item {
                AccountNavTile("Saved Addresses & Location", Icons.Default.LocationOn, Modifier.fillMaxWidth()) {
                    onNavigateToAddress()
                }
            }

            // Finance Options Section
            item {
                Text(
                    text = "Finance & Payment Options",
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
                        FinanceRow("VYROX Personal Loan", "₹10,00,000 | Instant approval")
                        HorizontalDivider()
                        FinanceRow("VYROX Pay Later", "Up to ₹1,00,000 credit line")
                        HorizontalDivider()
                        FinanceRow("VYROX Co-branded Credit Card", "5% Unlimited Cashback")
                    }
                }
            }

            if (currentUser != null) {
                item {
                    Button(
                        onClick = { AuthRepository.setGuestMode() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Logout", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
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
