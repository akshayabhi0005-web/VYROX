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
import com.veltrion.vyrox.data.model.OrderItemDto
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    buyNowProductId: Long? = null,
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cart by CommerceRepository.cartFlow.collectAsState()
    val coinBalance by CommerceRepository.coinBalanceFlow.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var useCoins by remember { mutableStateOf(true) }
    var selectedDelivery by remember { mutableStateOf("EXPRESS") }
    var appliedCoupon by remember { mutableStateOf("VYROX500") }
    var confirmedOrderNumber by remember { mutableStateOf<String?>(null) }
    var confirmedDoorstepOtp by remember { mutableStateOf<String?>("4829") }

    // Resolve Checkout Items
    val checkoutItems: List<OrderItemDto> = remember(buyNowProductId, cart) {
        if (buyNowProductId != null && buyNowProductId > 0) {
            val product = CommerceRepository.demoProducts.find { it.id == buyNowProductId } ?: CommerceRepository.demoProducts[0]
            listOf(
                OrderItemDto(
                    id = 1L,
                    productId = product.id,
                    productTitle = product.title,
                    mainImageUrl = product.mainImageUrl,
                    unitPrice = product.sellingPrice,
                    quantity = 1,
                    totalPrice = product.sellingPrice
                )
            )
        } else if (cart.items.isNotEmpty()) {
            cart.items.map { item ->
                OrderItemDto(
                    id = item.itemId,
                    productId = item.productId,
                    productTitle = item.productTitle,
                    mainImageUrl = item.mainImageUrl,
                    unitPrice = item.sellingPrice,
                    quantity = item.quantity,
                    totalPrice = item.sellingPrice * item.quantity
                )
            }
        } else {
            val product = CommerceRepository.demoProducts[0]
            listOf(
                OrderItemDto(
                    id = 1L,
                    productId = product.id,
                    productTitle = product.title,
                    mainImageUrl = product.mainImageUrl,
                    unitPrice = product.sellingPrice,
                    quantity = 1,
                    totalPrice = product.sellingPrice
                )
            )
        }
    }

    // Dynamic Financial Calculations
    val rawSubtotal = checkoutItems.sumOf { it.totalPrice }
    val mrpTotal = checkoutItems.sumOf { item ->
        val origMrp = CommerceRepository.demoProducts.find { it.id == item.productId }?.mrp ?: (item.unitPrice * 1.15)
        origMrp * item.quantity
    }
    val productDiscount = mrpTotal - rawSubtotal
    val couponDiscount = if (appliedCoupon.isNotBlank()) minOf(500.0, rawSubtotal * 0.15) else 0.0
    val maxCoinRedemption = minOf(coinBalance.toDouble(), rawSubtotal * 0.05).toInt().toDouble()
    val coinDiscount = if (useCoins) maxCoinRedemption else 0.0
    val deliveryFee = if (selectedDelivery == "EXPRESS" || rawSubtotal > 500.0) 0.0 else 40.0
    val platformFee = 19.0
    val taxes = (rawSubtotal * 0.18).toInt().toDouble()
    val grandTotal = maxOf(0.0, rawSubtotal - couponDiscount - coinDiscount + deliveryFee + platformFee)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (confirmedOrderNumber != null) "Order Confirmation & Invoice" else "Checkout & Billing", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (confirmedOrderNumber != null) {
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
                        Text("Order ID: #${confirmedOrderNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF047857))
                        Text("Doorstep Verification OTP: ${confirmedDoorstepOtp}", fontSize = 12.sp, color = Color(0xFF047857), fontWeight = FontWeight.SemiBold)
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
                            Text("03-SEP-2026", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("Billed To: Akshay N (Indiranagar, Bengaluru, 560038)", fontSize = 11.sp, color = Color(0xFF334155))
                        Text("Seller: VYROX Retail India Pvt Ltd (GSTIN: 29AABCU9603R1ZM)", fontSize = 11.sp, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        checkoutItems.forEach { item ->
                            InvoiceLine("${item.productTitle.take(35)} (x${item.quantity})", "₹${item.totalPrice.toInt()}")
                        }
                        if (couponDiscount > 0) {
                            InvoiceLine("Coupon Discount ($appliedCoupon)", "-₹${couponDiscount.toInt()}", isGreen = true)
                        }
                        if (useCoins && coinDiscount > 0) {
                            InvoiceLine("VYROX Coins Redeemed", "-₹${coinDiscount.toInt()}", isGreen = true)
                        }
                        InvoiceLine("Delivery Fee ($selectedDelivery)", if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}", isGreen = deliveryFee == 0.0)
                        InvoiceLine("Platform Fee", "₹${platformFee.toInt()}")
                        InvoiceLine("Taxes (18% GST Included)", "₹${taxes.toInt()}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid via $selectedPaymentMethod", fontWeight = FontWeight.Black, fontSize = 14.sp, color = VyroxNavy)
                            Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = VyroxOrange)
                        }
                    }
                }

                Button(
                    onClick = { onOrderPlaced(confirmedOrderNumber!!) },
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

                // Order Items Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order Items (${checkoutItems.sumOf { it.quantity }})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        HorizontalDivider()
                        checkoutItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productTitle, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text("Qty: ${item.quantity} × ₹${item.unitPrice.toInt()}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("₹${item.totalPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                            }
                        }
                    }
                }

                // Delivery Speed Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Delivery Speed", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DeliveryOptionChip(
                                title = "⚡ 15-Min Express",
                                price = "FREE",
                                selected = selectedDelivery == "EXPRESS",
                                onClick = { selectedDelivery = "EXPRESS" },
                                modifier = Modifier.weight(1f)
                            )
                            DeliveryOptionChip(
                                title = "📦 Standard (1-Day)",
                                price = "FREE",
                                selected = selectedDelivery == "STANDARD",
                                onClick = { selectedDelivery = "STANDARD" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Coupons & Coins
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Coupon Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = VyroxOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (appliedCoupon.isNotBlank()) "Coupon: $appliedCoupon" else "Apply Coupon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(if (appliedCoupon.isNotBlank()) "Applied (-₹${couponDiscount.toInt()})" else "Select", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider()

                        // Coin Redemption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Redeem VYROX Coins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Available Balance: $coinBalance Coins", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = useCoins && coinBalance > 0,
                                onCheckedChange = { useCoins = it }
                            )
                        }
                    }
                }

                // Payment Method Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        PaymentOptionRow("UPI / QR (Google Pay, PhonePe, Paytm)", "UPI", selectedPaymentMethod) { selectedPaymentMethod = "UPI" }
                        PaymentOptionRow("Credit / Debit Card (Visa, Master, RuPay)", "CARD", selectedPaymentMethod) { selectedPaymentMethod = "CARD" }
                        PaymentOptionRow("Net Banking (HDFC, ICICI, SBI)", "NET_BANKING", selectedPaymentMethod) { selectedPaymentMethod = "NET_BANKING" }
                        PaymentOptionRow("Cash on Delivery (COD)", "COD", selectedPaymentMethod) { selectedPaymentMethod = "COD" }
                    }
                }

                // Price Breakdown Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyroxNavy)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        PriceRow("Items Total (MRP)", "₹${mrpTotal.toInt()}")
                        PriceRow("Product Discount", "-₹${productDiscount.toInt()}", isGreen = true)
                        if (couponDiscount > 0) PriceRow("Coupon Discount ($appliedCoupon)", "-₹${couponDiscount.toInt()}", isGreen = true)
                        if (useCoins && coinDiscount > 0) PriceRow("Coins Redeemed", "-₹${coinDiscount.toInt()}", isGreen = true)
                        PriceRow("Delivery Fee", if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}", isGreen = deliveryFee == 0.0)
                        PriceRow("Platform Fee", "₹${platformFee.toInt()}")
                        PriceRow("Estimated Taxes (18% GST)", "₹${taxes.toInt()}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", fontWeight = FontWeight.Black, fontSize = 14.sp, color = VyroxNavy)
                            Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = VyroxOrange)
                        }
                    }
                }

                // Place Order Button
                Button(
                    onClick = {
                        val newOrder = CommerceRepository.createOrder(
                            items = checkoutItems,
                            subtotal = rawSubtotal,
                            grandTotal = grandTotal,
                            paymentMethod = selectedPaymentMethod,
                            coinsUsed = if (useCoins) coinDiscount.toInt() else 0
                        )
                        if (buyNowProductId == null || buyNowProductId <= 0) {
                            CommerceRepository.clearCart()
                        }
                        confirmedOrderNumber = newOrder.orderNumber
                        confirmedDoorstepOtp = newOrder.doorstepOtp
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Place Order & Pay ₹${grandTotal.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun InvoiceLine(label: String, amount: String, isGreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = if (isGreen) Color(0xFF047857) else Color(0xFF334155), fontWeight = if (isGreen) FontWeight.SemiBold else FontWeight.Normal)
        Text(amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGreen) Color(0xFF047857) else Color(0xFF1E293B))
    }
}

@Composable
fun PriceRow(label: String, amount: String, isGreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = if (isGreen) Color(0xFF047857) else Color.Gray)
        Text(amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGreen) Color(0xFF047857) else Color(0xFF1E293B))
    }
}

@Composable
fun DeliveryOptionChip(title: String, price: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFFFF7ED) else Color(0xFFF8F9FD))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Text(title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold, color = if (selected) VyroxOrange else Color(0xFF1E293B))
            Text(price, fontSize = 10.sp, color = if (selected) Color(0xFF047857) else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentOptionRow(title: String, method: String, selectedMethod: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedMethod == method,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = VyroxOrange)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal, color = Color(0xFF1E293B))
    }
}
