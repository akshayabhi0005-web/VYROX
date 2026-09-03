package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.ProductDto;
import com.veltrion.vyrox.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public catalog, search, details, deals, and comparison endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Search and filter products (Guest accessible)")
    public ResponseEntity<Page<ProductDto.Summary>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean isTopDeal,
            @RequestParam(required = false) Boolean isQuickCommerce,
            @RequestParam(required = false, defaultValue = "popularity") String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        ProductDto.SearchFilterRequest filter = ProductDto.SearchFilterRequest.builder()
                .query(query)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .isTopDeal(isTopDeal)
                .isQuickCommerce(isQuickCommerce)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(productService.searchProducts(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product full details (Guest accessible)")
    public ResponseEntity<ProductDto.Detail> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar recommended products")
    public ResponseEntity<List<ProductDto.Summary>> getSimilarProducts(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getSimilarProducts(id));
    }

    @GetMapping("/top-deals")
    @Operation(summary = "Get all top deals")
    public ResponseEntity<List<ProductDto.Summary>> getTopDeals() {
        return ResponseEntity.ok(productService.getTopDeals());
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending products")
    public ResponseEntity<List<ProductDto.Summary>> getTrending() {
        return ResponseEntity.ok(productService.getTrendingProducts());
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best sellers")
    public ResponseEntity<List<ProductDto.Summary>> getBestSellers() {
        return ResponseEntity.ok(productService.getBestSellers());
    }

    @GetMapping("/quick-commerce")
    @Operation(summary = "Get 15-minute quick commerce eligible items")
    public ResponseEntity<List<ProductDto.Summary>> getQuickCommerce() {
        return ResponseEntity.ok(productService.getQuickCommerceProducts());
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare up to 4 products side-by-side dynamically (Guest accessible)")
    public ResponseEntity<ProductDto.CompareMatrix> compareProducts(@RequestBody List<Long> productIds) {
        return ResponseEntity.ok(productService.compareProducts(productIds));
    }
}
