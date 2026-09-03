package com.veltrion.vyrox.data.repository

import com.veltrion.vyrox.data.api.ApiClient
import com.veltrion.vyrox.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object CommerceRepository {

    val demoProducts = listOf(
        ProductSummary(
            id = 1L,
            title = "Apple MacBook Pro 16\" (M3 Pro Chip, 18GB RAM, 512GB SSD, Space Black)",
            sku = "APP-MBP16-M3P",
            categoryName = "Electronics",
            categoryId = 2L,
            brandName = "Apple",
            mrp = 249900.0,
            sellingPrice = 219900.0,
            discountPercentage = 12,
            averageRating = 4.9,
            reviewCount = 1840,
            mainImageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = true,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "Tomorrow by 11 AM",
            freeDelivery = true
        ),
        ProductSummary(
            id = 2L,
            title = "Dell XPS 16 Laptop (Intel Core Ultra 9 185H, 32GB DDR5, 1TB NVMe, RTX 4070)",
            sku = "DELL-XPS16-U9",
            categoryName = "Electronics",
            categoryId = 2L,
            brandName = "Dell",
            mrp = 279990.0,
            sellingPrice = 234990.0,
            discountPercentage = 16,
            averageRating = 4.7,
            reviewCount = 640,
            mainImageUrl = "https://images.unsplash.com/photo-1593642632823-8f785ba67e45",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = false,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "2 Days",
            freeDelivery = true
        ),
        ProductSummary(
            id = 3L,
            title = "Samsung Galaxy S24 Ultra 5G (Titanium Gray, 12GB RAM, 256GB Storage, AI Enabled)",
            sku = "SAM-S24U-256",
            categoryName = "Mobiles",
            categoryId = 1L,
            brandName = "Samsung",
            mrp = 134999.0,
            sellingPrice = 119999.0,
            discountPercentage = 11,
            averageRating = 4.8,
            reviewCount = 4210,
            mainImageUrl = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = true,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "Tomorrow by 11 AM",
            freeDelivery = true
        ),
        ProductSummary(
            id = 4L,
            title = "Apple iPhone 15 Pro Max (256 GB, Natural Titanium, A17 Pro Chip)",
            sku = "APP-IP15PM-256",
            categoryName = "Mobiles",
            categoryId = 1L,
            brandName = "Apple",
            mrp = 159900.0,
            sellingPrice = 148900.0,
            discountPercentage = 7,
            averageRating = 4.9,
            reviewCount = 8920,
            mainImageUrl = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = false,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "Tomorrow by 11 AM",
            freeDelivery = true
        ),
        ProductSummary(
            id = 5L,
            title = "Sony WH-1000XM5 Wireless Industry Leading Active Noise Canceling Headphones",
            sku = "SNY-WH1000XM5",
            categoryName = "Audio",
            categoryId = 3L,
            brandName = "Sony",
            mrp = 34990.0,
            sellingPrice = 28990.0,
            discountPercentage = 17,
            averageRating = 4.8,
            reviewCount = 5230,
            mainImageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = true,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "Tomorrow by 11 AM",
            freeDelivery = true
        ),
        ProductSummary(
            id = 6L,
            title = "Nike Air Jordan 1 Retro High OG Chicago Lost and Found",
            sku = "NKE-AJ1-CHI",
            categoryName = "Fashion",
            categoryId = 4L,
            brandName = "Nike",
            mrp = 19995.0,
            sellingPrice = 16995.0,
            discountPercentage = 15,
            averageRating = 4.9,
            reviewCount = 3120,
            mainImageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = true,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "2 Days",
            freeDelivery = true
        ),
        ProductSummary(
            id = 7L,
            title = "Philips Digital Airfryer HD9252/90 with Rapid Air Technology (4.1 Litre)",
            sku = "PHI-AF-HD9252",
            categoryName = "Appliances",
            categoryId = 5L,
            brandName = "Philips",
            mrp = 11995.0,
            sellingPrice = 7995.0,
            discountPercentage = 33,
            averageRating = 4.6,
            reviewCount = 2890,
            mainImageUrl = "https://images.unsplash.com/photo-1584992236310-6edddc08acff",
            inStock = true,
            isTopDeal = true,
            isTrending = true,
            isBestSeller = true,
            isQuickCommerceEligible = false,
            estimatedDeliveryDays = "Tomorrow by 11 AM",
            freeDelivery = true
        ),
        ProductSummary(
            id = 8L,
            title = "VYROX Fresh California Roasted Almonds & Cranberry Trail Mix (500g)",
            sku = "VYR-QC-ALM500",
            categoryName = "Quick Commerce (15-Min)",
            categoryId = 8L,
            brandName = "VYROX Select",
            mrp = 650.0,
            sellingPrice = 399.0,
            discountPercentage = 39,
            averageRating = 4.8,
            reviewCount = 780,
            mainImageUrl = "https://images.unsplash.com/photo-1508746829417-e6f548d8d6ed",
            inStock = true,
            isTopDeal = true,
            isTrending = false,
            isBestSeller = true,
            isQuickCommerceEligible = true,
            estimatedDeliveryDays = "15 Mins",
            freeDelivery = false
        ),
        ProductSummary(
            id = 9L,
            title = "VYROX Organic Cold-Pressed Valencia Orange Juice (1 Litre, No Added Sugar)",
            sku = "VYR-QC-JUICE1L",
            categoryName = "Quick Commerce (15-Min)",
            categoryId = 8L,
            brandName = "VYROX Select",
            mrp = 250.0,
            sellingPrice = 179.0,
            discountPercentage = 28,
            averageRating = 4.9,
            reviewCount = 540,
            mainImageUrl = "https://images.unsplash.com/photo-1600271886742-f049cd451bba",
            inStock = true,
            isTopDeal = true,
            isTrending = false,
            isBestSeller = true,
            isQuickCommerceEligible = true,
            estimatedDeliveryDays = "15 Mins",
            freeDelivery = false
        )
    )

    private val localCartItems = mutableListOf<CartItemDto>(
        CartItemDto(
            itemId = 1L,
            productId = 1L,
            productTitle = "Apple MacBook Pro 16\" (M3 Pro Chip, 18GB RAM, 512GB SSD, Space Black)",
            productSku = "APP-MBP16-M3P",
            brandName = "Apple",
            mainImageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
            mrp = 249900.0,
            sellingPrice = 219900.0,
            discountPercentage = 12,
            quantity = 1,
            savedForLater = false,
            estimatedDelivery = "Tomorrow by 11 AM"
        )
    )

    private val localSavedForLaterItems = mutableListOf<CartItemDto>()

    private fun buildLocalCart(): CartResponse {
        val totalItems = localCartItems.sumOf { it.quantity }
        val subtotal = localCartItems.sumOf { it.mrp * it.quantity }
        val grandTotal = localCartItems.sumOf { it.sellingPrice * it.quantity }
        val savings = subtotal - grandTotal
        return CartResponse(
            cartId = 1L,
            items = localCartItems.toList(),
            savedForLaterItems = localSavedForLaterItems.toList(),
            totalItems = totalItems,
            subtotal = subtotal,
            totalSavings = savings,
            deliveryFee = if (grandTotal > 500.0 || localCartItems.isEmpty()) 0.0 else 40.0,
            grandTotal = grandTotal,
            potentialCoinsEarned = (grandTotal * 0.05).toInt()
        )
    }

    private val _cartFlow = MutableStateFlow<CartResponse>(buildLocalCart())
    val cartFlow: StateFlow<CartResponse> = _cartFlow.asStateFlow()

    private val _wishlistFlow = MutableStateFlow<Set<Long>>(setOf(1L, 3L))
    val wishlistFlow: StateFlow<Set<Long>> = _wishlistFlow.asStateFlow()

    private val localOrders = mutableListOf<OrderDto>(
        OrderDto(
            id = 1L,
            orderNumber = "VYR-2026-90412",
            status = "OUT_FOR_DELIVERY",
            subtotal = 219900.0,
            grandTotal = 219900.0,
            doorstepOtp = "4829",
            quickCommerce = false,
            estimatedDeliveryTime = "Arriving in 3 Mins",
            items = listOf(
                OrderItemDto(
                    id = 1L,
                    productId = 1L,
                    productTitle = "Apple MacBook Pro 16\" (M3 Pro Chip, 18GB RAM, 512GB SSD, Space Black)",
                    mainImageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
                    unitPrice = 219900.0,
                    quantity = 1,
                    totalPrice = 219900.0
                )
            )
        )
    )

    private val _ordersFlow = MutableStateFlow<List<OrderDto>>(localOrders.toList())
    val ordersFlow: StateFlow<List<OrderDto>> = _ordersFlow.asStateFlow()

    private val _coinBalanceFlow = MutableStateFlow(250)
    val coinBalanceFlow: StateFlow<Int> = _coinBalanceFlow.asStateFlow()

    suspend fun getTopDeals(): List<ProductSummary> {
        return try {
            val res = ApiClient.apiService.getTopDeals()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) res.body()!! else demoProducts.filter { it.isTopDeal }
        } catch (e: Exception) {
            demoProducts.filter { it.isTopDeal }
        }
    }

    suspend fun getTrending(): List<ProductSummary> {
        return try {
            val res = ApiClient.apiService.getTrending()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) res.body()!! else demoProducts.filter { it.isTrending }
        } catch (e: Exception) {
            demoProducts.filter { it.isTrending }
        }
    }

    suspend fun getBestSellers(): List<ProductSummary> {
        return try {
            val res = ApiClient.apiService.getBestSellers()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) res.body()!! else demoProducts.filter { it.isBestSeller }
        } catch (e: Exception) {
            demoProducts.filter { it.isBestSeller }
        }
    }

    suspend fun getQuickCommerce(): List<ProductSummary> {
        return try {
            val res = ApiClient.apiService.getQuickCommerce()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) res.body()!! else demoProducts.filter { it.isQuickCommerceEligible }
        } catch (e: Exception) {
            demoProducts.filter { it.isQuickCommerceEligible }
        }
    }

    suspend fun getProductDetail(id: Long): ProductDetail? {
        return try {
            val res = ApiClient.apiService.getProductDetail(id)
            if (res.isSuccessful && res.body() != null) res.body() else getDemoProductDetail(id)
        } catch (e: Exception) {
            getDemoProductDetail(id)
        }
    }

    fun getDemoProductDetail(id: Long): ProductDetail {
        val item = demoProducts.find { it.id == id } ?: demoProducts[0]
        return ProductDetail(
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
                SpecItem("General", "Model Name", item.title.take(30)),
                SpecItem("General", "Brand", item.brandName ?: "VYROX"),
                SpecItem("Performance", "Processor/Engine", "Next-Gen Ultra Architecture"),
                SpecItem("Warranty", "Warranty Summary", "1 Year Manufacturer Comprehensive Warranty")
            ),
            sellerName = "VYROX Retail India Pvt Ltd",
            sellerRating = 4.8,
            warrantyInfo = "1 Year Manufacturer Comprehensive Warranty",
            isTopDeal = item.isTopDeal,
            isQuickCommerceEligible = item.isQuickCommerceEligible,
            estimatedDeliveryDays = item.estimatedDeliveryDays
        )
    }

    suspend fun getCart(): CartResponse {
        return try {
            val res = ApiClient.apiService.getCart()
            if (res.isSuccessful && res.body() != null) {
                _cartFlow.value = res.body()!!
                res.body()!!
            } else {
                val local = buildLocalCart()
                _cartFlow.value = local
                local
            }
        } catch (e: Exception) {
            val local = buildLocalCart()
            _cartFlow.value = local
            local
        }
    }

    suspend fun addToCart(productId: Long, deltaQuantity: Int = 1): CartResponse {
        val product = demoProducts.find { it.id == productId } ?: demoProducts[0]
        val existingIndex = localCartItems.indexOfFirst { it.productId == productId }
        if (existingIndex >= 0) {
            val existing = localCartItems[existingIndex]
            val newQty = existing.quantity + deltaQuantity
            if (newQty > 0) {
                localCartItems[existingIndex] = existing.copy(quantity = newQty)
            } else {
                localCartItems.removeAt(existingIndex)
            }
        } else if (deltaQuantity > 0) {
            localCartItems.add(
                CartItemDto(
                    itemId = (localCartItems.size + 1).toLong(),
                    productId = product.id,
                    productTitle = product.title,
                    productSku = product.sku,
                    brandName = product.brandName,
                    mainImageUrl = product.mainImageUrl,
                    mrp = product.mrp,
                    sellingPrice = product.sellingPrice,
                    discountPercentage = product.discountPercentage,
                    quantity = deltaQuantity,
                    savedForLater = false,
                    estimatedDelivery = product.estimatedDeliveryDays
                )
            )
        }

        try {
            ApiClient.apiService.addToCart(AddToCartRequest(productId, deltaQuantity))
        } catch (_: Exception) {}

        val updated = buildLocalCart()
        _cartFlow.value = updated
        return updated
    }

    fun saveForLater(productId: Long): CartResponse {
        val itemIndex = localCartItems.indexOfFirst { it.productId == productId }
        if (itemIndex >= 0) {
            val item = localCartItems.removeAt(itemIndex)
            localSavedForLaterItems.add(item.copy(savedForLater = true))
        }
        val updated = buildLocalCart()
        _cartFlow.value = updated
        return updated
    }

    fun moveToCart(productId: Long): CartResponse {
        val itemIndex = localSavedForLaterItems.indexOfFirst { it.productId == productId }
        if (itemIndex >= 0) {
            val item = localSavedForLaterItems.removeAt(itemIndex)
            localCartItems.add(item.copy(savedForLater = false))
        }
        val updated = buildLocalCart()
        _cartFlow.value = updated
        return updated
    }

    fun clearCart(): CartResponse {
        localCartItems.clear()
        val updated = buildLocalCart()
        _cartFlow.value = updated
        return updated
    }

    fun toggleWishlist(productId: Long): Boolean {
        val current = _wishlistFlow.value.toMutableSet()
        val newState = if (current.contains(productId)) {
            current.remove(productId)
            false
        } else {
            current.add(productId)
            true
        }
        _wishlistFlow.value = current
        return newState
    }

    fun isWishlisted(productId: Long): Boolean {
        return _wishlistFlow.value.contains(productId)
    }

    suspend fun getOrders(): List<OrderDto> {
        return try {
            val res = ApiClient.apiService.getOrders()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                _ordersFlow.value = res.body()!!
                res.body()!!
            } else {
                localOrders.toList()
            }
        } catch (e: Exception) {
            localOrders.toList()
        }
    }

    fun createOrder(
        items: List<OrderItemDto>,
        subtotal: Double,
        grandTotal: Double,
        paymentMethod: String,
        coinsUsed: Int = 0
    ): OrderDto {
        val randomNum = Random.nextInt(10000, 99999)
        val orderNumber = "VYR-2026-$randomNum"
        val otp = Random.nextInt(1000, 9999).toString()

        val newOrder = OrderDto(
            id = (localOrders.size + 1).toLong(),
            orderNumber = orderNumber,
            status = "CONFIRMED",
            subtotal = subtotal,
            grandTotal = grandTotal,
            doorstepOtp = otp,
            quickCommerce = items.any { it.productTitle.contains("15-Min", ignoreCase = true) || it.productTitle.contains("Fresh", ignoreCase = true) },
            estimatedDeliveryTime = "Arriving in 15 Mins",
            items = items
        )

        localOrders.add(0, newOrder)
        _ordersFlow.value = localOrders.toList()

        // Update Coins
        if (coinsUsed > 0) {
            _coinBalanceFlow.value = maxOf(0, _coinBalanceFlow.value - coinsUsed)
        }
        val earned = (grandTotal * 0.05).toInt()
        _coinBalanceFlow.value += earned

        return newOrder
    }

    suspend fun getLiveTracking(orderNumber: String): LiveTrackingDto {
        return try {
            val res = ApiClient.apiService.getLiveTracking(orderNumber)
            if (res.isSuccessful && res.body() != null) res.body()!! else getDemoLiveTracking(orderNumber)
        } catch (e: Exception) {
            getDemoLiveTracking(orderNumber)
        }
    }

    fun getDemoLiveTracking(orderNumber: String): LiveTrackingDto {
        val order = localOrders.find { it.orderNumber == orderNumber }
        val otp = order?.doorstepOtp ?: "4829"
        return LiveTrackingDto(
            orderNumber = orderNumber,
            status = order?.status ?: "OUT_FOR_DELIVERY",
            estimatedDeliveryTime = order?.estimatedDeliveryTime ?: "Arriving in 3 Mins",
            doorstepOtp = otp,
            customerLat = 12.9716,
            customerLng = 77.5946,
            darkstoreLat = 12.9780,
            darkstoreLng = 77.6400,
            darkstoreName = "VYROX Indiranagar Hub",
            driverLat = 12.9730,
            driverLng = 77.6010,
            driverName = "Ramesh Kumar (VYROX Express Rider)",
            driverPhone = "+91 98765 43210",
            driverVehicle = "Ather 450X EV Scooter [KA-01-VY-4098]",
            currentStatusDescription = "Rider is en route with your package!",
            distanceKm = 0.8,
            etaMinutes = 3,
            isSimulatedGps = true
        )
    }
}
