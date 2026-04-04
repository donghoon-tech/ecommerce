package com.mall.product.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SkuResponse(
    Long id,
    String skuCode,
    Map<String, Object> attributes,
    BigDecimal additionalPrice,
    Integer stockQuantity
) {}
