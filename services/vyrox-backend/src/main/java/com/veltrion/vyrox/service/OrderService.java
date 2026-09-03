package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final CouponService couponService;
    private final CoinService coinService;
    private final PaymentRepository paymentRepository;
    private final TrackingLogRepository trackingLogRepository;
    private final DarkstoreRepository darkstoreRepository;

    @Transactional(readOnly = true)
    public CommerceDto.CheckoutSummaryResponse calculateCheckout(User user, CommerceDto.CheckoutCalculateRequest request) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        List<CartItem> activeItems = cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(i -> !i.isSavedForLater())
                .collect(Collectors.toList());

        if (activeItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal originalMrpTotal = BigDecimal.ZERO;

        for (CartItem item : activeItems) {
            BigDecimal linePrice = item.getProduct().getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineMrp = item.getProduct().getMrp().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(linePrice);
            originalMrpTotal = originalMrpTotal.add(lineMrp);
        }

        BigDecimal productDiscount = originalMrpTotal.subtract(subtotal);

        // Calculate coupon discount
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            CommerceDto.CouponValidationResult couponResult = couponService.validateCoupon(request.getCouponCode(), subtotal);
            if (couponResult.isValid()) {
                couponDiscount = couponResult.getDiscountAmount();
            }
        }

        // Calculate coins discount (1 Coin = ₹1, max 20% of subtotal)
        BigDecimal coinsDiscount = BigDecimal.ZERO;
        int coinsToRedeem = 0;
        if (request.isRedeemCoins()) {
            CoinWallet wallet = coinService.getOrCreateWallet(user);
            int maxAllowedCoins = subtotal.multiply(BigDecimal.valueOf(0.20)).intValue();
            coinsToRedeem = Math.min(wallet.getBalance(), maxAllowedCoins);
            coinsDiscount = BigDecimal.valueOf(coinsToRedeem);
        }

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (request.isQuickCommerce()) {
            deliveryFee = BigDecimal.valueOf(15); // Instant delivery fee
        } else if (subtotal.compareTo(BigDecimal.valueOf(500)) < 0) {
            deliveryFee = BigDecimal.valueOf(40);
        }

        BigDecimal grandTotal = subtotal.subtract(couponDiscount).subtract(coinsDiscount).add(deliveryFee);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;

        BigDecimal totalSavings = productDiscount.add(couponDiscount).add(coinsDiscount);
        int coinsEarned = grandTotal.multiply(BigDecimal.valueOf(0.05)).intValue();

        String estimatedDelivery = request.isQuickCommerce() ? "Delivering in 15 Minutes" : "Delivery by Tomorrow, 11 PM";

        return CommerceDto.CheckoutSummaryResponse.builder()
                .subtotal(subtotal)
                .originalMrpTotal(originalMrpTotal)
                .productDiscount(productDiscount)
                .couponDiscount(couponDiscount)
                .coinsDiscount(coinsDiscount)
                .coinsToRedeem(coinsToRedeem)
                .deliveryFee(deliveryFee)
                .grandTotal(grandTotal)
                .totalSavings(totalSavings)
                .coinsEarned(coinsEarned)
                .estimatedDelivery(estimatedDelivery)
                .build();
    }

    @Transactional
    public CommerceDto.OrderDto createOrder(User user, CommerceDto.CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        List<CartItem> activeItems = cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(i -> !i.isSavedForLater())
                .collect(Collectors.toList());

        if (activeItems.isEmpty()) {
            throw new IllegalArgumentException("Cart has no items to order");
        }

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipping address not found: " + request.getAddressId()));
        } else {
            List<Address> userAddresses = addressRepository.findByUserId(user.getId());
            if (!userAddresses.isEmpty()) {
                address = userAddresses.get(0);
            }
        }

        CommerceDto.CheckoutCalculateRequest calcReq = new CommerceDto.CheckoutCalculateRequest(
                request.getAddressId(), request.getCouponCode(), request.isRedeemCoins(), request.isQuickCommerce()
        );
        CommerceDto.CheckoutSummaryResponse summary = calculateCheckout(user, calcReq);

        // Generate 4-digit doorstep delivery confirmation OTP
        String doorstepOtp = String.format("%04d", new Random().nextInt(9000) + 1000);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .subtotal(summary.getSubtotal())
                .discountAmount(summary.getProductDiscount())
                .couponDiscount(summary.getCouponDiscount())
                .coinsDiscount(summary.getCoinsDiscount())
                .deliveryFee(summary.getDeliveryFee())
                .grandTotal(summary.getGrandTotal())
                .coinsEarned(summary.getCoinsEarned())
                .coinsRedeemed(summary.getCoinsToRedeem())
                .couponCodeApplied(summary.getCouponDiscount().compareTo(BigDecimal.ZERO) > 0 ? request.getCouponCode() : null)
                .shippingAddress(address)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI)
                .paymentStatus(request.getPaymentMethod() == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.SUCCESS)
                .doorstepOtp(doorstepOtp)
                .quickCommerce(request.isQuickCommerce())
                .estimatedDeliveryTime(summary.getEstimatedDelivery())
                .deliveryLatitude(address != null && address.getLatitude() != null ? address.getLatitude() : 12.9716)
                .deliveryLongitude(address != null && address.getLongitude() != null ? address.getLongitude() : 77.5946)
                .build();

        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : activeItems) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .productTitle(ci.getProduct().getTitle())
                    .productSku(ci.getProduct().getSku())
                    .mainImageUrl(ci.getProduct().getMainImageUrl())
                    .unitPrice(ci.getProduct().getSellingPrice())
                    .quantity(ci.getQuantity())
                    .totalPrice(ci.getProduct().getSellingPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build();
            orderItems.add(orderItemRepository.save(oi));
        }
        order.setItems(orderItems);

        // Record Payment transaction
        Payment payment = Payment.builder()
                .order(order)
                .transactionId("TXN-" + System.currentTimeMillis())
                .method(order.getPaymentMethod())
                .status(order.getPaymentStatus())
                .amount(order.getGrandTotal())
                .currency("INR")
                .gatewayResponse("Payment processed successfully via VYROX Secure Gateway (Sandbox Mode)")
                .build();
        paymentRepository.save(payment);

        // Record Initial Tracking log
        TrackingLog initialLog = TrackingLog.builder()
                .order(order)
                .status(OrderStatus.CONFIRMED)
                .description("Order confirmed. Preparing for packing at nearest Darkstore.")
                .locationName("VYROX Central Hub")
                .latitude(12.9610)
                .longitude(77.6010)
                .build();
        trackingLogRepository.save(initialLog);

        // Debit redeemed coins
        if (summary.getCoinsToRedeem() > 0) {
            coinService.debitCoins(user, summary.getCoinsToRedeem(), "Redeemed on Order #" + order.getOrderNumber(), order.getOrderNumber());
        }

        // Credit cashback coins
        if (summary.getCoinsEarned() > 0) {
            coinService.creditCoins(user, summary.getCoinsEarned(), CoinTransactionType.EARNED_PURCHASE, "Cashback on Order #" + order.getOrderNumber(), order.getOrderNumber());
        }

        // Clear ordered items from cart
        cartItemRepository.deleteAll(activeItems);

        return mapToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<CommerceDto.OrderDto> getUserOrders(User user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommerceDto.OrderDto getOrderById(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized order access");
        }

        return mapToOrderDto(order);
    }

    @Transactional
    public CommerceDto.OrderDto cancelOrder(User user, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized order cancellation");
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Order is already in delivery/delivered. Please request a return instead.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason != null ? reason : "Cancelled by customer");
        orderRepository.save(order);

        TrackingLog cancelLog = TrackingLog.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .description("Order cancelled: " + order.getCancelReason())
                .locationName("System")
                .build();
        trackingLogRepository.save(cancelLog);

        return mapToOrderDto(order);
    }

    public CommerceDto.OrderDto mapToOrderDto(Order o) {
        List<CommerceDto.OrderItemDto> items = o.getItems().stream().map(i -> CommerceDto.OrderItemDto.builder()
                .id(i.getId())
                .productId(i.getProduct().getId())
                .productTitle(i.getProductTitle())
                .productSku(i.getProductSku())
                .mainImageUrl(i.getMainImageUrl())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .totalPrice(i.getTotalPrice())
                .build()).collect(Collectors.toList());

        CommerceDto.AddressDto addrDto = null;
        if (o.getShippingAddress() != null) {
            Address a = o.getShippingAddress();
            addrDto = CommerceDto.AddressDto.builder()
                    .id(a.getId())
                    .name(a.getName())
                    .mobile(a.getMobile())
                    .street(a.getStreet())
                    .locality(a.getLocality())
                    .city(a.getCity())
                    .state(a.getState())
                    .pincode(a.getPincode())
                    .landmark(a.getLandmark())
                    .addressType(a.getAddressType())
                    .isDefault(a.isDefault())
                    .latitude(a.getLatitude())
                    .longitude(a.getLongitude())
                    .build();
        }

        return CommerceDto.OrderDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .couponDiscount(o.getCouponDiscount())
                .coinsDiscount(o.getCoinsDiscount())
                .deliveryFee(o.getDeliveryFee())
                .grandTotal(o.getGrandTotal())
                .coinsEarned(o.getCoinsEarned())
                .coinsRedeemed(o.getCoinsRedeemed())
                .couponCodeApplied(o.getCouponCodeApplied())
                .shippingAddress(addrDto)
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .doorstepOtp(o.getDoorstepOtp())
                .quickCommerce(o.isQuickCommerce())
                .estimatedDeliveryTime(o.getEstimatedDeliveryTime())
                .deliveredAt(o.getDeliveredAt())
                .createdAt(o.getCreatedAt())
                .items(items)
                .build();
    }
}
