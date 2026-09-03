package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.ProductDto;
import com.veltrion.vyrox.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category hierarchy and navigation")
public class CategoryController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all main categories (Guest accessible)")
    public ResponseEntity<List<ProductDto.CategorySummary>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured categories for homepage carousel")
    public ResponseEntity<List<ProductDto.CategorySummary>> getFeaturedCategories() {
        return ResponseEntity.ok(productService.getFeaturedCategories());
    }
}
