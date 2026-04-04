package com.mall.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductCreateRequest(
    String name,
    BigDecimal basePrice,
    Long categoryId,
    Map<String, Object> attributes,
    List<SkuCreateRequest> skus
) {
    public record SkuCreateRequest(
        String skuCode,
        Map<String, Object> attributes,
        BigDecimal additionalPrice,
        Integer stockQuantity
    ) {}
}
