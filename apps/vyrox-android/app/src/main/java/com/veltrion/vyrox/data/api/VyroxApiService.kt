package com.veltrion.vyrox.data.api

import com.veltrion.vyrox.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface VyroxApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/otp/send")
    suspend fun sendOtp(@Body request: OtpSendRequest): Response<Map<String, Any>>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<AuthResponse>

    @POST("auth/oauth/google")
    suspend fun googleOAuth(@Body request: OAuthRequest): Response<AuthResponse>

    @POST("auth/oauth/facebook")
    suspend fun facebookOAuth(@Body request: OAuthRequest): Response<AuthResponse>

    // Products (Guest Accessible)
    @GET("products/top-deals")
    suspend fun getTopDeals(): Response<List<ProductSummary>>

    @GET("products/trending")
    suspend fun getTrending(): Response<List<ProductSummary>>

    @GET("products/best-sellers")
    suspend fun getBestSellers(): Response<List<ProductSummary>>

    @GET("products/quick-commerce")
    suspend fun getQuickCommerce(): Response<List<ProductSummary>>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: Long): Response<ProductDetail>

    @GET("products")
    suspend fun searchProducts(
        @Query("query") query: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("sortBy") sortBy: String? = "popularity"
    ): Response<Map<String, Any>>

    // Cart (Protected)
    @GET("cart")
    suspend fun getCart(): Response<CartResponse>

    @POST("cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<CartResponse>

    @PUT("cart/items/{itemId}")
    suspend fun updateQuantity(
        @Path("itemId") itemId: Long,
        @Body body: Map<String, Int>
    ): Response<CartResponse>

    @DELETE("cart/items/{itemId}")
    suspend fun removeCartItem(@Path("itemId") itemId: Long): Response<CartResponse>

    // Wishlist (Protected)
    @GET("wishlist")
    suspend fun getWishlist(): Response<Map<String, Any>>

    @POST("wishlist/add/{productId}")
    suspend fun addToWishlist(@Path("productId") productId: Long): Response<Map<String, Any>>

    // Orders (Protected)
    @GET("orders")
    suspend fun getOrders(): Response<List<OrderDto>>

    // Live Tracking
    @GET("tracking/order/{orderNumber}")
    suspend fun getLiveTracking(@Path("orderNumber") orderNumber: String): Response<LiveTrackingDto>
}
