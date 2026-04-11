package com.mall.cart.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CartResponse(
    Long skuId,
    String productName,
    String skuCode,
    Map<String, Object> attributes,
    BigDecimal price,
    int quantity,
    BigDecimal subTotal
) {
}
