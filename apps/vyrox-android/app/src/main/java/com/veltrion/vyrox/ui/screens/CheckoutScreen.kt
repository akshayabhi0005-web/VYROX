package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var useCoins by remember { mutableStateOf(true) }
    var selectedDelivery by remember { mutableStateOf("EXPRESS") }
    var orderConfirmed by remember { mutableStateOf(false) }

    val rawSubtotal = 219900.0
    val mrpTotal = 249900.0
    val productDiscount = mrpTotal - rawSubtotal
    val couponDiscount = 500.0
    val coinDiscount = if (useCoins) 350.0 else 0.0
    val deliveryFee = if (selectedDelivery == "EXPRESS") 0.0 else 40.0
    val platformFee = 19.0
    val taxes = (rawSubtotal * 0.18).toInt().toDouble() // GST component
    val grandTotal = rawSubtotal - couponDiscount - coinDiscount + deliveryFee + platformFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orderConfirmed) "Order Confirmation & Invoice" else "Checkout & Billing", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (orderConfirmed) {
            // Invoice & Order Confirmation View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FD))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Order Placed Successfully!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF065F46))
                        Text("Order ID: #VYR-2026-90412", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF047857))
                        Text("Doorstep Verification OTP: 4829", fontSize = 12.sp, color = Color(0xFF047857), fontWeight = FontWeight.SemiBold)
                    }
                }

                // Official Invoice Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VYROX TAX INVOICE", fontWeight = FontWeight.Black, fontSize = 14.sp, color = VyroxNavy)
                            Text("02-SEP-2026", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("Billed To: Akshay N (Indiranagar, Bengaluru, 560038)", fontSize = 11.sp, color = Color(0xFF334155))
                        Text("Seller: VYROX Retail India Pvt Ltd (GSTIN: 29AABCU9603R1ZM)", fontSize = 11.sp, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        InvoiceLine("Apple MacBook Pro 16\" (M3 Pro)", "₹2,19,900")
                        InvoiceLine("Coupon Discount (VYROX500)", "-₹500", isGreen = true)
                        if (useCoins) InvoiceLine("VYROX Coins Redeemed", "-₹350", isGreen = true)
                        InvoiceLine("Delivery Fee (Express 15-Min)", "FREE", isGreen = true)
                        InvoiceLine("Platform Fee", "₹19")
                        InvoiceLine("Taxes (18% GST Included)", "₹39,582")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid via $selectedPaymentMethod (DEMO)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = VyroxNavy)
                            Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = VyroxOrange)
                        }
                    }
                }

                Button(
                    onClick = { onOrderPlaced("VYR-2026-90412") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroxNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Live Tracking Radar →", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Checkout Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FD))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Delivery Address Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = VyroxOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delivery Address (Home)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        }
                        Text("Akshay N | +91 98765 43210", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 4.dp))
                        Text("Flat 402, Skyline Residency, 100 Feet Road, Indiranagar, Bengaluru - 560038", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // Delivery Speed Options
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Delivery Speed", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedDelivery == "EXPRESS",
                                onClick = { selectedDelivery = "EXPRESS" },
                                label = { Text("⚡ 15-Min Express (FREE)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedDelivery == "STANDARD",
                                onClick = { selectedDelivery = "STANDARD" },
                                label = { Text("Standard Delivery", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Coins Toggle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Redeem 350 VYROX Coins", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                Text("Save extra ₹350 on this order", fontSize = 10.sp, color = Color(0xFFB45309))
                            }
                        }
                        Switch(checked = useCoins, onCheckedChange = { useCoins = it })
                    }
                }

                // Payment Method Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Payment Method (DEMO SANDBOX)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        listOf(
                            "UPI" to "Instant UPI (Google Pay, PhonePe, Paytm)",
                            "CARD" to "Credit / Debit Card",
                            "NET_BANKING" to "Net Banking (All Major Banks)",
                            "COD" to "Cash on Delivery"
                        ).forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = key }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedPaymentMethod == key, onClick = { selectedPaymentMethod = key })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }

                // Dynamic Billing Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Price Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        InvoiceLine("Total MRP", "₹${mrpTotal.toInt()}")
                        InvoiceLine("Product Discount", "-₹${productDiscount.toInt()}", isGreen = true)
                        InvoiceLine("Coupon Discount", "-₹500", isGreen = true)
                        if (useCoins) InvoiceLine("Coins Redeemed", "-₹350", isGreen = true)
                        InvoiceLine("Delivery Charges", if (deliveryFee == 0.0) "FREE" else "₹40", isGreen = (deliveryFee == 0.0))
                        InvoiceLine("Platform Fee", "₹19")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", fontWeight = FontWeight.Black, fontSize = 14.sp, color = VyroxNavy)
                            Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = VyroxOrange)
                        }
                    }
                }

                Button(
                    onClick = { orderConfirmed = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay ₹${grandTotal.toInt()} (Demo Checkout)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun InvoiceLine(title: String, value: String, isGreen: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 11.sp, color = Color(0xFF475569))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isGreen) Color(0xFF059669) else Color(0xFF1E293B))
    }
}
