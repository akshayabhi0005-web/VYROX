package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon engine and discounts")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/public")
    @Operation(summary = "Get available public coupons")
    public ResponseEntity<List<CommerceDto.CouponDto>> getCoupons() {
        return ResponseEntity.ok(couponService.getAvailableCoupons());
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon code against cart amount")
    public ResponseEntity<CommerceDto.CouponValidationResult> validateCoupon(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "0") BigDecimal cartTotal
    ) {
        return ResponseEntity.ok(couponService.validateCoupon(code, cartTotal));
    }
}
