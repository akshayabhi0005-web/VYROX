package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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

data class CouponModel(
    val code: String,
    val title: String,
    val description: String,
    val minOrder: String,
    val expiry: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsScreen(
    onBackClick: () -> Unit,
    onApplyCoupon: (String) -> Unit
) {
    val coupons = listOf(
        CouponModel("VYROX500", "Flat ₹500 OFF", "Applicable on Electronics & Laptops above ₹50,000", "Min Order: ₹50,000", "Expires in 3 days"),
        CouponModel("SUPER10", "10% Instant Cashback", "Maximum discount up to ₹2,000 on Mobile purchases", "Min Order: ₹15,000", "Expires in 7 days"),
        CouponModel("BLACKVIP", "Free 15-Min Delivery", "Unlimited free delivery on all Quick Commerce & Grocery orders", "No Min Order", "Active Member Benefit"),
        CouponModel("WELCOME100", "Flat ₹100 OFF", "Special welcome coupon for your next purchase", "Min Order: ₹999", "Expires in 30 days")
    )

    var appliedCode by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Coupons", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            items(coupons) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF7ED))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = coupon.code,
                                    color = VyroxOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Button(
                                onClick = {
                                    appliedCode = coupon.code
                                    onApplyCoupon(coupon.code)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (appliedCode == coupon.code) Color(0xFF059669) else VyroxNavy
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (appliedCode == coupon.code) "APPLIED ✓" else "APPLY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = coupon.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(text = coupon.description, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = coupon.minOrder, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                            Text(text = coupon.expiry, fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
