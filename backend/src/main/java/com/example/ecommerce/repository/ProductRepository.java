package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // 카테고리별 조회
    List<Product> findByCategoryId(UUID categoryId);
    
    // 속성 필터링 (동적 쿼리가 좋지만 일단 간단한 JPQL 예시)
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:grade IS NULL OR p.grade = :grade) AND " +
           "(:itemName IS NULL OR p.itemName = :itemName)")
    List<Product> searchProducts(@Param("categoryId") UUID categoryId, 
                                 @Param("grade") String grade, 
                                 @Param("itemName") String itemName);
}