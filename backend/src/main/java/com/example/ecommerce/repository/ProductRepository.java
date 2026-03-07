package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
       List<Product> findBySellerId(UUID sellerId);

       List<Product> findByCategoryId(UUID categoryId);

       List<Product> findByStatus(Product.Status status);

       long countBySellerId(UUID sellerId);
}