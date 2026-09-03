package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String specGroup; // e.g. "Performance", "Display", "General", "Camera"
    private String specName;  // e.g. "RAM", "Processor", "Battery Capacity"
    private String specValue; // e.g. "16 GB", "Apple M3 Pro", "5000 mAh"
    private Integer displayOrder;
}
