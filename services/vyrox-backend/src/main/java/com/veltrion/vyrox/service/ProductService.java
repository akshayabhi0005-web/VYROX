package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.ProductDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecificationRepository specificationRepository;

    public Page<ProductDto.Summary> searchProducts(ProductDto.SearchFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "averageRating");
        if ("price_asc".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "sellingPrice");
        } else if ("price_desc".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "sellingPrice");
        } else if ("discount".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "discountPercentage");
        } else if ("newest".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.searchProducts(
                filter.getQuery(),
                filter.getCategoryId(),
                filter.getBrandId(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getMinRating(),
                filter.getIsTopDeal(),
                filter.getIsQuickCommerce(),
                pageable
        );

        return products.map(this::mapToSummary);
    }

    public List<ProductDto.Summary> getTopDeals() {
        return productRepository.findByIsTopDealTrue().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public List<ProductDto.Summary> getTrendingProducts() {
        return productRepository.findByIsTrendingTrue().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public List<ProductDto.Summary> getBestSellers() {
        return productRepository.findByIsBestSellerTrue().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public List<ProductDto.Summary> getQuickCommerceProducts() {
        return productRepository.findByIsQuickCommerceEligibleTrue().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto.Detail getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        List<ProductSpecification> specs = specificationRepository.findByProductIdOrderByDisplayOrderAsc(id);

        List<ProductDto.SpecItem> specItems = specs.stream()
                .map(s -> ProductDto.SpecItem.builder()
                        .group(s.getSpecGroup())
                        .name(s.getSpecName())
                        .value(s.getSpecValue())
                        .build())
                .collect(Collectors.toList());

        return ProductDto.Detail.builder()
                .id(product.getId())
                .title(product.getTitle())
                .sku(product.getSku())
                .description(product.getDescription())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .mrp(product.getMrp())
                .sellingPrice(product.getSellingPrice())
                .discountPercentage(product.getDiscountPercentage())
                .stockQuantity(product.getStockQuantity())
                .inStock(product.isInStock())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .images(product.getImages())
                .mainImageUrl(product.getMainImageUrl())
                .highlights(product.getHighlights())
                .bankOffers(product.getBankOffers())
                .specifications(specItems)
                .sellerName(product.getSellerName())
                .sellerRating(product.getSellerRating())
                .warrantyInfo(product.getWarrantyInfo())
                .isTopDeal(product.isTopDeal())
                .isQuickCommerceEligible(product.isQuickCommerceEligible())
                .estimatedDeliveryDays(product.getEstimatedDeliveryDays())
                .freeDelivery(product.isFreeDelivery())
                .build();
    }

    public List<ProductDto.Summary> getSimilarProducts(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getCategory() == null) return Collections.emptyList();

        return productRepository.findSimilarProducts(product.getCategory().getId(), productId, PageRequest.of(0, 8))
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto.CompareMatrix compareProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least 2 products to compare");
        }
        if (productIds.size() > 4) {
            productIds = productIds.subList(0, 4); // Limit to 4 products max
        }

        List<Product> products = productRepository.findAllById(productIds);
        List<ProductDto.Summary> productSummaries = products.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        List<ProductSpecification> allSpecs = specificationRepository.findByProductIdIn(productIds);

        // Map: Group -> SpecName -> Map(ProductId -> Value)
        Map<String, Map<String, List<String>>> groupedSpecs = new LinkedHashMap<>();

        // Collect all distinct groups and names
        Set<String> groups = new LinkedHashSet<>();
        Map<String, Set<String>> groupSpecNames = new LinkedHashMap<>();

        for (ProductSpecification spec : allSpecs) {
            String group = spec.getSpecGroup() != null ? spec.getSpecGroup() : "General";
            groups.add(group);
            groupSpecNames.computeIfAbsent(group, k -> new LinkedHashSet<>()).add(spec.getSpecName());
        }

        // Always add Price, Brand, Rating to "Overview"
        Map<String, List<String>> overview = new LinkedHashMap<>();
        overview.put("Price", products.stream().map(p -> "₹" + p.getSellingPrice()).collect(Collectors.toList()));
        overview.put("Brand", products.stream().map(p -> p.getBrand() != null ? p.getBrand().getName() : "-").collect(Collectors.toList()));
        overview.put("Rating", products.stream().map(p -> p.getAverageRating() + " ★ (" + p.getReviewCount() + ")").collect(Collectors.toList()));
        overview.put("Warranty", products.stream().map(p -> p.getWarrantyInfo() != null ? p.getWarrantyInfo() : "1 Year Standard").collect(Collectors.toList()));
        groupedSpecs.put("Overview", overview);

        // Populate spec matrices
        for (String group : groups) {
            Map<String, List<String>> specMap = new LinkedHashMap<>();
            Set<String> names = groupSpecNames.get(group);
            if (names != null) {
                for (String name : names) {
                    List<String> values = new ArrayList<>();
                    for (Product p : products) {
                        String val = allSpecs.stream()
                                .filter(s -> s.getProduct().getId().equals(p.getId()) && s.getSpecGroup().equalsIgnoreCase(group) && s.getSpecName().equalsIgnoreCase(name))
                                .map(ProductSpecification::getSpecValue)
                                .findFirst()
                                .orElse("-");
                        values.add(val);
                    }
                    specMap.put(name, values);
                }
            }
            groupedSpecs.put(group, specMap);
        }

        return ProductDto.CompareMatrix.builder()
                .products(productSummaries)
                .groupedSpecs(groupedSpecs)
                .build();
    }

    public List<ProductDto.CategorySummary> getFeaturedCategories() {
        return categoryRepository.findByFeaturedTrueOrderByDisplayOrderAsc().stream()
                .map(c -> ProductDto.CategorySummary.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .iconUrl(c.getIconUrl())
                        .bannerUrl(c.getBannerUrl())
                        .description(c.getDescription())
                        .subCategoryCount(c.getSubCategories() != null ? c.getSubCategories().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProductDto.CategorySummary> getAllCategories() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc().stream()
                .map(c -> ProductDto.CategorySummary.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .iconUrl(c.getIconUrl())
                        .bannerUrl(c.getBannerUrl())
                        .description(c.getDescription())
                        .subCategoryCount(c.getSubCategories() != null ? c.getSubCategories().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    public ProductDto.Summary mapToSummary(Product p) {
        return ProductDto.Summary.builder()
                .id(p.getId())
                .title(p.getTitle())
                .sku(p.getSku())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .mrp(p.getMrp())
                .sellingPrice(p.getSellingPrice())
                .discountPercentage(p.getDiscountPercentage())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .mainImageUrl(p.getMainImageUrl())
                .inStock(p.isInStock())
                .isTopDeal(p.isTopDeal())
                .isTrending(p.isTrending())
                .isBestSeller(p.isBestSeller())
                .isQuickCommerceEligible(p.isQuickCommerceEligible())
                .estimatedDeliveryDays(p.getEstimatedDeliveryDays())
                .freeDelivery(p.isFreeDelivery())
                .bankOffers(p.getBankOffers())
                .build();
    }
}
