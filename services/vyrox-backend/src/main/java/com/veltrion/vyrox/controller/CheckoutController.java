package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout calculation and order placement")
public class CheckoutController {

    private final OrderService orderService;
    private final AuthService authService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate order totals, discounts, coins, and delivery fee")
    public ResponseEntity<CommerceDto.CheckoutSummaryResponse> calculate(
            Authentication authentication,
            @RequestBody CommerceDto.CheckoutCalculateRequest request
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(orderService.calculateCheckout(user, request));
    }

    @PostMapping("/place-order")
    @Operation(summary = "Place final order")
    public ResponseEntity<CommerceDto.OrderDto> placeOrder(
            Authentication authentication,
            @RequestBody CommerceDto.CreateOrderRequest request
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(orderService.createOrder(user, request));
    }
}
