package com.veltrion.vyrox.data.model

data class UserDto(
    val id: Long,
    val fullName: String,
    val email: String?,
    val mobile: String?,
    val profilePictureUrl: String?,
    val roles: List<String>?,
    val coinBalance: Int?
)

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresIn: Long?,
    val user: UserDto?,
    val message: String?,
    val configurationRequired: Boolean?
)

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String?,
    val mobile: String?,
    val password: String?
)

data class OtpSendRequest(val mobile: String)
data class OtpVerifyRequest(val mobile: String, val otp: String)
data class OAuthRequest(val token: String, val provider: String)

data class ProductSummary(
    val id: Long,
    val title: String,
    val sku: String,
    val categoryName: String?,
    val categoryId: Long?,
    val brandName: String?,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercentage: Int,
    val averageRating: Double,
    val reviewCount: Int,
    val mainImageUrl: String,
    val inStock: Boolean,
    val isTopDeal: Boolean,
    val isTrending: Boolean,
    val isBestSeller: Boolean,
    val isQuickCommerceEligible: Boolean,
    val estimatedDeliveryDays: String?,
    val freeDelivery: Boolean
)

data class SpecItem(
    val group: String,
    val name: String,
    val value: String
)

data class ProductDetail(
    val id: Long,
    val title: String,
    val sku: String,
    val description: String?,
    val categoryName: String?,
    val brandName: String?,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercentage: Int,
    val averageRating: Double,
    val reviewCount: Int,
    val images: List<String>?,
    val mainImageUrl: String,
    val highlights: List<String>?,
    val bankOffers: List<String>?,
    val specifications: List<SpecItem>?,
    val sellerName: String?,
    val sellerRating: Double?,
    val warrantyInfo: String?,
    val isTopDeal: Boolean,
    val isQuickCommerceEligible: Boolean,
    val estimatedDeliveryDays: String?
)

data class CartItemDto(
    val itemId: Long,
    val productId: Long,
    val productTitle: String,
    val productSku: String,
    val brandName: String?,
    val mainImageUrl: String,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercentage: Int,
    val quantity: Int,
    val savedForLater: Boolean,
    val estimatedDelivery: String?
)

data class CartResponse(
    val cartId: Long,
    val items: List<CartItemDto>,
    val savedForLaterItems: List<CartItemDto>?,
    val totalItems: Int,
    val subtotal: Double,
    val totalSavings: Double,
    val deliveryFee: Double,
    val grandTotal: Double,
    val potentialCoinsEarned: Int
)

data class AddToCartRequest(
    val productId: Long,
    val quantity: Int = 1
)

data class OrderItemDto(
    val id: Long,
    val productId: Long,
    val productTitle: String,
    val mainImageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double
)

data class OrderDto(
    val id: Long,
    val orderNumber: String,
    val status: String,
    val subtotal: Double,
    val grandTotal: Double,
    val doorstepOtp: String?,
    val quickCommerce: Boolean,
    val estimatedDeliveryTime: String?,
    val items: List<OrderItemDto>
)

data class LiveTrackingDto(
    val orderNumber: String,
    val status: String,
    val estimatedDeliveryTime: String,
    val doorstepOtp: String,
    val customerLat: Double,
    val customerLng: Double,
    val darkstoreLat: Double,
    val darkstoreLng: Double,
    val darkstoreName: String,
    val driverLat: Double,
    val driverLng: Double,
    val driverName: String,
    val driverPhone: String,
    val driverVehicle: String,
    val currentStatusDescription: String,
    val distanceKm: Double,
    val etaMinutes: Int,
    val isSimulatedGps: Boolean
)
