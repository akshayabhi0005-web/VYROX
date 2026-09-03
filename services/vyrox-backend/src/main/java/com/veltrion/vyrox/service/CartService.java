package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Cart getOrCreateUserCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    public CommerceDto.CartResponse getCartDto(User user) {
        Cart cart = getOrCreateUserCart(user);
        List<CartItem> allItems = cartItemRepository.findByCartId(cart.getId());

        List<CommerceDto.CartItemDto> activeItems = new ArrayList<>();
        List<CommerceDto.CartItemDto> savedItems = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal originalMrpSum = BigDecimal.ZERO;

        for (CartItem item : allItems) {
            Product p = item.getProduct();
            CommerceDto.CartItemDto dto = mapToItemDto(item);

            if (item.isSavedForLater()) {
                savedItems.add(dto);
            } else {
                activeItems.add(dto);
                BigDecimal linePrice = p.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal lineMrp = p.getMrp().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(linePrice);
                originalMrpSum = originalMrpSum.add(lineMrp);
            }
        }

        BigDecimal savings = originalMrpSum.subtract(subtotal);
        if (savings.compareTo(BigDecimal.ZERO) < 0) savings = BigDecimal.ZERO;

        BigDecimal deliveryFee = (subtotal.compareTo(BigDecimal.valueOf(500)) >= 0 || activeItems.isEmpty()) ? BigDecimal.ZERO : BigDecimal.valueOf(40);
        BigDecimal grandTotal = subtotal.add(deliveryFee);

        // 5% cashback as VYROX Coins
        int potentialCoins = subtotal.multiply(BigDecimal.valueOf(0.05)).intValue();

        return CommerceDto.CartResponse.builder()
                .cartId(cart.getId())
                .items(activeItems)
                .savedForLaterItems(savedItems)
                .totalItems(activeItems.stream().mapToInt(CommerceDto.CartItemDto::getQuantity).sum())
                .subtotal(subtotal)
                .totalSavings(savings)
                .deliveryFee(deliveryFee)
                .grandTotal(grandTotal)
                .potentialCoinsEarned(potentialCoins)
                .build();
    }

    @Transactional
    public CommerceDto.CartResponse addToCart(User user, Long productId, Integer quantity) {
        Cart cart = getOrCreateUserCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        int qtyToAdd = (quantity != null && quantity > 0) ? quantity : 1;

        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        if (existing != null) {
            if (existing.isSavedForLater()) {
                existing.setSavedForLater(false);
                existing.setQuantity(qtyToAdd);
            } else {
                existing.setQuantity(existing.getQuantity() + qtyToAdd);
            }
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(qtyToAdd)
                    .savedForLater(false)
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCartDto(user);
    }

    @Transactional
    public CommerceDto.CartResponse updateQuantity(User user, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized cart item access");
        }

        if (quantity == null || quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCartDto(user);
    }

    @Transactional
    public CommerceDto.CartResponse toggleSaveForLater(User user, Long itemId, boolean saveForLater) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized cart item access");
        }

        item.setSavedForLater(saveForLater);
        cartItemRepository.save(item);

        return getCartDto(user);
    }

    @Transactional
    public CommerceDto.CartResponse removeItem(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized cart item access");
        }

        cartItemRepository.delete(item);
        return getCartDto(user);
    }

    private CommerceDto.CartItemDto mapToItemDto(CartItem item) {
        Product p = item.getProduct();
        return CommerceDto.CartItemDto.builder()
                .itemId(item.getId())
                .productId(p.getId())
                .productTitle(p.getTitle())
                .productSku(p.getSku())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .mainImageUrl(p.getMainImageUrl())
                .mrp(p.getMrp())
                .sellingPrice(p.getSellingPrice())
                .discountPercentage(p.getDiscountPercentage())
                .quantity(item.getQuantity())
                .savedForLater(item.isSavedForLater())
                .estimatedDelivery(p.getEstimatedDeliveryDays())
                .inStock(p.isInStock())
                .build();
    }
}
