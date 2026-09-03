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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun TopDealsScreen(
    onProductClick: (Long) -> Unit
) {
    var allDeals by remember { mutableStateOf(CommerceRepository.demoProducts) }
    var selectedCategory by remember { mutableStateOf("All Deals") }

    LaunchedEffect(Unit) {
        val deals = CommerceRepository.getTopDeals()
        if (deals.isNotEmpty()) allDeals = deals
    }

    val filteredDeals = remember(selectedCategory, allDeals) {
        if (selectedCategory == "All Deals") {
            allDeals
        } else {
            allDeals.filter { item ->
                val cat = item.categoryName ?: ""
                when (selectedCategory) {
                    "Mobiles" -> cat.contains("Mobile", ignoreCase = true)
                    "Electronics" -> cat.contains("Electronic", ignoreCase = true)
                    "Fashion" -> cat.contains("Fashion", ignoreCase = true)
                    "Appliances" -> cat.contains("Appliance", ignoreCase = true)
                    "Quick 15-Min" -> item.isQuickCommerceEligible
                    else -> true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FD))) {
        VyroxTopBar()

        // Reference UX Header: "All your deals in one place"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "All your deals in one place",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = VyroxNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Deals", "Mobiles", "Electronics", "Fashion", "Appliances", "Quick 15-Min").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VyroxOrange else Color(0xFFF1F5F9))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Product Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredDeals) { product ->
                ProductCardItem(
                    product = product,
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}
