package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "User wishlist endpoints (Protected)")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get user wishlist items")
    public ResponseEntity<CommerceDto.WishlistResponse> getWishlist(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(wishlistService.getWishlist(user));
    }

    @PostMapping("/add/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<CommerceDto.WishlistResponse> addToWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(wishlistService.addToWishlist(user, productId));
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<CommerceDto.WishlistResponse> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(wishlistService.removeFromWishlist(user, productId));
    }
}
