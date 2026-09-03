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
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
@Tag(name = "Seller Portal", description = "Seller portal SKU, inventory and orders management")
public class SellerController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Seller Dashboard Overview")
    public ResponseEntity<Map<String, Object>> getSellerDashboard(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(dashboardService.getSellerStats(user));
    }
}
