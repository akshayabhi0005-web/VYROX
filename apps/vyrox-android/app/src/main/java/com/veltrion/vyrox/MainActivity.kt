package com.veltrion.vyrox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.veltrion.vyrox.ui.screens.*
import com.veltrion.vyrox.ui.theme.VYROXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VYROXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VyroxAppNavigation()
                }
            }
        }
    }
}

@Composable
fun VyroxAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGuestContinue = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {}
            )
        }

        composable("main") {
            MainContainerScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToProductDetail = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                onNavigateToTracking = { orderNumber ->
                    navController.navigate("order_tracking/$orderNumber")
                },
                onNavigateToAi = { navController.navigate("ai") },
                onNavigateToLocation = { navController.navigate("address") },
                onNavigateToOrders = { navController.navigate("orders") },
                onNavigateToCoins = { navController.navigate("coins") },
                onNavigateToWishlist = { navController.navigate("wishlist") },
                onNavigateToCoupons = { navController.navigate("coupons") },
                onNavigateToHelpCenter = { navController.navigate("help") },
                onNavigateToCheckout = { navController.navigate("checkout?productId=-1") }
            )
        }

        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 1L
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToCart = {
                    navController.navigate("main")
                },
                onNavigateToBuyNow = { pId ->
                    navController.navigate("checkout?productId=$pId")
                }
            )
        }

        composable(
            route = "order_tracking/{orderNumber}",
            arguments = listOf(navArgument("orderNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderNumber = backStackEntry.arguments?.getString("orderNumber") ?: "VYR-2026-90412"
            OrderTrackingScreen(
                orderNumber = orderNumber,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("orders") {
            OrdersScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToTracking = { orderNumber ->
                    navController.navigate("order_tracking/$orderNumber")
                }
            )
        }

        composable("coins") {
            CoinsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("wishlist") {
            WishlistScreen(
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate("product_detail/$productId") },
                onAddToCart = { productId -> navController.popBackStack() }
            )
        }

        composable("coupons") {
            CouponsScreen(
                onBackClick = { navController.popBackStack() },
                onApplyCoupon = { navController.popBackStack() }
            )
        }

        composable("help") {
            HelpCenterScreen(
                onBackClick = { navController.popBackStack() },
                onContactSupport = {}
            )
        }

        composable("address") {
            AddressLocationScreen(
                onBackClick = { navController.popBackStack() },
                onSaveAddress = { navController.popBackStack() }
            )
        }

        composable("ai") {
            VyroxAiScreen(
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate("product_detail/$productId") }
            )
        }

        composable(
            route = "checkout?productId={productId}",
            arguments = listOf(navArgument("productId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
            CheckoutScreen(
                buyNowProductId = if (productId > 0) productId else null,
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = { orderNumber ->
                    navController.navigate("order_tracking/$orderNumber") {
                        popUpTo("main")
                    }
                }
            )
        }
    }
}
