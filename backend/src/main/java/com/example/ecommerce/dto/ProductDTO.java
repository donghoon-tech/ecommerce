package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private UUID id;
    private UUID categoryId;
    private String categoryName;

    // Seller info (간단하게)
    private UUID sellerId;
    private String sellerName; // 상호명

    private String itemName;
    private String itemCondition; // 신재/고재
    private BigDecimal unitPrice;
    private String saleUnit;
    private Integer stockQuantity;
    private BigDecimal totalAmount;

    private String loadingAddress;
    private String loadingAddressDisplay; // 시/구 단위

    private String status; // pending, selling, etc
    private String rejectionReason;

    private List<String> imageUrls; // 별도 테이블 조회 후 매핑

    private LocalDateTime createdAt;
}