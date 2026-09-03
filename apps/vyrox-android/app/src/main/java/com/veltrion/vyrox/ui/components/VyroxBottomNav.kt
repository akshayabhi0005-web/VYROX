package com.veltrion.vyrox.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange

/**
 * MANDATED 4 PRIMARY BOTTOM NAVIGATION ITEMS:
 * 1. HOME
 * 2. TOP DEALS
 * 3. ACCOUNT
 * 4. CART
 *
 * Strictly NO Play, Games, Travel, or extra modules.
 */
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object TopDeals : BottomNavItem("top_deals", "Top Deals", Icons.Filled.LocalOffer)
    object Account : BottomNavItem("account", "Account", Icons.Filled.AccountCircle)
    object Cart : BottomNavItem("cart", "Cart", Icons.Filled.ShoppingCart)
}

@Composable
fun VyroxBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.TopDeals,
        BottomNavItem.Account,
        BottomNavItem.Cart
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VyroxOrange,
                    selectedTextColor = VyroxNavy,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
