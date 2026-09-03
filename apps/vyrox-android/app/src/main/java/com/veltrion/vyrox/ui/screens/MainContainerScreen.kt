package com.veltrion.vyrox.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.veltrion.vyrox.ui.components.BottomNavItem
import com.veltrion.vyrox.ui.components.VyroxBottomNav

@Composable
fun MainContainerScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToAi: () -> Unit = {},
    onNavigateToLocation: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToCoins: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf<String>(BottomNavItem.Home.route) }

    Scaffold(
        bottomBar = {
            VyroxBottomNav(
                currentRoute = currentTab,
                onNavigate = { route -> currentTab = route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomNavItem.Home.route -> {
                    HomeScreen(
                        onProductClick = { productId -> onNavigateToProductDetail(productId) },
                        onNavigateToDeals = { currentTab = BottomNavItem.TopDeals.route },
                        onNavigateToAi = onNavigateToAi,
                        onNavigateToLocation = onNavigateToLocation
                    )
                }
                BottomNavItem.TopDeals.route -> {
                    TopDealsScreen(
                        onProductClick = { productId -> onNavigateToProductDetail(productId) }
                    )
                }
                BottomNavItem.Account.route -> {
                    AccountScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToTracking = onNavigateToTracking,
                        onNavigateToOrders = onNavigateToOrders,
                        onNavigateToCoins = onNavigateToCoins,
                        onNavigateToWishlist = onNavigateToWishlist,
                        onNavigateToCoupons = onNavigateToCoupons,
                        onNavigateToHelpCenter = onNavigateToHelpCenter,
                        onNavigateToAddress = onNavigateToLocation
                    )
                }
                BottomNavItem.Cart.route -> {
                    CartScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToCheckout = onNavigateToCheckout,
                        onNavigateToLocation = onNavigateToLocation,
                        onProductClick = { productId -> onNavigateToProductDetail(productId) }
                    )
                }
            }
        }
    }
}
