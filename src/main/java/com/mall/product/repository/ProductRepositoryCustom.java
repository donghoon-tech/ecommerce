package com.mall.product.repository;

import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable);
}
