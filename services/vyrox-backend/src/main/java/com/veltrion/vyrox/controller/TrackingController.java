package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "Live Tracking", description = "Live map delivery GPS tracking (Real GPS & Simulation)")
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/order/{orderNumber}")
    @Operation(summary = "Get live GPS coordinates, ETA, driver info, and route for order (Public / Authenticated)")
    public ResponseEntity<CommerceDto.LiveTrackingDto> getLiveTracking(@PathVariable String orderNumber) {
        return ResponseEntity.ok(trackingService.getLiveTracking(orderNumber));
    }
}
