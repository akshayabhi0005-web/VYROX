package com.veltrion.vyrox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Public health and system status endpoint")
public class HealthController {

    @Value("${app.demo-mode:true}")
    private boolean demoMode;

    @GetMapping
    @Operation(summary = "Check backend health status", description = "Public health check endpoint for cloud load balancers and deployment verification")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "vyrox-backend");
        health.put("version", "1.0.0");
        health.put("team", "TEAM VELTRION");
        health.put("demoMode", demoMode);
        health.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(health);
    }
}
