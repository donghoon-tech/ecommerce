package com.mall.product.dto;

import com.mall.product.domain.ProductStatus;
import java.math.BigDecimal;

public record ProductSearchRequest(
    String name,
    Long categoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    ProductStatus status,
    int page,
    int size
) {
    public ProductSearchRequest(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        this(name, categoryId, minPrice, maxPrice, null, page, size);
    }
}
