package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product customer ratings and reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all reviews for product (Guest accessible)")
    public ResponseEntity<List<CommerceDto.ReviewDto>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping("/product/{productId}")
    @Operation(summary = "Add a verified review (Protected)")
    public ResponseEntity<CommerceDto.ReviewDto> addReview(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody CommerceDto.CreateReviewRequest request
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(reviewService.addReview(user, productId, request));
    }
}
