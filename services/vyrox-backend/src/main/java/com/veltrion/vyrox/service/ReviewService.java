package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.Product;
import com.veltrion.vyrox.model.ProductReview;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.repository.ProductRepository;
import com.veltrion.vyrox.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public List<CommerceDto.ReviewDto> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(r -> CommerceDto.ReviewDto.builder()
                        .id(r.getId())
                        .reviewerName(r.getReviewerName())
                        .rating(r.getRating())
                        .title(r.getTitle())
                        .comment(r.getComment())
                        .verifiedPurchase(r.isVerifiedPurchase())
                        .helpfulCount(r.getHelpfulCount() != null ? r.getHelpfulCount() : 0)
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CommerceDto.ReviewDto addReview(User user, Long productId, CommerceDto.CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .reviewerName(user.getFullName())
                .rating(Math.max(1, Math.min(5, request.getRating() != null ? request.getRating() : 5)))
                .title(request.getTitle())
                .comment(request.getComment())
                .verifiedPurchase(true)
                .helpfulCount(0)
                .build();

        review = reviewRepository.save(review);

        // Update product rating aggregate
        List<ProductReview> allReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        double avg = allReviews.stream().mapToInt(ProductReview::getRating).average().orElse(5.0);
        product.setAverageRating(Math.round(avg * 10.0) / 10.0);
        product.setReviewCount(allReviews.size());
        productRepository.save(product);

        return CommerceDto.ReviewDto.builder()
                .id(review.getId())
                .reviewerName(review.getReviewerName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .verifiedPurchase(review.isVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
