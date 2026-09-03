package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.dto.ProductDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId()).orElseGet(() -> {
            Wishlist w = Wishlist.builder().user(user).build();
            return wishlistRepository.save(w);
        });
    }

    @Transactional(readOnly = true)
    public CommerceDto.WishlistResponse getWishlist(User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        List<WishlistItem> items = wishlistItemRepository.findByWishlistId(wishlist.getId());

        List<ProductDto.Summary> productSummaries = items.stream()
                .map(item -> productService.mapToSummary(item.getProduct()))
                .collect(Collectors.toList());

        return CommerceDto.WishlistResponse.builder()
                .wishlistId(wishlist.getId())
                .items(productSummaries)
                .totalItems(productSummaries.size())
                .build();
    }

    @Transactional
    public CommerceDto.WishlistResponse addToWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        if (wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId).isEmpty()) {
            WishlistItem item = WishlistItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .build();
            wishlistItemRepository.save(item);
        }

        return getWishlist(user);
    }

    @Transactional
    public CommerceDto.WishlistResponse removeFromWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .ifPresent(wishlistItemRepository::delete);

        return getWishlist(user);
    }
}
