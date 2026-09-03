package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    List<ProductSpecification> findByProductIdOrderByDisplayOrderAsc(Long productId);
    List<ProductSpecification> findByProductIdIn(List<Long> productIds);
}
