package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
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
fun OrdersScreen(
    onBackClick: () -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val orders by CommerceRepository.ordersFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Orders (${orders.size})",
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
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8F9FD)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No orders placed yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VyroxNavy
                    )
                    Text(
                        text = "Your placed orders and live delivery tracking will appear here.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8F9FD))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Order #${order.orderNumber}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = VyroxNavy
                                    )
                                    Text(
                                        text = if (order.quickCommerce) "⚡ 15-Min Quick Delivery" else "Standard Fast Delivery",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (order.status) {
                                                "DELIVERED" -> Color(0xFFECFDF5)
                                                "OUT_FOR_DELIVERY" -> Color(0xFFFFF7ED)
                                                else -> Color(0xFFEFF6FF)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (order.status) {
                                            "DELIVERED" -> Color(0xFF047857)
                                            "OUT_FOR_DELIVERY" -> VyroxOrange
                                            else -> Color(0xFF1D4ED8)
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            order.items.forEach { itm ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${itm.productTitle.take(38)} (x${itm.quantity})",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "₹${itm.totalPrice.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VyroxNavy
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Grand Total: ₹${order.grandTotal.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = VyroxNavy
                                    )
                                    if (order.doorstepOtp != null) {
                                        Text(
                                            text = "Doorstep Delivery OTP: ${order.doorstepOtp}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyroxOrange
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onNavigateToTracking(order.orderNumber) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VyroxNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Track on Map →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
