package com.mall.product.dto;

import java.util.List;

public record CategoryTreeResponse(
    Long id,
    String name,
    String path,
    List<CategoryTreeResponse> children
) {}
