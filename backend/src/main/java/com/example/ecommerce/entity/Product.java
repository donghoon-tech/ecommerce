package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId; // 논리적 연결

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    // 상품 속성 (필터링용)
    @Column(nullable = false)
    private String grade; // 상태 (신재, 고재 등)

    @Column(name = "item_name", nullable = false)
    private String itemName; // 품목 (파이프 등)

    @Column(nullable = false)
    private String spec; // 규격 (6m 등)

    @Column(name = "image_urls", columnDefinition = "text[]")
    private List<String> imageUrls;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private String description;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}