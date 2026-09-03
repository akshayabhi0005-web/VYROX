package com.veltrion.vyrox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String title;
        private String sku;
        private String categoryName;
        private Long categoryId;
        private String brandName;
        private BigDecimal mrp;
        private BigDecimal sellingPrice;
        private Integer discountPercentage;
        private Double averageRating;
        private Integer reviewCount;
        private String mainImageUrl;
        private boolean inStock;
        private boolean isTopDeal;
        private boolean isTrending;
        private boolean isBestSeller;
        private boolean isQuickCommerceEligible;
        private String estimatedDeliveryDays;
        private boolean freeDelivery;
        private List<String> bankOffers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String sku;
        private String description;
        private String categoryName;
        private Long categoryId;
        private String brandName;
        private BigDecimal mrp;
        private BigDecimal sellingPrice;
        private Integer discountPercentage;
        private Integer stockQuantity;
        private boolean inStock;
        private Double averageRating;
        private Integer reviewCount;
        private List<String> images;
        private String mainImageUrl;
        private List<String> highlights;
        private List<String> bankOffers;
        private List<SpecItem> specifications;
        private String sellerName;
        private Double sellerRating;
        private String warrantyInfo;
        private boolean isTopDeal;
        private boolean isQuickCommerceEligible;
        private String estimatedDeliveryDays;
        private boolean freeDelivery;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecItem {
        private String group;
        private String name;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private Long id;
        private String name;
        private String slug;
        private String iconUrl;
        private String bannerUrl;
        private String description;
        private Integer subCategoryCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompareMatrix {
        private List<Summary> products;
        private Map<String, Map<String, List<String>>> groupedSpecs; // Group -> SpecName -> [Prod1Val, Prod2Val, ...]
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchFilterRequest {
        private String query;
        private Long categoryId;
        private Long brandId;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
        private Boolean isTopDeal;
        private Boolean isQuickCommerce;
        private String sortBy; // "popularity", "price_asc", "price_desc", "discount", "rating"
        private Integer page;
        private Integer size;
    }
}
