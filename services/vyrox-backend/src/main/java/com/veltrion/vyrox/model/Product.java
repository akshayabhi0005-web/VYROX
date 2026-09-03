package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp; // Original Price / MRP

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice; // Final Discounted Price

    private Integer discountPercentage;

    private Integer stockQuantity;
    private boolean inStock;

    private Double averageRating;
    private Integer reviewCount;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    private String mainImageUrl;

    @ElementCollection
    @CollectionTable(name = "product_highlights", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "highlight")
    @Builder.Default
    private List<String> highlights = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_bank_offers", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "offer_text")
    @Builder.Default
    private List<String> bankOffers = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductSpecification> specifications = new ArrayList<>();

    private String sellerName;
    private Double sellerRating;
    private String warrantyInfo;

    private boolean isTopDeal;
    private boolean isFeatured;
    private boolean isTrending;
    private boolean isBestSeller;
    private boolean isQuickCommerceEligible; // 15-minute delivery

    private String estimatedDeliveryDays; // e.g. "Tomorrow by 11 PM", "15 Mins"
    private boolean freeDelivery;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (inStock == false && stockQuantity != null && stockQuantity > 0) {
            inStock = true;
        }
        if (mrp != null && sellingPrice != null && mrp.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = mrp.subtract(sellingPrice);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                discountPercentage = diff.multiply(BigDecimal.valueOf(100)).divide(mrp, 0, java.math.RoundingMode.HALF_UP).intValue();
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
