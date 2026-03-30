package com.mall.product.dto;

import java.math.BigDecimal;

public record SkuResponse(
    Long id,
    String skuCode,
    String name,
    BigDecimal additionalPrice,
    Integer stockQuantity
) {
}
