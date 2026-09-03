package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByIsTopDealTrue();

    List<Product> findByIsTrendingTrue();

    List<Product> findByIsBestSellerTrue();

    List<Product> findByIsFeaturedTrue();

    List<Product> findByIsQuickCommerceEligibleTrue();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandId(Long brandId);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%')) OR (p.brand IS NOT NULL AND LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :query, '%')))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId OR p.category.parent.id = :categoryId) AND " +
           "(:brandId IS NULL OR (p.brand IS NOT NULL AND p.brand.id = :brandId)) AND " +
           "(:minPrice IS NULL OR p.sellingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.sellingPrice <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.averageRating >= :minRating) AND " +
           "(:isTopDeal IS NULL OR p.isTopDeal = :isTopDeal) AND " +
           "(:isQuickCommerce IS NULL OR p.isQuickCommerceEligible = :isQuickCommerce)")
    Page<Product> searchProducts(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            @Param("isTopDeal") Boolean isTopDeal,
            @Param("isQuickCommerce") Boolean isQuickCommerce,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :excludeId ORDER BY p.averageRating DESC")
    List<Product> findSimilarProducts(@Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId, Pageable pageable);
}
