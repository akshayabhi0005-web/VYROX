package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veltrion.vyrox.data.model.ProductDetail
import com.veltrion.vyrox.data.repository.AuthRepository
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.components.ProductImage
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onAddToCartSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by AuthRepository.currentUser.collectAsState()
    var product by remember {
        mutableStateOf(
            CommerceRepository.demoProducts.find { it.id == productId }?.let { item ->
                ProductDetail(
                    id = item.id,
                    title = item.title,
                    sku = item.sku,
                    description = "High-performance premium product designed for superior productivity, entertainment, and everyday speed with official manufacturer warranty.",
                    categoryName = item.categoryName,
                    brandName = item.brandName,
                    mrp = item.mrp,
                    sellingPrice = item.sellingPrice,
                    discountPercentage = item.discountPercentage,
                    averageRating = item.averageRating,
                    reviewCount = item.reviewCount,
                    images = listOf(item.mainImageUrl),
                    mainImageUrl = item.mainImageUrl,
                    highlights = listOf(
                        "Genuine Brand Warranty with 7-day replacement policy",
                        "Ultra-fast processing speed and high energy efficiency",
                        "High-resolution TrueTone / OLED dynamic display",
                        "VYROX Verified Seller with 100% genuine product guarantee"
                    ),
                    bankOffers = listOf(
                        "₹5,000 Instant Discount on HDFC/ICICI Bank Credit Cards",
                        "No Cost EMI starting from ₹4,500/month",
                        "Up to ₹22,000 off on Exchange"
                    ),
                    specifications = listOf(
                        com.veltrion.vyrox.data.model.SpecItem("General", "Model Name", item.title.take(30)),
                        com.veltrion.vyrox.data.model.SpecItem("General", "Brand", item.brandName ?: "VYROX"),
                        com.veltrion.vyrox.data.model.SpecItem("Performance", "Processor/Engine", "Next-Gen Ultra Architecture"),
                        com.veltrion.vyrox.data.model.SpecItem("Warranty", "Warranty Summary", "1 Year Manufacturer Comprehensive Warranty")
                    ),
                    sellerName = "VYROX Retail India Pvt Ltd",
                    sellerRating = 4.8,
                    warrantyInfo = "1 Year Manufacturer Comprehensive Warranty",
                    isTopDeal = item.isTopDeal,
                    isQuickCommerceEligible = item.isQuickCommerceEligible,
                    estimatedDeliveryDays = item.estimatedDeliveryDays
                )
            }
        )
    }

    LaunchedEffect(productId) {
        val fetched = CommerceRepository.getProductDetail(productId)
        if (fetched != null) product = fetched
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VyroxOrange)
        }
        return
    }

    val item = product!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.brandName ?: "VYROX", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Wishlist */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                CommerceRepository.addToCart(item.id, 1)
                                onAddToCartSuccess()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroxNavy)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                CommerceRepository.addToCart(item.id, 1)
                                onAddToCartSuccess()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FD))
        ) {
            // Product Hero Image Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProductImage(
                            imageUrl = item.mainImageUrl,
                            categoryName = item.categoryName,
                            title = item.title,
                            brandName = item.brandName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Product Details Block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title and Ratings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF059669),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.averageRating}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "(${item.reviewCount} Ratings & Reviews)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pricing Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${item.sellingPrice.toInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = VyroxNavy
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${item.mrp.toInt()}",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.discountPercentage}% OFF",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }

                    // Bank Offers Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Available Offers",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyroxNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            for (offer in (item.bankOffers ?: emptyList())) {
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = offer,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Highlights Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Product Highlights",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyroxNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            for (hl in (item.highlights ?: emptyList())) {
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", color = VyroxOrange, fontWeight = FontWeight.Black)
                                    Text(
                                        text = hl,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
