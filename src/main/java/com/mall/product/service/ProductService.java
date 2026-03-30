package com.mall.product.service;

import com.mall.product.dto.ProductResponse;
import com.mall.product.dto.ProductSearchRequest;
import com.mall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return productRepository.searchProducts(request, pageable);
    }
}
