package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Portal", description = "Delivery partner app, GPS updates, and Doorstep OTP verification")
public class DeliveryController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Delivery Partner Active Assigned Deliveries")
    public ResponseEntity<Map<String, Object>> getDeliveryDashboard(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(dashboardService.getDeliveryPartnerStats(user));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify Doorstep OTP to confirm delivery")
    public ResponseEntity<Map<String, Object>> verifyDoorstepOtp(
            @RequestParam Long orderId,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(dashboardService.verifyDoorstepOtp(orderId, otp));
    }
}
