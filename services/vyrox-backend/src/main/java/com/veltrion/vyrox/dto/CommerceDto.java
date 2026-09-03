package com.veltrion.vyrox.dto;

import com.veltrion.vyrox.model.OrderStatus;
import com.veltrion.vyrox.model.PaymentMethod;
import com.veltrion.vyrox.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CommerceDto {

    // CART
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartResponse {
        private Long cartId;
        private List<CartItemDto> items;
        private List<CartItemDto> savedForLaterItems;
        private Integer totalItems;
        private BigDecimal subtotal;
        private BigDecimal totalSavings;
        private BigDecimal deliveryFee;
        private BigDecimal grandTotal;
        private Integer potentialCoinsEarned;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDto {
        private Long itemId;
        private Long productId;
        private String productTitle;
        private String productSku;
        private String categoryName;
        private String brandName;
        private String mainImageUrl;
        private BigDecimal mrp;
        private BigDecimal sellingPrice;
        private Integer discountPercentage;
        private Integer quantity;
        private boolean savedForLater;
        private String estimatedDelivery;
        private boolean inStock;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartRequest {
        private Long productId;
        private Integer quantity = 1;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCartQuantityRequest {
        private Integer quantity;
    }

    // WISHLIST
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistResponse {
        private Long wishlistId;
        private List<ProductDto.Summary> items;
        private Integer totalItems;
    }

    // COUPONS
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponDto {
        private Long id;
        private String code;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private LocalDateTime validUntil;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyCouponRequest {
        private String code;
        private BigDecimal cartTotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponValidationResult {
        private boolean valid;
        private String message;
        private String code;
        private BigDecimal discountAmount;
    }

    // COINS
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoinWalletDto {
        private Integer balance;
        private Integer lifetimeEarned;
        private Integer lifetimeSpent;
        private List<CoinTransactionDto> recentTransactions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoinTransactionDto {
        private Long id;
        private String type;
        private Integer amount;
        private String description;
        private String referenceId;
        private LocalDateTime timestamp;
    }

    // CHECKOUT & ORDERS
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutCalculateRequest {
        private Long addressId;
        private String couponCode;
        private boolean redeemCoins;
        private boolean quickCommerce;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutSummaryResponse {
        private BigDecimal subtotal;
        private BigDecimal originalMrpTotal;
        private BigDecimal productDiscount;
        private BigDecimal couponDiscount;
        private BigDecimal coinsDiscount;
        private Integer coinsToRedeem;
        private BigDecimal deliveryFee;
        private BigDecimal grandTotal;
        private BigDecimal totalSavings;
        private Integer coinsEarned;
        private String estimatedDelivery;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {
        private Long addressId;
        private String couponCode;
        private boolean redeemCoins;
        private PaymentMethod paymentMethod;
        private boolean quickCommerce;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDto {
        private Long id;
        private String orderNumber;
        private OrderStatus status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal couponDiscount;
        private BigDecimal coinsDiscount;
        private BigDecimal deliveryFee;
        private BigDecimal grandTotal;
        private Integer coinsEarned;
        private Integer coinsRedeemed;
        private String couponCodeApplied;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;
        private String doorstepOtp;
        private boolean quickCommerce;
        private String estimatedDeliveryTime;
        private LocalDateTime deliveredAt;
        private LocalDateTime createdAt;
        private AddressDto shippingAddress;
        private List<OrderItemDto> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productSku;
        private String mainImageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDto {
        private Long id;
        private String name;
        private String mobile;
        private String street;
        private String locality;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private String addressType;
        private boolean isDefault;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LiveTrackingDto {
        private String orderNumber;
        private OrderStatus status;
        private String estimatedDeliveryTime;
        private String doorstepOtp;
        private Double customerLat;
        private Double customerLng;
        private Double darkstoreLat;
        private Double darkstoreLng;
        private String darkstoreName;
        private Double driverLat;
        private Double driverLng;
        private String driverName;
        private String driverPhone;
        private String driverVehicle;
        private String currentStatusDescription;
        private Double distanceKm;
        private Integer etaMinutes;
        private boolean isSimulatedGps;
        private List<TrackingLogItem> logs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackingLogItem {
        private String status;
        private String description;
        private String locationName;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDto {
        private Long id;
        private String reviewerName;
        private Integer rating;
        private String title;
        private String comment;
        private boolean verifiedPurchase;
        private Integer helpfulCount;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        private Integer rating;
        private String title;
        private String comment;
    }
}
