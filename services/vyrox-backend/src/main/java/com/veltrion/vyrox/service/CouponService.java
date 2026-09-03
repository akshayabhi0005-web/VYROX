package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.Coupon;
import com.veltrion.vyrox.model.DiscountType;
import com.veltrion.vyrox.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<CommerceDto.CouponDto> getAvailableCoupons() {
        return couponRepository.findByActiveTrue().stream()
                .filter(c -> c.getValidUntil() == null || c.getValidUntil().isAfter(LocalDateTime.now()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CommerceDto.CouponValidationResult validateCoupon(String code, BigDecimal cartTotal) {
        if (code == null || code.trim().isEmpty()) {
            return CommerceDto.CouponValidationResult.builder()
                    .valid(false)
                    .message("Coupon code cannot be empty")
                    .discountAmount(BigDecimal.ZERO)
                    .build();
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (coupon == null || !coupon.isActive()) {
            return CommerceDto.CouponValidationResult.builder()
                    .valid(false)
                    .message("Invalid or expired coupon code: " + code)
                    .discountAmount(BigDecimal.ZERO)
                    .build();
        }

        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDateTime.now())) {
            return CommerceDto.CouponValidationResult.builder()
                    .valid(false)
                    .message("Coupon " + code + " has expired")
                    .discountAmount(BigDecimal.ZERO)
                    .build();
        }

        if (coupon.getMinOrderAmount() != null && cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CommerceDto.CouponValidationResult.builder()
                    .valid(false)
                    .message("Minimum order value for coupon " + code + " is ₹" + coupon.getMinOrderAmount())
                    .discountAmount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = cartTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(cartTotal) > 0) {
            discount = cartTotal;
        }

        return CommerceDto.CouponValidationResult.builder()
                .valid(true)
                .code(coupon.getCode())
                .message("Coupon applied successfully! You saved ₹" + discount)
                .discountAmount(discount)
                .build();
    }

    private CommerceDto.CouponDto mapToDto(Coupon c) {
        return CommerceDto.CouponDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .discountType(c.getDiscountType().name())
                .discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .validUntil(c.getValidUntil())
                .build();
    }
}
