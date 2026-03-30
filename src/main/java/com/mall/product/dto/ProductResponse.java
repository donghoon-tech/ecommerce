package com.mall.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductResponse(
    Long id,
    String name,
    BigDecimal basePrice,
    Map<String, Object> attributes,
    List<SkuResponse> skus
) {
}
