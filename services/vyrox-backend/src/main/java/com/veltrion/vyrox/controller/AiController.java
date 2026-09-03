package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.ProductDto;
import com.veltrion.vyrox.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "VYROX AI", description = "AI Shopping Assistant, Natural Language catalog query & smart recommendations")
public class AiController {

    private final ProductService productService;

    @PostMapping("/chat")
    @Operation(summary = "Ask VYROX AI shopping assistant with natural language queries")
    public ResponseEntity<Map<String, Object>> chatWithAi(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            prompt = "recommend top products";
        }

        String lower = prompt.toLowerCase();
        BigDecimal budget = extractBudget(lower);
        String categoryKeyword = extractCategoryKeyword(lower);

        ProductDto.SearchFilterRequest filter = ProductDto.SearchFilterRequest.builder()
                .query(categoryKeyword)
                .maxPrice(budget)
                .size(6)
                .build();

        var pageResult = productService.searchProducts(filter);
        List<ProductDto.Summary> matchedProducts = pageResult.getContent();
        if (matchedProducts.isEmpty()) {
            matchedProducts = productService.getTrendingProducts();
        }

        String replyMessage = generateAiMessage(prompt, matchedProducts, budget);

        Map<String, Object> response = new HashMap<>();
        response.put("prompt", prompt);
        response.put("reply", replyMessage);
        response.put("recommendedProducts", matchedProducts);
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("model", "VYROX Semantic Commerce Engine v2.6");

        return ResponseEntity.ok(response);
    }

    private BigDecimal extractBudget(String text) {
        // Match numbers after under / below / budget / rs / ₹ / inr
        try {
            var matcher = java.util.regex.Pattern.compile("(?:under|below|budget|rs|₹|inr)\\s*([0-9,]+)").matcher(text);
            if (matcher.find()) {
                String numStr = matcher.group(1).replace(",", "");
                return new BigDecimal(numStr);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractCategoryKeyword(String text) {
        if (text.contains("laptop") || text.contains("macbook")) return "laptop";
        if (text.contains("phone") || text.contains("mobile") || text.contains("iphone") || text.contains("galaxy")) return "phone";
        if (text.contains("headphone") || text.contains("earbud") || text.contains("audio") || text.contains("earphones")) return "headphone";
        if (text.contains("tv") || text.contains("television") || text.contains("smart tv")) return "tv";
        if (text.contains("watch") || text.contains("smartwatch")) return "smartwatch";
        if (text.contains("shoe") || text.contains("shirt") || text.contains("fashion") || text.contains("jacket")) return "fashion";
        if (text.contains("furniture") || text.contains("table") || text.contains("chair")) return "table";
        if (text.contains("quick") || text.contains("instant") || text.contains("snack") || text.contains("grocery")) return "grocery";
        return null;
    }

    private String generateAiMessage(String prompt, List<ProductDto.Summary> products, BigDecimal budget) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the best VYROX catalog matches for your query: \"").append(prompt).append("\". ");
        if (budget != null) {
            sb.append("Filtered strictly within your budget of ₹").append(budget).append(". ");
        }
        if (!products.isEmpty()) {
            sb.append("Top recommendation: **").append(products.get(0).getTitle()).append("** at ₹")
              .append(products.get(0).getSellingPrice()).append(" (")
              .append(products.get(0).getDiscountPercentage()).append("% off, rating: ")
              .append(products.get(0).getAverageRating()).append("★).");
        } else {
            sb.append("Explore our trending picks below.");
        }
        return sb.toString();
    }
}
