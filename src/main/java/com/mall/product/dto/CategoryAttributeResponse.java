package com.mall.product.dto;

public record CategoryAttributeResponse(
    Long id,
    String name,
    String attributeType,
    boolean required
) {}
