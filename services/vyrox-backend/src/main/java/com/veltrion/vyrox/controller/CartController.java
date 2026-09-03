package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart operations (Protected)")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get user shopping cart")
    public ResponseEntity<CommerceDto.CartResponse> getCart(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(cartService.getCartDto(user));
    }

    @PostMapping("/add")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<CommerceDto.CartResponse> addToCart(
            Authentication authentication,
            @RequestBody CommerceDto.AddToCartRequest request
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(cartService.addToCart(user, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<CommerceDto.CartResponse> updateQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestBody CommerceDto.UpdateCartQuantityRequest request
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(cartService.updateQuantity(user, itemId, request.getQuantity()));
    }

    @PostMapping("/items/{itemId}/save-for-later")
    @Operation(summary = "Save item for later or move back to cart")
    public ResponseEntity<CommerceDto.CartResponse> toggleSaveForLater(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestParam boolean saveForLater
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(cartService.toggleSaveForLater(user, itemId, saveForLater));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CommerceDto.CartResponse> removeItem(
            Authentication authentication,
            @PathVariable Long itemId
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }
}
