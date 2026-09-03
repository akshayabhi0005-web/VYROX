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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order history and lifecycle management")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get user order history")
    public ResponseEntity<List<CommerceDto.OrderDto>> getOrders(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(orderService.getUserOrders(user));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<CommerceDto.OrderDto> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(orderService.getOrderById(user, orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<CommerceDto.OrderDto> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        String reason = body != null ? body.get("reason") : "Customer request";
        return ResponseEntity.ok(orderService.cancelOrder(user, orderId, reason));
    }
}
