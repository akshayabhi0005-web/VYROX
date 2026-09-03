package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.veltrion.vyrox.data.model.ProductSummary
import com.veltrion.vyrox.data.repository.CommerceRepository
import com.veltrion.vyrox.ui.components.ProductCardItem
import com.veltrion.vyrox.ui.components.VyroxTopBar
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

@Composable
fun HomeScreen(
    onProductClick: (Long) -> Unit,
    onNavigateToDeals: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    var topDeals by remember { mutableStateOf(CommerceRepository.demoProducts.filter { it.isTopDeal }) }
    var trending by remember { mutableStateOf(CommerceRepository.demoProducts.filter { it.isTrending }) }
    var quickCommerce by remember { mutableStateOf(CommerceRepository.demoProducts.filter { it.isQuickCommerceEligible }) }

    LaunchedEffect(Unit) {
        val deals = CommerceRepository.getTopDeals()
        if (deals.isNotEmpty()) topDeals = deals
        val tr = CommerceRepository.getTrending()
        if (tr.isNotEmpty()) trending = tr
        val qc = CommerceRepository.getQuickCommerce()
        if (qc.isNotEmpty()) quickCommerce = qc
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FD))) {
            VyroxTopBar(
                onLocationClick = onNavigateToLocation,
                onSearchClick = onNavigateToDeals,
                onVoiceSearchResult = { onNavigateToDeals() },
                onImageSearchClick = onNavigateToDeals
            )

            // Categories Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("For You", "Mobiles", "Electronics", "Fashion", "Home", "Appliances", "15-Min Quick").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (cat == "For You") VyroxNavy else Color(0xFFF1F5F9))
                            .clickable { onNavigateToDeals() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (cat == "For You") Color.White else Color(0xFF1E293B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Content Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Commerce Banner (Full span)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToDeals() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚡ 15-MIN QUICK COMMERCE",
                                        color = VyroxOrange,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Instant Darkstore Groceries & Snacks",
                                    color = Color(0xFF1E293B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Top Deals Section Title
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Deals For You",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = VyroxNavy
                        )
                        Text(
                            text = "View All →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyroxOrange,
                            modifier = Modifier.clickable { onNavigateToDeals() }
                        )
                    }
                }

                items(topDeals) { product ->
                    ProductCardItem(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }

                // Quick Commerce Snacks & Essentials
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Text(
                        text = "⚡ 15-Min Quick Essentials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = VyroxOrange,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                items(quickCommerce) { product ->
                    ProductCardItem(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }

                // Trending & Recommended Section Title
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Text(
                        text = "Trending & Recommended",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = VyroxNavy,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                items(trending) { product ->
                    ProductCardItem(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        // Floating "Ask VYROX AI" Action Button
        FloatingActionButton(
            onClick = onNavigateToAi,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = VyroxOrange,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Ask VYROX AI", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask AI", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
